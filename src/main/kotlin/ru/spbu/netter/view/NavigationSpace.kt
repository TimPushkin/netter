package ru.spbu.netter.view

import ru.spbu.netter.controller.NetworkEventHandler
import ru.spbu.netter.controller.NetworkNavigator
import tornadofx.*


class NavigationSpace : View() {
    private val navigator: NetworkEventHandler = find<NetworkNavigator>()

    override val root = pane().apply {
        setOnMousePressed { it?.let { navigator.handleMousePressed(it) } }
        setOnMouseDragged { it?.let { navigator.handleMouseDragged(it) } }
        setOnScroll { it?.let { navigator.handleScroll(it) } }
    }

    init {
        root += label("Import a network to be displayed here").apply {
            translateXProperty().bind(root.widthProperty() / 2)
            translateYProperty().bind(root.heightProperty() / 2)
        }
    }

    fun replaceNetwork(network: NetworkView) {
        root.children.clear()
        root += network.apply {
            translateX = root.width / 2
            translateY = root.height / 2
        }
    }
}

package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import ru.spbu.netter.controller.NetworkNavigationHandler
import ru.spbu.netter.controller.NetworkNavigator
import tornadofx.*


class NavigationSpace : View() {
    private val navigator: NetworkNavigationHandler = find<NetworkNavigator>()

    override val root = pane().apply {
        setOnMousePressed { it?.let { navigator.handleMousePressed(it) } }
        setOnMouseDragged { it?.let { navigator.handleMouseDragged(it) } }
        setOnScroll { it?.let { navigator.handleScroll(it) } }
    }

    init {
        root += label("Import a network to be displayed here").apply {
            translateXProperty().bind(
                Bindings.createDoubleBinding({ calculateRootCenterX() - width / 2 }, root.widthProperty())
            )
            translateYProperty().bind(
                Bindings.createDoubleBinding({ calculateRootCenterY() - height / 2 }, root.heightProperty())
            )
        }
    }

    fun replaceNetwork(graph: GraphView) {
        root.children.clear()
        root += graph.apply {
            translateX = calculateRootCenterX()
            translateY = calculateRootCenterY()
        }
    }

    private fun calculateRootCenterX() = root.width / 2

    private fun calculateRootCenterY() = root.height / 2
}

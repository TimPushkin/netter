package ru.spbu.netter.view

import ru.spbu.netter.controller.NetworkNavigationHandler
import ru.spbu.netter.controller.NetworkNavigator
import tornadofx.*


class NavigationSpace : View() {
    private val navigator: NetworkNavigationHandler = find<NetworkNavigator>()

    override val root = pane {
        label("Import a network to be displayed here")
    }

    fun replaceNetwork(graph: GraphView) {
        root.apply {
            children.clear()
            add(graph)
            setOnMouseDragged { it?.let { navigator.handleMouseDragged(it) } }
            setOnScroll { it?.let { navigator.handleScroll(it) } }
        }
    }
}

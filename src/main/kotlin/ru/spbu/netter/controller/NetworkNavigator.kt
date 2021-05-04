package ru.spbu.netter.controller

import javafx.scene.input.InputEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import ru.spbu.netter.view.GraphView
import tornadofx.*


class NetworkNavigator : Controller(), NetworkNavigationHandler {
    companion object {
        private const val MIN_ZOOM = 0.2
        private const val ZOOM_SCALING = 0.01
    }

    override fun handleMouseDragged(event: MouseEvent) {
        if (!event.isPrimaryButtonDown) return

        getGraphView(event).apply {
            translateX = event.x
            translateY = event.y
        }

        event.consume()
    }

    override fun handleScroll(event: ScrollEvent) {
        getGraphView(event).apply {
            scaleX = (scaleX + event.deltaY * ZOOM_SCALING).takeIf { it >= MIN_ZOOM } ?: MIN_ZOOM
            scaleY = scaleX
        }

        event.consume()
    }

    private fun getGraphView(event: InputEvent): GraphView {
        require(event.source is Pane) { "Unsupported event source: expected a Pane but was ${event.source::class}" }
        require((event.source as Pane).children.size == 1) { "Unsupported event source children: expected 1 child but was ${(event.source as Pane).children.size}" }
        require((event.source as Pane).children[0] is GraphView) { "Unsupported event source child: expected a GraphView but was ${(event.source as Pane).children[0]}" }

        return (event.source as Pane).children[0] as GraphView
    }
}

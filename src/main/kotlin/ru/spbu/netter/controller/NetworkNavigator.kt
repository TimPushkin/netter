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

        getGraphView(event)?.apply {
            translateX = event.x
            translateY = event.y

            event.consume()
        }
    }

    override fun handleScroll(event: ScrollEvent) {
        getGraphView(event)?.apply {
            scaleX = (scaleX + event.deltaY * ZOOM_SCALING).takeIf { it >= MIN_ZOOM } ?: MIN_ZOOM
            scaleY = scaleX

            event.consume()
        }
    }

    private fun getGraphView(event: InputEvent): GraphView? {
        require(event.source is Pane) { "Unsupported event source: expected a Pane but was ${event.source::class}" }

        with((event.source as Pane).children) {
            require(isNotEmpty()) { "Unsupported event source children: collection $this is empty" }
            return if (first() is GraphView) first() as GraphView else null
        }
    }
}

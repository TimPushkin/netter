package ru.spbu.netter.controller

import javafx.geometry.Point2D
import javafx.scene.input.InputEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.Pane
import ru.spbu.netter.view.NetworkView
import tornadofx.*


class NetworkNavigator : Controller(), NetworkEventHandler {
    lateinit var prevMousePressedPoint: Point2D

    companion object {
        private const val MIN_ZOOM = 0.2
        private const val ZOOM_SCALING = 0.01
    }

    override fun handleMousePressed(event: MouseEvent) {
        if (!event.isPrimaryButtonDown) return

        prevMousePressedPoint = Point2D(event.x, event.y)

        event.consume()
    }

    override fun handleMouseDragged(event: MouseEvent) {
        if (!event.isPrimaryButtonDown) return

        getNetworkView(event)?.apply {
            translateX += event.x - prevMousePressedPoint.x
            translateY += event.y - prevMousePressedPoint.y

            prevMousePressedPoint = Point2D(event.x, event.y)

            event.consume()
        }
    }

    override fun handleScroll(event: ScrollEvent) {
        getNetworkView(event)?.apply {
            scaleX = (scaleX + event.deltaY * ZOOM_SCALING).takeIf { it >= MIN_ZOOM } ?: MIN_ZOOM
            scaleY = scaleX

            event.consume()
        }
    }

    private fun getNetworkView(event: InputEvent): NetworkView? {
        require(event.source is Pane) { "Unsupported event source: expected a Pane but was ${event.source::class}" }

        with((event.source as Pane).children) {
            require(isNotEmpty()) { "Unsupported event source children: collection $this is empty" }
            return first().takeIf { it is NetworkView } as NetworkView?
        }
    }
}

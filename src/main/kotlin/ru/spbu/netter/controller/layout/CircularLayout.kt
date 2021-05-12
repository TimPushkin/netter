package ru.spbu.netter.controller.layout

import ru.spbu.netter.model.Network
import javafx.geometry.Point2D
import mu.KotlinLogging
import tornadofx.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


private val logger = KotlinLogging.logger {}

private const val CENTER_X = 0.0
private const val CENTER_Y = 0.0


class CircularLayout : Controller(), SimpleLayoutMethod {

    override fun calculateLayout(network: Network, repulsion: Double): List<Point2D> {
        if (network.isEmpty()) {
            logger.info { "There is nothing to lay out: network $network is empty" }
            return emptyList()
        }

        logger.info { "Placing nodes in a circular shape with repulsion $repulsion..." }

        val center = Point2D(CENTER_X, CENTER_Y)
        val coordinates = MutableList(network.nodes.size) { Point2D(0.0, 0.0) }

        if (network.nodes.size == 1) {
            coordinates[0] = center
            return coordinates
        }

        val angle = Math.toRadians(360.0 / network.nodes.size)
        val radius = repulsion * sqrt(2 / (1 - cos(angle)))
        var curr = Point2D(0.0, radius)

        network.nodes.keys.onEach {
            coordinates[it] = curr
            curr = curr.rotated(center, angle)
        }

        return coordinates
    }

    private fun Point2D.rotated(center: Point2D, angle: Double): Point2D {
        val sin = sin(angle)
        val cos = cos(angle)

        val tmp = subtract(center)
        val rotated = Point2D(
            tmp.x * cos - tmp.y * sin,
            tmp.x * sin + tmp.y * cos,
        )

        return rotated.add(center)
    }
}

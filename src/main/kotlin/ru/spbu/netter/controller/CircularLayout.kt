package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import javafx.geometry.Point2D
import tornadofx.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class CircularLayout : Controller(), LayoutMethod {
    override val direct = mutableMapOf<Int, Point2D>()

    override fun layout(
        graph: Graph,
        center: Point2D,
        radiusVertex: Double // TODO radiusVertex - размер вершины на изображении
    ) {
        if (graph.isEmpty()) {
            println("there is nothing to draw") // TODO logging
            return
        }
        println("Placing vertices in a circular shape...")

        val angle = Math.toRadians(360.0 / graph.vertices.size)
        val radius = 3 * radiusVertex * sqrt(1 / 2 / (1 - cos(angle)))
        var prev = Point2D(0.0, radius)

        graph.vertices.keys.onEach {
            direct[it] = prev
            prev = prev.rotate(center, angle)
        }
    }

    private fun Point2D.rotate(center: Point2D, angle: Double): Point2D {
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

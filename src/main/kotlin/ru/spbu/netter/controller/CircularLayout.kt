package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import javafx.geometry.Point2D
import tornadofx.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class CircularLayout : Controller(), LayoutMethod {

    override fun layOut(graph: Graph, center: Point2D, repulsion: Double): Array<Point2D> {
        if (graph.isEmpty()) {
            println("There is nothing to lay out: graph $graph is empty")
            return emptyArray()
        }

        println("Placing vertices in a circular shape...")

        val coordinates = Array(graph.vertices.size) { Point2D(0.0, 0.0) }

        val angle = Math.toRadians(360.0 / graph.vertices.size)
        val radius = repulsion * sqrt(2 / (1 - cos(angle)))
        var curr = Point2D(0.0, radius)

        graph.vertices.keys.onEach {
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

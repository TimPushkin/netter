package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import javafx.geometry.Point2D
import tornadofx.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class CircularLayout : Controller(), LayoutMethod {

    override fun layout(
        graph: Graph,
        center: Point2D,
        repulsion: Double
    ): ArrayList<Point2D> {

        if (graph.isEmpty()) {
            println("there is nothing to draw")
            return arrayListOf()
        }

        println("Placing vertices in a circular shape with repulsion $repulsion...")

        val coordinates = ArrayList<Point2D>(graph.vertices.size)

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

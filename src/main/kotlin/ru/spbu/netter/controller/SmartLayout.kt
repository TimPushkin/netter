package ru.spbu.netter.controller

import javafx.geometry.Point2D
import ru.spbu.netter.model.Graph
import tornadofx.*


class SmartLayout : Controller(), LayoutMethod {

    override fun layOut(graph: Graph, center: Point2D, repulsion: Double): Array<Point2D> {
        println("Placing vertices in a nice shape with repulsion $repulsion...")

        return Array(graph.vertices.size) { Point2D(0.0, 0.0) }
    }
}

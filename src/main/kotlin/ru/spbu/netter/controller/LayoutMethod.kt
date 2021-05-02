package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import javafx.geometry.Point2D


interface LayoutMethod {
    fun layout(
        graph: Graph,
        center: Point2D = Point2D(0.0, 0.0),
        repulsion: Double = 1.0
    ): ArrayList<Point2D>
}

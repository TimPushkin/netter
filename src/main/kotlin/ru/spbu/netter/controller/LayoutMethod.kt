package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import javafx.geometry.Point2D


interface LayoutMethod {

    fun layOut(
        graph: Graph,
        center: Point2D = Point2D(0.0, 0.0),
        repulsion: Double = 10.0
    ): Array<Point2D>
}

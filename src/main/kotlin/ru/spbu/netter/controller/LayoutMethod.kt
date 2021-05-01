package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import ru.spbu.netter.model.Vertex
import javafx.geometry.Point2D


interface LayoutMethod {
    val direct: Map<Int, Point2D>

    fun layout(graph: Graph, center: Point2D, radiusVertex: Double)
}

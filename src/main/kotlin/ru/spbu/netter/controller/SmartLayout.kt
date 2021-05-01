package ru.spbu.netter.controller

import javafx.geometry.Point2D
import ru.spbu.netter.model.Graph
import tornadofx.*


class SmartLayout : Controller(), LayoutMethod {
    override val direct = mutableMapOf<Int, Point2D>()

    override fun layout(graph: Graph, center: Point2D, radiusVertex: Double) {
        println("Placing vertices in a nice shape...")
    }
}

package ru.spbu.netter.controller

import javafx.geometry.Point2D
import ru.spbu.netter.model.Graph
import tornadofx.*


class SmartLayout : Controller(), LayoutMethod {
    override fun layout(
        graph: Graph,
        center: Point2D,
        k: Double
    ): ArrayList<Point2D> {

        println("Placing vertices in a nice shape...")
        return arrayListOf()
    }
}

package ru.spbu.netter.controller

import ru.spbu.netter.model.Network
import javafx.geometry.Point2D


interface LayoutMethod {

    fun layOut(
        network: Network,
        center: Point2D = Point2D(0.0, 0.0),
        repulsion: Double = 10.0
    ): Array<Point2D>
}

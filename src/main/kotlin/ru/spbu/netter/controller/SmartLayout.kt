package ru.spbu.netter.controller

import javafx.geometry.Point2D
import ru.spbu.netter.model.Network
import tornadofx.*


class SmartLayout : Controller(), LayoutMethod {

    override fun layOut(network: Network, center: Point2D, repulsion: Double): Array<Point2D> {
        println("Placing nodes in a nice shape with repulsion $repulsion...")

        return Array(network.nodes.size) { Point2D(0.0, 0.0) }
    }
}

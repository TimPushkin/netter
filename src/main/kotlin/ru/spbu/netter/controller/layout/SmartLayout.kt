package ru.spbu.netter.controller.layout

import javafx.geometry.Point2D
import mu.KotlinLogging
import ru.spbu.netter.model.Network
import tornadofx.*


private val logger = KotlinLogging.logger {}


class SmartLayout : Controller(), LayoutMethod {

    override fun layOut(network: Network, center: Point2D, repulsion: Double): Array<Point2D> {
        logger.info { "Placing nodes in a nice shape with repulsion $repulsion..." }

        return Array(network.nodes.size) { Point2D(0.0, 0.0) }
    }
}

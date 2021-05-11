package ru.spbu.netter.controller.layout

import javafx.geometry.Point2D
import mu.KotlinLogging
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import ru.spbu.netter.model.Network
import tornadofx.*


private val logger = KotlinLogging.logger {}


class ForceAtlas2Layout : Controller(), LayoutMethod {

    override fun layOut(network: Network, center: Point2D, repulsion: Double): List<Point2D> {
        logger.info { "Placing nodes in a nice shape with repulsion $repulsion..." }

        val forceAtlas2Algorithm = ForceAtlas2(network)

        repeat(100) { forceAtlas2Algorithm.start() }

        return forceAtlas2Algorithm.getFaNodes().map { Point2D(it.x, it.y) }
    }
}

package ru.spbu.netter.controller.layout

import javafx.geometry.Point2D
import mu.KotlinLogging
import org.gephi.graph.api.Edge
import org.gephi.graph.api.Graph
import org.gephi.graph.api.Node
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData
import ru.spbu.netter.model.Network
import tornadofx.*


private val logger = KotlinLogging.logger {}


class ForceAtlas2Layout : Controller(), LayoutMethod {

    override fun layOut(network: Network, center: Point2D, repulsion: Double): List<Point2D> {
        logger.info { "Placing nodes in a nice shape with repulsion $repulsion..." }

        val convertedNetwork = convertNetwork(network)

        val forceAtlas2Algorithm = ForceAtlas2().apply {
            setGraph(convertedNetwork)

            isOutboundAttractionDistribution = false
            isAdjustSizes = true
            isBarnesHutOptimize = true
            isLinLogMode = false
            isStrongGravityMode = false

            edgeWeightInfluence = 1.0
            jitterTolerance = 1.0
            scalingRatio = 10.0
            gravity = 1.0
            barnesHutTheta = 1.2
        }

        with(forceAtlas2Algorithm) {
            initAlgo()
            repeat(1000) { goAlgo() }
            endAlgo()
        }

        return List(network.nodes.size) { convertedNetwork.nodes[it].run { Point2D(x, y) } }
    }

    private fun convertNetwork(network: Network): Graph {
        val nodes = network.nodes.values.map { Node(it.id, ForceAtlas2LayoutData()) }
        val edges = network.links.map { Edge(nodes[it.n1.id], nodes[it.n2.id]) }
        return Graph(nodes, edges)
    }
}

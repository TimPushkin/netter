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


class ForceAtlas2Layout : Controller(), SmartLayoutMethod {

    override fun applyLayout(
        network: Network,
        loopsNum: Int,
        applyOutboundAttrDistr: Boolean,
        applyAdjustSizes: Boolean,
        applyBarnesHut: Boolean,
        applyLinLogMode: Boolean,
        applyStrongGravityMode: Boolean,
        withJitterTolerance: Double,
        withScalingRatio: Double,
        withGravity: Double,
        withBarnesHutTheta: Double,
    ) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to lay out: network $network is empty" }
            return
        }

        logger.info { "Placing nodes using $loopsNum loops of ForceAtlas2 with the following parameters:" }
        logger.info { "-- outboundAttractionDistribution: $applyOutboundAttrDistr" }
        logger.info { "-- adjustSizes: $applyOutboundAttrDistr" }
        logger.info { "-- barnesHutOptimize: $applyOutboundAttrDistr" }
        logger.info { "-- linLogMode: $applyOutboundAttrDistr" }
        logger.info { "-- strongGravityMode: $applyOutboundAttrDistr" }
        logger.info { "-- jitterTolerance: $applyOutboundAttrDistr" }
        logger.info { "-- scalingRatio: $applyOutboundAttrDistr" }
        logger.info { "-- gravity: $applyOutboundAttrDistr" }
        logger.info { "-- barnesHutTheta: $applyOutboundAttrDistr" }

        val convertedNetwork = convertNetwork(network)

        val forceAtlas2Algorithm = ForceAtlas2().apply {
            setGraph(convertedNetwork)

            isOutboundAttractionDistribution = applyOutboundAttrDistr
            isAdjustSizes = applyAdjustSizes
            isBarnesHutOptimize = applyBarnesHut
            isLinLogMode = applyLinLogMode
            isStrongGravityMode = applyStrongGravityMode

            edgeWeightInfluence = 1.0

            jitterTolerance = withJitterTolerance
            scalingRatio = withScalingRatio
            gravity = withGravity
            barnesHutTheta = withBarnesHutTheta
        }

        with(forceAtlas2Algorithm) {
            initAlgo()
            repeat(loopsNum) { goAlgo() }
            endAlgo()
        }

        with(convertedNetwork.nodes) {
            network.nodes.values.forEach {
                get(it.id).run {
                    it.x = x
                    it.y = y
                }
            }
        }
    }

    private fun convertNetwork(network: Network): Graph {
        val nodes = network.nodes.values.map { Node(it.id, ForceAtlas2LayoutData()) }
        val edges = network.links.map { Edge(nodes[it.n1.id], nodes[it.n2.id]) }
        return Graph(nodes, edges)
    }
}

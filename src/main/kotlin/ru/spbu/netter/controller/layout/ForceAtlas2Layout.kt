package ru.spbu.netter.controller.layout

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
    override val status = TaskStatus()

    override fun applyLayout(
        network: Network,
        loopsNum: Int,
        applyAdjustSizes: Boolean,
        applyBarnesHut: Boolean,
        applyLinLogMode: Boolean,
        applyStrongGravityMode: Boolean,
        withJitterTolerance: Double,
        withScalingRatio: Double,
        withGravity: Double,
        withBarnesHutTheta: Double,
        executeOnSuccess: () -> Unit,
    ) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to lay out using ForceAtlas2: network $network is empty" }
            return
        }

        logger.info { "Placing nodes using $loopsNum loops of ForceAtlas2 with the following parameters:" }
        logger.info { "-- adjustSizes: $applyAdjustSizes" }
        logger.info { "-- barnesHutOptimize: $applyBarnesHut" }
        logger.info { "-- linLogMode: $applyLinLogMode" }
        logger.info { "-- strongGravityMode: $applyStrongGravityMode" }
        logger.info { "-- jitterTolerance: $withJitterTolerance" }
        logger.info { "-- scalingRatio: $withScalingRatio" }
        logger.info { "-- gravity: $withGravity" }
        logger.info { "-- barnesHutTheta: $withBarnesHutTheta" }

        val convertedNetwork = convertNetwork(network)

        val forceAtlas2Algorithm = ForceAtlas2().apply {
            setGraph(convertedNetwork)

            isOutboundAttractionDistribution = false
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

        runAsync(true, status) {
            updateMessage("ForceAtlas2 layout")

            with(forceAtlas2Algorithm) {
                initAlgo()
                loopsNum.toLong().let {
                    for (i in 1..it) {
                        updateProgress(i, it)
                        goAlgo()
                    }
                }
                endAlgo()
            }
        } success {
            with(convertedNetwork.nodes) {
                network.nodes.values.withEach {
                    get(id).let { resultingNode ->
                        x = resultingNode.x
                        y = resultingNode.y
                    }
                }
            }

            logger.info { "Placing nodes using ForceAtlas2 has been finished" }

            executeOnSuccess()
        } fail { ex ->
            throw RuntimeException("Placing nodes using ForceAtlas2 has been failed", ex)
        }
    }

    private fun convertNetwork(network: Network): Graph {
        val nodes = network.nodes.values.map { Node(it.id, ForceAtlas2LayoutData()) }
        val edges = network.links.map { Edge(nodes[it.n1.id], nodes[it.n2.id]) }
        return Graph(nodes, edges)
    }
}

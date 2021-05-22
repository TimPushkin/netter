package ru.spbu.netter.controller.centrality

import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.scoring.HarmonicCentrality
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import ru.spbu.netter.model.Network
import tornadofx.*


private val logger = KotlinLogging.logger {}


class HarmonicCentralityIdentifier : Controller(), CentralityIdentifier {

    override fun identifyCentrality(network: Network) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to inspect for centrality: network $network is empty" }
            return
        }

        logger.info { "Identifying centrality..." }

        runAsync {
            HarmonicCentrality(convertNetwork(network)).scores
        } success { centralityValues ->
            network.nodes.values.withEach {
                centralityValues[id]?.let { centrality = it }
                    ?: throw IllegalStateException("Node $id not found in the harmonic centrality calculation result")
            }

            logger.info { "Centrality identification has been finished" }
        } fail { ex ->
            throw RuntimeException("Harmonic centrality calculation has been failed", ex)
        }
    }

    private fun convertNetwork(network: Network): Graph<Int, DefaultEdge> {
        val graph = DefaultUndirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)

        network.nodes.values.forEach { graph.addVertex(it.id) }
        network.links.forEach { graph.addEdge(it.n1.id, it.n2.id) }

        return graph
    }
}

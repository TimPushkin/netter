package ru.spbu.netter.controller.centrality

import mu.KotlinLogging
import org.jgrapht.Graph
import org.jgrapht.alg.scoring.HarmonicCentrality
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import ru.spbu.netter.model.Network
import tornadofx.Controller

private val logger = KotlinLogging.logger {}

class HarmonicCentralityIdentifier : Controller(), CentralityIdentifier {

    override fun identifyCentrality(network: Network) {

        logger.info { "Identifying centrality..." }

        val centralityValues = HarmonicCentrality(convertNetwork(network)).scores
        network.nodes.values.forEach { node ->  
            centralityValues[node.id]?.let { node.centrality = it }
                ?: throw IllegalStateException("Node ${node.id} not found in the harmonic centrality calculation result")
        }
    }

    private fun convertNetwork(network: Network): Graph<Int, DefaultEdge> {
        val graph = DefaultUndirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)

        network.nodes.forEach { graph.addVertex(it.key) }
        network.links.forEach { graph.addEdge(it.n1.id, it.n2.id) }

        return graph
    }
}

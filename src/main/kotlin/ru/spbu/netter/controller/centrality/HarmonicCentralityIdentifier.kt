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
    override val status = TaskStatus()

    override fun identifyCentrality(network: Network, executeOnSuccess: () -> Unit) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to inspect for centrality: network $network is empty" }
            return
        }

        logger.info { "Identifying centrality..." }

        runAsync(true, status) {
            updateMessage(messages["ProgressMessage"])

            HarmonicCentrality(convertNetwork(network)).scores
        } success { centralityValues ->
            network.nodes.values.withEach {
                centralityValues[id]?.let { centrality = it }
                    ?: throw IllegalStateException("Node $id not found in the harmonic centrality calculation result")
            }

            logger.info { "Harmonic centrality identification has been finished" }

            executeOnSuccess()
        } fail { ex ->
            throw RuntimeException("Harmonic centrality identification has been failed", ex)
        }
    }

    private fun convertNetwork(network: Network): Graph<Int, DefaultEdge> {
        val graph = DefaultUndirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)

        network.nodes.values.forEach { graph.addVertex(it.id) }
        network.links.forEach { graph.addEdge(it.n1.id, it.n2.id) }

        return graph
    }
}

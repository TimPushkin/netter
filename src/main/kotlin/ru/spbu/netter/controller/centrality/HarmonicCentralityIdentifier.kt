package ru.spbu.netter.controller.centrality

import org.jgrapht.Graph
import org.jgrapht.alg.scoring.HarmonicCentrality
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import ru.spbu.netter.model.Network
import tornadofx.Controller


class HarmonicCentralityIdentifier : Controller(), CentralityIdentifier {

    override fun identifyCentrality(network: Network) {
        val buf = HarmonicCentrality(convertNetwork(network)).scores
        network.nodes.forEach {
            it.value.centrality = buf[it.key]!!
        }
    }

    private fun convertNetwork(network: Network): Graph<Int, DefaultEdge> {
        var graph = DefaultUndirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)
        network.nodes.forEach {
            graph.addVertex(it.key)
        }
        network.links.forEach {
            graph.addEdge(it.n1.id, it.n2.id)
        }
        return graph
    }

}

package ru.spbu.netter.controller.centrality

import mu.KotlinLogging
import ru.spbu.netter.model.Network
import tornadofx.*


private val logger = KotlinLogging.logger {}


class DegreeCentralityIdentifier : Controller(), CentralityIdentifier {

    override fun identifyCentrality(network: Network) {
        logger.info { "Identifying centrality..." }

        val centralityValues = Array(network.nodes.size) { 0.0 }

        for (link in network.links) {
            centralityValues[link.n1.id]++
            centralityValues[link.n2.id]++
        }

        for (node in network.nodes.values) node.centrality = centralityValues[node.id]
    }
}

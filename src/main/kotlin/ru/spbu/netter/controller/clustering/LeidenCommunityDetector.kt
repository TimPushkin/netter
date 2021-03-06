package ru.spbu.netter.controller.clustering

import mu.KotlinLogging
import nl.cwts.networkanalysis.Clustering
import nl.cwts.networkanalysis.LeidenAlgorithm
import nl.cwts.networkanalysis.Network as LeidenNetwork
import ru.spbu.netter.model.Network
import tornadofx.*
import java.util.*


private val logger = KotlinLogging.logger {}

// The number of iterations of the algorithm. If equals 0, the algorithm runs until no more improvements are possible
private const val ITERATIONS_NUM = 0

// Sets the randomness factor used by the algorithm in its refinement phase
private const val RANDOMNESS = 1e-2


class LeidenCommunityDetector : Controller(), CommunityDetector {
    override val status = TaskStatus()

    override fun detectCommunities(network: Network, resolution: Double, executeOnSuccess: () -> Unit) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to inspect for communities: network $network is empty" }
            return
        }

        logger.info { "Detecting communities with resolution $resolution..." }

        val convertedNetwork = convertNetwork(network)
        val clustering = Clustering(convertedNetwork.nNodes)

        require(resolution > 0) { "Wrong resolution: resolution must be positive but was $resolution" }
        val leidenAlgorithm = LeidenAlgorithm(resolution, ITERATIONS_NUM, RANDOMNESS, Random())

        runAsync(true, status) {
            updateMessage("Community detection")

            leidenAlgorithm.improveClustering(convertedNetwork, clustering)
        } success {
            applyClustering(network, clustering)

            logger.info { "Leiden community detection has been finished" }

            executeOnSuccess()
        } fail { ex ->
            throw RuntimeException("Leiden community detection has been failed", ex)
        }
    }

    private fun convertNetwork(network: Network): LeidenNetwork {
        with(network.links.toTypedArray()) {
            return LeidenNetwork(
                network.nodes.size,
                false,
                arrayOf(IntArray(size) { this[it].n1.id }, IntArray(size) { this[it].n2.id }),
                false,
                false,
            )
        }
    }

    private fun applyClustering(network: Network, clustering: Clustering) {
        require(network.nodes.size == clustering.nNodes) {
            "Clustering application failed: network nodes number (${network.nodes.size}) differs from clustering nodes number (${clustering.nNodes})"
        }
        with(clustering.clusters) { for (node in network.nodes.values) node.community = get(node.id) }
    }
}

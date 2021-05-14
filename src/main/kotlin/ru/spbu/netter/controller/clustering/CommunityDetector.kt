package ru.spbu.netter.controller.clustering

import ru.spbu.netter.model.Network


interface CommunityDetector {

    fun detectCommunities(
        network: Network,
        resolution: Double = 0.2,
    )
}

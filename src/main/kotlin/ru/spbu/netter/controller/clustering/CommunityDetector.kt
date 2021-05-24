package ru.spbu.netter.controller.clustering

import ru.spbu.netter.controller.Statusable
import ru.spbu.netter.model.Network


interface CommunityDetector : Statusable {

    fun detectCommunities(
        network: Network,
        resolution: Double = 0.2,
        executeOnSuccess: () -> Unit = {},
    )
}

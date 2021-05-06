package ru.spbu.netter.controller

import ru.spbu.netter.model.Network


interface CommunityDetector {

    fun detectCommunities(network: Network)
}

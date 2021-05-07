package ru.spbu.netter.controller.io

import ru.spbu.netter.model.Network


interface UriIOHandler {
    companion object {
        internal const val MIN_NODE_ID = 0
        internal const val MIN_COMMUNITY = 0
        internal const val MIN_CENTRALITY = 0.0
    }

    fun importNetwork(network: Network, uri: String, user: String, password: String)

    fun exportNetwork(network: Network, uri: String, user: String, password: String)
}

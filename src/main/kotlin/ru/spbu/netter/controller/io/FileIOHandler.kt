package ru.spbu.netter.controller.io

import ru.spbu.netter.model.Network
import java.io.File


interface FileIOHandler {
    companion object {
        internal const val MIN_NODE_ID = 0
        internal const val MIN_COMMUNITY = 0
        internal const val MIN_CENTRALITY = 0.0
    }

    fun importNetwork(network: Network, file: File)

    fun exportNetwork(network: Network, file: File)
}

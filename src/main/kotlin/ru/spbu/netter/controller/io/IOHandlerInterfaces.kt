package ru.spbu.netter.controller.io

import ru.spbu.netter.model.Network
import java.io.File


interface FileIOHandler {

    fun importNetwork(network: Network, file: File)

    fun exportNetwork(network: Network, file: File)
}

interface UriIOHandler {

    fun importNetwork(network: Network, uri: String, username: String, password: String)

    fun exportNetwork(network: Network, uri: String, username: String, password: String)
}

internal object IOHandlerData {
    const val MIN_NODE_ID = 0
    const val MIN_COMMUNITY = 0
    const val MIN_CENTRALITY = 0.0
}

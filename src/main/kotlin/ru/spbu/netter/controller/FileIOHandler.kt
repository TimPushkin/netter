package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph


interface FileIOHandler {
    companion object {
        internal const val MIN_VERTEX_ID = 0
        internal const val MIN_COMMUNITY = 0
        internal const val MIN_CENTRALITY = 0.0
    }


    fun importNetwork(graph: Graph, filePath: String)

    fun exportNetwork(graph: Graph, filePath: String)
}

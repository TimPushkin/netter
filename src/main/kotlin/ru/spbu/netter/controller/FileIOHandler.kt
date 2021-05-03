package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import java.io.File


interface FileIOHandler {
    companion object {
        internal const val MIN_VERTEX_ID = 0
        internal const val MIN_COMMUNITY = 0
        internal const val MIN_CENTRALITY = 0.0
    }

    fun importNetwork(graph: Graph, file: File)

    fun exportNetwork(graph: Graph, file: File)
}

package ru.spbu.netter.model


interface Vertex {
    companion object {
        internal const val DEFAULT_COMMUNITY = 0
        internal const val DEFAULT_CENTRALITY = 1.0
    }

    val id: Int
    var community: Int
    var centrality: Double
}

interface Edge {
    val v1: Vertex
    val v2: Vertex
}

interface Graph {
    val vertices: Map<Int, Vertex>
    val edges: Collection<Edge>

    fun addVertex(id: Int): Vertex

    fun addEdge(id1: Int, id2: Int): Edge
}

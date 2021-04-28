package ru.spbu.netter.model


interface Vertex {
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

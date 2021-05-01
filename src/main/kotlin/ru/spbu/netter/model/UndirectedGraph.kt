package ru.spbu.netter.model


class UndirectedGraph : Graph {
    override val vertices = mutableMapOf<Int, Vertex>()
    override val edges = mutableSetOf<Edge>()

    override fun addVertex(id: Int): Vertex = vertices.getOrPut(id) { VertexImpl(id) }

    override fun addEdge(id1: Int, id2: Int): Edge =
        UndirectedEdge(addVertex(id1), addVertex(id2)).also { edges.add(it) }

    override fun isEmpty(): Boolean = vertices.isEmpty()


    private data class VertexImpl(override val id: Int) : Vertex {
        override var community = Vertex.DEFAULT_COMMUNITY
        override var centrality = Vertex.DEFAULT_CENTRALITY
    }

    private data class UndirectedEdge(override val v1: Vertex, override val v2: Vertex) : Edge {
        override fun equals(other: Any?) =
            if (other is UndirectedEdge) v1 == other.v1 && v2 == other.v2 || v1 == other.v2 && v2 == other.v1 else false

        override fun hashCode() = v1.hashCode() xor v2.hashCode()
    }
}

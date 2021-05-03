package ru.spbu.netter.model

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*


class UndirectedGraph : Graph {
    override val vertices = mutableMapOf<Int, Vertex>()
    override val edges = mutableSetOf<Edge>()

    override fun addVertex(id: Int): Vertex = vertices.getOrPut(id) { VertexImpl(id) }

    override fun addEdge(id1: Int, id2: Int): Edge =
        UndirectedEdge(addVertex(id1), addVertex(id2)).also { edges.add(it) }

    override fun clear() {
        vertices.clear()
        edges.clear()
    }

    override fun isEmpty(): Boolean = vertices.isEmpty()

    private data class VertexImpl(override val id: Int) : Vertex {
        override val communityProperty = SimpleIntegerProperty(this, "community", Vertex.DEFAULT_COMMUNITY)
        override var community by communityProperty

        override val centralityProperty = SimpleDoubleProperty(this, "centrality", Vertex.DEFAULT_CENTRALITY)
        override var centrality by centralityProperty
    }

    private data class UndirectedEdge(override val v1: Vertex, override val v2: Vertex) : Edge {

        override fun equals(other: Any?) =
            if (other is UndirectedEdge) v1 == other.v1 && v2 == other.v2 || v1 == other.v2 && v2 == other.v1 else false

        override fun hashCode() = v1.hashCode() xor v2.hashCode()
    }
}

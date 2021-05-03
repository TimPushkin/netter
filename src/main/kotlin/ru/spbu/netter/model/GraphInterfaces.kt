package ru.spbu.netter.model

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty


interface Vertex {
    val id: Int

    val communityProperty: IntegerProperty
    var community: Int

    val centralityProperty: DoubleProperty
    var centrality: Double

    companion object {
        internal const val DEFAULT_COMMUNITY = 0
        internal const val DEFAULT_CENTRALITY = 1.0
    }
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

    fun clear()

    fun isEmpty(): Boolean
}

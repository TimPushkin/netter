package ru.spbu.netter.model

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty


interface Node {
    val id: Int

    val communityProperty: IntegerProperty
    var community: Int

    val centralityProperty: DoubleProperty
    var centrality: Double

    val xProperty: DoubleProperty
    var x: Double

    val yProperty: DoubleProperty
    var y: Double

    companion object {
        internal const val DEFAULT_COMMUNITY = 0
        internal const val DEFAULT_CENTRALITY = 0.5

        internal const val DEFAULT_X = 0.0
        internal const val DEFAULT_Y = 0.0
    }
}

interface Link {
    val n1: Node
    val n2: Node
}

interface Network {
    val nodes: Map<Int, Node>
    val links: Collection<Link>

    fun addNode(id: Int): Node

    fun addLink(id1: Int, id2: Int): Link

    fun isEmpty(): Boolean
}

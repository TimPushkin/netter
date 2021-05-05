package ru.spbu.netter.model

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*


class UndirectedNetwork : Network {
    override val nodes = mutableMapOf<Int, Node>()
    override val links = mutableSetOf<Link>()

    override fun addNode(id: Int): Node = nodes.getOrPut(id) { UndirectedNode(id) }

    override fun addLink(id1: Int, id2: Int): Link = UndirectedLink(addNode(id1), addNode(id2)).also { links.add(it) }

    override fun isEmpty(): Boolean = nodes.isEmpty()

    private data class UndirectedNode(override val id: Int) : Node {
        override val communityProperty = SimpleIntegerProperty(this, "community", Node.DEFAULT_COMMUNITY)
        override var community by communityProperty

        override val centralityProperty = SimpleDoubleProperty(this, "centrality", Node.DEFAULT_CENTRALITY)
        override var centrality by centralityProperty
    }

    private data class UndirectedLink(override val v1: Node, override val v2: Node) : Link {

        override fun equals(other: Any?) =
            if (other is UndirectedLink) v1 == other.v1 && v2 == other.v2 || v1 == other.v2 && v2 == other.v1 else false

        override fun hashCode() = v1.hashCode() xor v2.hashCode()
    }
}

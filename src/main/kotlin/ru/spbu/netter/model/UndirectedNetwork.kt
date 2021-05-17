package ru.spbu.netter.model

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.*


class UndirectedNetwork : Network {
    override val nodes = mutableMapOf<Int, Node>()
    override val links = mutableSetOf<Link>()

    override fun addNode(id: Int): Node = nodes.getOrPut(id) { UndirectedNode(id) }

    override fun addLink(id1: Int, id2: Int): Link = UndirectedLink(addNode(id1), addNode(id2)).also { links.add(it) }

    override fun isEmpty() = nodes.isEmpty()

    private data class UndirectedNode(override val id: Int) : Node {
        override val communityProperty = SimpleIntegerProperty(this, "community", Node.DEFAULT_COMMUNITY)
        override var community by communityProperty

        override val centralityProperty = SimpleDoubleProperty(this, "centrality", Node.DEFAULT_CENTRALITY)
        override var centrality by centralityProperty

        override val xProperty = SimpleDoubleProperty(this, "x", Node.DEFAULT_X)
        override var x by xProperty

        override val yProperty = SimpleDoubleProperty(this, "y", Node.DEFAULT_Y)
        override var y by yProperty
    }

    private data class UndirectedLink(override val n1: Node, override val n2: Node) : Link {

        override fun equals(other: Any?) =
            other is UndirectedLink && (n1 == other.n1 && n2 == other.n2 || n1 == other.n2 && n2 == other.n1)

        override fun hashCode() = n1.hashCode() xor n2.hashCode()
    }
}

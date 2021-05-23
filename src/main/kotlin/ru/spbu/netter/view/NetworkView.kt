package ru.spbu.netter.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.Group
import ru.spbu.netter.model.Network
import tornadofx.*


class NetworkView(val network: Network) : Group() {
    private val colorsNumProperty = SimpleIntegerProperty(this, "colorsNum", calculateColorsNum())
    private var colorsNum by colorsNumProperty

    private val nodes = network.nodes.values.associateWith { NodeView(it, colorsNumProperty) }
    private val links = network.links.map {
        val n1 = nodes[it.n1] ?: throw IllegalStateException("NodeView not found for node ${it.n1}")
        val n2 = nodes[it.n2] ?: throw IllegalStateException("NodeView not found for node ${it.n2}")
        LinkView(n1, n2)
    }

    init {
        links.forEach { add(it) }
        updateNodeOrder()
    }

    fun updateNodeOrder() {
        children.retainAll { it is LinkView }
        nodes.values.sortedByDescending { it.node.centrality }.forEach {
            add(it)
            add(it.label)
        }
    }

    fun updateColorsNum() {
        colorsNum = calculateColorsNum()
    }

    private fun calculateColorsNum() = (network.nodes.values.maxOfOrNull { it.community } ?: -1) + 1
}

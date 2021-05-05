package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Point2D
import javafx.scene.Group
import ru.spbu.netter.model.Network
import tornadofx.*


class NetworkView(val network: Network) : Group() {
    private val colorsNumProperty = SimpleIntegerProperty(this, "colorsNum", calculateColorsNum())

    private val nodes = network.nodes.values.associateWith { NodeView(it, 0.0, 0.0, colorsNumProperty) }
    private val links = network.links.associateWith {
        val v1 = nodes[it.v1] ?: throw IllegalStateException("NodeView not found for node ${it.v1}")
        val v2 = nodes[it.v2] ?: throw IllegalStateException("NodeView not found for node ${it.v2}")
        LinkView(v1, v2)
    }

    init {
        colorsNumProperty.bind(
            Bindings.createObjectBinding(
                ::calculateColorsNum,
                *network.nodes.values.map { it.communityProperty }.toTypedArray()
            )
        )

        links.values.forEach { add(it) }
        nodes.values.forEach {
            add(it)
            add(it.label)
        }
    }

    fun applyLayout(coordinates: Array<Point2D>) {
        nodes.values.forEachIndexed { i, nodeView ->
            nodeView.centerX = coordinates[i].x
            nodeView.centerY = coordinates[i].y
        }
    }

    private fun calculateColorsNum() = (network.nodes.values.maxOfOrNull { it.community } ?: -1) + 1
}

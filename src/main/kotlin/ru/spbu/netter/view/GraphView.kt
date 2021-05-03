package ru.spbu.netter.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Point2D
import javafx.scene.Group
import ru.spbu.netter.model.Graph
import tornadofx.*


class GraphView(val graph: Graph) : Group() {
    private val communitiesNumProperty =
        SimpleIntegerProperty(this, "communitiesNum", (graph.vertices.values.maxOfOrNull { it.community } ?: 0) + 1)
    private var communitiesNum by communitiesNumProperty

    private val vertices = graph.vertices.values.associateWith { VertexView(it, 0.0, 0.0, communitiesNumProperty) }
    private val edges = graph.edges.associateWith {
        val v1 = vertices[it.v1] ?: throw IllegalStateException("VertexView not found for vertex ${it.v1}")
        val v2 = vertices[it.v2] ?: throw IllegalStateException("VertexView not found for vertex ${it.v2}")
        EdgeView(v1, v2)
    }

    init {
        vertices.values.forEach { add(it) }
        edges.values.forEach { add(it) }
    }

    fun applyLayout(coordinates: Array<Point2D>) {
        vertices.values.forEachIndexed { i, vertexView ->
            vertexView.centerX = coordinates[i].x
            vertexView.centerY = coordinates[i].y
        }
    }
}

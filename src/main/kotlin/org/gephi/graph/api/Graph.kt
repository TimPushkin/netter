package org.gephi.graph.api


class Graph(val nodes: List<Node>, val edges: List<Edge>) {
    private val degrees = MutableList(nodes.size) { 0 }

    init {
        for (edge in edges) {
            degrees[edge.source.id]++
            if (edge.source.id != edge.target.id) degrees[edge.target.id]++
        }
    }

    fun getDegree(node: Node) = degrees[node.id]

    fun getNodeCount() = nodes.size
}

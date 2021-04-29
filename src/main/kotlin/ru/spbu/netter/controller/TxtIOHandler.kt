package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import tornadofx.*
import java.io.File
import java.io.IOException


class TxtIOHandler : Controller(), FileIOHandler {
    companion object {
        private const val COLUMN_DELIMITER = " "
        private const val EDGE_INPUT_COLUMNS_NUM = 2
        private const val VERTEX_INPUT_COLUMNS_NUM = 3
    }


    override fun importNetwork(graph: Graph, filePath: String) {
        var lineNum = 0

        File(filePath).bufferedReader().useLines { lines ->
            val iterator = lines.iterator()

            listOf(::parseEdge, ::parseVertex).forEach { parseColumns ->
                while (iterator.hasNext()) {
                    val line = iterator.next()
                    lineNum++

                    if (line.isEmpty()) break

                    val columns = line.split(COLUMN_DELIMITER).toMutableList()
                    parseColumns(graph, columns, lineNum).let {
                        if (columns.size != it) throw IOException("Incorrect number of columns on line $lineNum: expected $it but was ${columns.size}")
                    }

                    addSkippedVertices(graph, columns[0].toInt() - 1)
                }
            }

            if (iterator.hasNext()) println("Excessive lines found after blank line $lineNum. Skipping...")
        }
    }

    private fun parseEdge(graph: Graph, columns: MutableList<String>, lineNum: Int): Int {
        if (columns.size == EDGE_INPUT_COLUMNS_NUM) {
            if (!columns[0].isInt() || columns[0].toInt() < FileIOHandler.MIN_VERTEX_ID ||
                !columns[1].isInt() || columns[1].toInt() < FileIOHandler.MIN_VERTEX_ID
            ) throw IOException("Incorrect input format on line $lineNum: both vertex ids must be non-negative integers")

            graph.addEdge(columns[0].toInt(), columns[1].toInt())

            if (columns[0] > columns[1]) columns[0] = columns[1].also { columns[1] = columns[0] }
            addSkippedVertices(graph, columns[1].toInt())
        }
        return EDGE_INPUT_COLUMNS_NUM
    }

    private fun parseVertex(graph: Graph, columns: List<String>, lineNum: Int): Int {
        if (columns.size == VERTEX_INPUT_COLUMNS_NUM) {
            if (!columns[0].isInt() || columns[0].toInt() < FileIOHandler.MIN_VERTEX_ID ||
                !columns[1].isInt() || columns[1].toInt() < FileIOHandler.MIN_COMMUNITY_ID ||
                !columns[2].isDouble() || columns[2].toDouble() < FileIOHandler.MIN_CENTRALITY
            ) throw IOException("Incorrect input format on line $lineNum: vertex and community ids must be non-negative integers, centrality must be a non-negative decimal")

            graph.addVertex(columns[0].toInt()).apply {
                community = columns[1].toInt()
                centrality = columns[2].toDouble()
            }
        }
        return VERTEX_INPUT_COLUMNS_NUM
    }

    private fun addSkippedVertices(graph: Graph, fromId: Int) {
        var prevId = fromId
        while (prevId >= FileIOHandler.MIN_VERTEX_ID && !graph.vertices.containsKey(prevId)) graph.addVertex(prevId--)
    }


    override fun exportNetwork(graph: Graph, filePath: String) {
        File(filePath).bufferedWriter().use { writer ->
            for (edge in graph.edges) writer.write("${edge.v1}$COLUMN_DELIMITER${edge.v2}\n")

            writer.newLine()

            for (entry in graph.vertices) with(entry.value) {
                writer.write("$id$COLUMN_DELIMITER$community$COLUMN_DELIMITER$centrality")
            }
        }
    }
}

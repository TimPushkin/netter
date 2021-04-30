package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import tornadofx.*
import java.io.File
import java.io.IOException
import java.io.FileNotFoundException


class TxtIOHandler : Controller(), FileIOHandler {
    companion object {
        private const val COLUMN_DELIMITER = " "
        private const val EDGE_INPUT_COLUMNS_NUM = 2
        private const val VERTEX_INPUT_COLUMNS_NUM = 3
    }


    override fun importNetwork(graph: Graph, filePath: String) {
        val parsingMethods = listOf(::parseEdge, ::parseVertex)
        var lineNum = 0

        val bufferedReader = try {
            File(filePath).bufferedReader()
        } catch (exception: FileNotFoundException) {
            throw FileNotFoundException("File cannot be read: ${exception.localizedMessage}")
        }

        bufferedReader.use { reader ->
            parsingMethods.forEach { parseColumns ->
                while (reader.ready()) {
                    val line = reader.readLine()
                    lineNum++

                    if (line.isEmpty()) break

                    with(line.split(COLUMN_DELIMITER)) {
                        parseColumns(graph, this, lineNum)
                        addSkippedVertices(graph, first().toInt())
                    }
                }
            }

            if (reader.ready()) println("Excessive lines found after blank line $lineNum. Skipping...")
        }
    }

    private fun parseEdge(graph: Graph, columns: List<String>, lineNum: Int) {
        if (columns.size != EDGE_INPUT_COLUMNS_NUM) {
            handleError(lineNum, "expected $EDGE_INPUT_COLUMNS_NUM columns but was ${columns.size}")
        }

        if (!columns[0].isInt() || !columns[1].isInt()) {
            handleError(lineNum, "vertex ids must be integers")
        }

        val parsedId1 = columns[0].toInt()
        val parsedId2 = columns[1].toInt()

        if (parsedId1 < FileIOHandler.MIN_VERTEX_ID || parsedId2 < FileIOHandler.MIN_VERTEX_ID) {
            handleError(lineNum, "vertex ids must be not less than ${FileIOHandler.MIN_VERTEX_ID}")
        }

        graph.addEdge(parsedId1, parsedId2)

        addSkippedVertices(graph, parsedId2)
    }

    private fun parseVertex(graph: Graph, columns: List<String>, lineNum: Int) {
        if (columns.size != VERTEX_INPUT_COLUMNS_NUM) {
            handleError(lineNum, "expected $VERTEX_INPUT_COLUMNS_NUM columns but was ${columns.size}")
        }

        if (!columns[0].isInt() || !columns[1].isInt() || !columns[2].isDouble()) {
            handleError(lineNum, "vertex id and community must be integers, centrality must be decimal")
        }

        val parsedId = columns[0].toInt().also {
            if (it < FileIOHandler.MIN_VERTEX_ID) {
                handleError(lineNum, "vertex id must be not less than ${FileIOHandler.MIN_VERTEX_ID}")
            }
        }
        val parsedCommunity = columns[1].toInt().also {
            if (it < FileIOHandler.MIN_COMMUNITY) {
                handleError(lineNum, "community must be not less than ${FileIOHandler.MIN_COMMUNITY}")
            }
        }
        val parsedCentrality = columns[2].toDouble().also {
            if (it < FileIOHandler.MIN_CENTRALITY) {
                handleError(lineNum, "centrality must be not less than ${FileIOHandler.MIN_CENTRALITY}")
            }
        }

        graph.addVertex(parsedId).apply {
            community = parsedCommunity
            centrality = parsedCentrality
        }
    }

    private fun addSkippedVertices(graph: Graph, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= FileIOHandler.MIN_VERTEX_ID && !graph.vertices.containsKey(prevId)) graph.addVertex(prevId--)
    }


    override fun exportNetwork(graph: Graph, filePath: String) {
        val bufferedWriter = try {
            File(filePath).bufferedWriter()
        } catch (exception: IOException) {
            throw IOException("File cannot be written: ${exception.localizedMessage}")
        }

        bufferedWriter.use { writer ->
            for (edge in graph.edges) writer.write("${edge.v1}$COLUMN_DELIMITER${edge.v2}\n")

            writer.newLine()

            for (entry in graph.vertices) with(entry.value) {
                writer.write("$id$COLUMN_DELIMITER$community$COLUMN_DELIMITER$centrality")
            }
        }
    }


    private fun handleError(lineNum: Int, message: String): Nothing {
        throw IOException("Incorrect input format on line $lineNum: $message")
    }
}

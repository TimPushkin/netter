package ru.spbu.netter.controller

import ru.spbu.netter.model.Network
import tornadofx.*
import java.io.File
import java.io.IOException
import java.io.FileNotFoundException
import java.nio.file.Files


class TxtIOHandler : Controller(), FileIOHandler {
    companion object {
        private const val COLUMN_DELIMITER = " "
        private const val LINK_INPUT_COLUMNS_NUM = 2
        private const val NODE_INPUT_COLUMNS_NUM = 3
    }

    override fun importNetwork(network: Network, file: File) {
        val parsingMethods = listOf(::parseLink, ::parseNode)
        var lineNum = 0

        val bufferedReader = try {
            file.bufferedReader()
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
                        parseColumns(network, this, lineNum)
                        addSkippedNodes(network, first().toInt())
                    }
                }
            }

            if (reader.ready()) println("Excessive lines found after blank line $lineNum. Skipping...")
        }
    }

    private fun parseLink(network: Network, columns: List<String>, lineNum: Int) {
        if (columns.size != LINK_INPUT_COLUMNS_NUM) {
            handleInputError(lineNum, "expected $LINK_INPUT_COLUMNS_NUM columns but was ${columns.size}")
        }

        if (!columns[0].isInt() || !columns[1].isInt()) {
            handleInputError(lineNum, "node ids must be integers")
        }

        val parsedId1 = columns[0].toInt()
        val parsedId2 = columns[1].toInt()

        if (parsedId1 < FileIOHandler.MIN_NODE_ID || parsedId2 < FileIOHandler.MIN_NODE_ID) {
            handleInputError(lineNum, "node ids must be not less than ${FileIOHandler.MIN_NODE_ID}")
        }

        network.addLink(parsedId1, parsedId2)

        addSkippedNodes(network, parsedId2)
    }

    private fun parseNode(network: Network, columns: List<String>, lineNum: Int) {
        if (columns.size != NODE_INPUT_COLUMNS_NUM) {
            handleInputError(lineNum, "expected $NODE_INPUT_COLUMNS_NUM columns but was ${columns.size}")
        }

        if (!columns[0].isInt() || !columns[1].isInt() || !columns[2].isDouble()) {
            handleInputError(lineNum, "node id and community must be integers, centrality must be decimal")
        }

        val parsedId = columns[0].toInt().also {
            if (it < FileIOHandler.MIN_NODE_ID) {
                handleInputError(lineNum, "node id must be not less than ${FileIOHandler.MIN_NODE_ID}")
            }
        }
        val parsedCommunity = columns[1].toInt().also {
            if (it < FileIOHandler.MIN_COMMUNITY) {
                handleInputError(lineNum, "community must be not less than ${FileIOHandler.MIN_COMMUNITY}")
            }
        }
        val parsedCentrality = columns[2].toDouble().also {
            if (it < FileIOHandler.MIN_CENTRALITY) {
                handleInputError(lineNum, "centrality must be not less than ${FileIOHandler.MIN_CENTRALITY}")
            }
        }

        network.addNode(parsedId).apply {
            community = parsedCommunity
            centrality = parsedCentrality
        }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= FileIOHandler.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    private fun handleInputError(lineNum: Int, message: String): Nothing {
        throw IOException("Incorrect input format on line $lineNum: $message")
    }

    override fun exportNetwork(network: Network, file: File) {
        try {
            Files.createDirectories(file.toPath().parent)
        } catch (exception: Exception) {
            throw IOException("Output file's parent dir cannot be created: ${exception.localizedMessage}")
        }

        val bufferedWriter = try {
            file.bufferedWriter()
        } catch (exception: IOException) {
            throw IOException("Output file cannot be written: ${exception.localizedMessage}")
        }

        bufferedWriter.use { writer ->
            for (link in network.links) with(link) {
                writer.write("${n1.id}$COLUMN_DELIMITER${n2.id}\n")
            }

            writer.newLine()

            for (entry in network.nodes) with(entry.value) {
                writer.write("$id$COLUMN_DELIMITER$community$COLUMN_DELIMITER$centrality\n")
            }
        }
    }
}

package ru.spbu.netter.controller.io

import mu.KotlinLogging
import ru.spbu.netter.model.Network
import tornadofx.*
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files


private val logger = KotlinLogging.logger {}

private const val COLUMN_DELIMITER = " "
private const val LINK_INPUT_COLUMNS_NUM = 2
private const val NODE_INPUT_COLUMNS_NUM = 3


class TxtIOHandler : Controller(), FileIOHandler {

    override fun importNetwork(network: Network, file: File) {
        val parsingMethods = listOf(::parseLink, ::parseNode)
        var lineNum = 0

        val bufferedReader = try {
            file.bufferedReader()
        } catch (ex: FileNotFoundException) {
            throw HandledIOException("Input file not found", ex)
        } catch (ex: SecurityException) {
            throw HandledIOException("Input file cannot be read: no read access", ex)
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

            if (reader.ready()) logger.warn { "Excessive lines found after blank line $lineNum. Skipping..." }
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

        if (parsedId1 < IOHandlerData.MIN_NODE_ID || parsedId2 < IOHandlerData.MIN_NODE_ID) {
            handleInputError(lineNum, "node ids must be not less than ${IOHandlerData.MIN_NODE_ID}")
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

        val parsedId = columns[0].toInt()
        val parsedCommunity = columns[1].toInt()
        val parsedCentrality = columns[2].toDouble()

        if (parsedId  < IOHandlerData.MIN_NODE_ID) {
            handleInputError(lineNum, "node id must be not less than ${IOHandlerData.MIN_NODE_ID}")
        }

        if (parsedCommunity < IOHandlerData.MIN_COMMUNITY) {
            handleInputError(lineNum, "community must be not less than ${IOHandlerData.MIN_COMMUNITY}")
        }

        if (parsedCentrality < IOHandlerData.MIN_CENTRALITY) {
            handleInputError(lineNum, "centrality must be not less than ${IOHandlerData.MIN_CENTRALITY}")
        }

        network.addNode(parsedId).apply {
            community = parsedCommunity
            centrality = parsedCentrality
        }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandlerData.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    private fun handleInputError(lineNum: Int, message: String): Nothing {
        throw HandledIOException("Incorrect input format on line $lineNum: $message")
    }

    override fun exportNetwork(network: Network, file: File) {
        try {
            Files.createDirectories(file.toPath().parent)
        } catch (ex: SecurityException) {
            throw HandledIOException("Parent dir ${file.toPath().parent} cannot be created: no read-write access", ex)
        }

        val bufferedWriter = try {
            file.bufferedWriter()
        } catch (ex: FileNotFoundException) {
            throw HandledIOException("Output file cannot be opened or created", ex)
        } catch (ex: SecurityException) {
            throw HandledIOException("Output file cannot be written: no write access", ex)
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

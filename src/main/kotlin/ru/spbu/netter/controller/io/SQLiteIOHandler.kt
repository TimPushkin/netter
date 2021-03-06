package ru.spbu.netter.controller.io

import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import tornadofx.Controller
import java.io.File
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import ru.spbu.netter.model.Network
import java.lang.NumberFormatException


private val logger = KotlinLogging.logger {}

private const val LINKS_TABLE_NAME = "links"
private const val NODES_TABLE_NAME = "nodes"


class SQLiteIOHandler : Controller(), FileIOHandler {

    override fun importNetwork(network: Network, file: File) {
        logger.info { "Import has started" }

        Database.connect("jdbc:sqlite:${file.path}", "org.sqlite.JDBC")

        var linksAreProvided = false
        var nodesAreProvided = false

        try {
            transaction {
                addLogger(StdOutSqlLogger)
                for (tblName in SqliteMaster.selectAll()) {
                    if (tblName[SqliteMaster.tblName] == LINKS_TABLE_NAME) linksAreProvided = true
                    if (tblName[SqliteMaster.tblName] == NODES_TABLE_NAME) nodesAreProvided = true
                }
            }
        } catch (ex: ExposedSQLException) {
            throw HandledIOException("File is not a database", ex)
        }

        if (!linksAreProvided && !nodesAreProvided) {
            throw HandledIOException("Could not find tables by these names: $LINKS_TABLE_NAME, $NODES_TABLE_NAME")
        }

        if (linksAreProvided) parseLinks(network)
        if (nodesAreProvided) parseNodes(network)

        if (network.isEmpty()) {
            logger.info { "The provided network is empty" }
            throw HandledIOException("The provided network is empty")
        }

        logger.info { "Import has been finished" }
    }

    override fun exportNetwork(network: Network, file: File) {
        try {
            Files.createDirectories(file.toPath().parent)
        } catch (ex: SecurityException) {
            throw HandledIOException("Parent dir ${file.toPath().parent} cannot be created: no read-write access", ex)
        }

        Database.connect("jdbc:sqlite:${file.path}", "org.sqlite.JDBC")

        transaction {
            try {
                SchemaUtils.drop(Nodes, Links)
                SchemaUtils.create(Nodes, Links)
            } catch (ex: ExposedSQLException) {
                throw HandledIOException("File is not a database", ex)
            }

            logger.info { "Empty tables were created" }

            for (link in network.links) {
                Links.insert {
                    it[id1] = link.n1.id
                    it[id2] = link.n2.id
                }
            }

            logger.info { "Links have been exported" }

            for (node in network.nodes) {
                Nodes.insert {
                    it[id] = node.value.id
                    it[community] = node.value.community
                    it[centrality] = node.value.centrality
                    it[x] = node.value.x
                    it[y] = node.value.y
                }
            }

            logger.info { "Nodes have been exported" }
        }

        logger.info { "Export has been finished" }
    }

    private fun parseNodes(network: Network) {
        logger.info { "Parsing nodes..." }

        transaction {
            addLogger(StdOutSqlLogger)
            for (node in Nodes.selectAll()) {
                try {
                    val parsedId = node[Nodes.id]
                    val parsedCommunity = node[Nodes.community]
                    val parsedCentrality: Double = node[Nodes.centrality]
                    val parsedX: Double? = node[Nodes.x]
                    val parsedY: Double? = node[Nodes.y]

                    if (parsedId < IOHandlerData.MIN_NODE_ID) {
                        throw HandledIOException("node id must be not less than ${IOHandlerData.MIN_NODE_ID}")
                    }
                    if (parsedCommunity < IOHandlerData.MIN_COMMUNITY) {
                        throw HandledIOException("community must be not less than ${IOHandlerData.MIN_COMMUNITY}")
                    }
                    if (parsedCentrality < IOHandlerData.MIN_CENTRALITY) {
                        throw HandledIOException("centrality must be not less than ${IOHandlerData.MIN_CENTRALITY}")
                    }

                    network.addNode(parsedId).apply {
                        community = parsedCommunity
                        centrality = parsedCentrality
                        x = parsedX ?: x
                        y = parsedY ?: y
                    }

                    addSkippedNodes(network, parsedId)
                } catch (ex: NumberFormatException) {
                    throw HandledIOException(
                        "Node ids and community must be integers, centrality and coordinates must be doubles",
                        ex
                    )
                }
            }
        }

        logger.info { "Nodes have been parsed" }
    }

    private fun parseLinks(network: Network) {
        logger.info { "Parsing links..." }

        transaction {
            addLogger(StdOutSqlLogger)
            for (link in Links.selectAll()) {
                try {
                    val parsedId1 = link[Links.id1]
                    val parsedId2 = link[Links.id2]

                    if (parsedId1 < IOHandlerData.MIN_NODE_ID || parsedId2 < IOHandlerData.MIN_NODE_ID) {
                        throw  HandledIOException("Node ids must be not less than ${IOHandlerData.MIN_NODE_ID}")
                    }

                    network.addLink(parsedId1, parsedId2)

                    addSkippedNodes(network, parsedId1)
                    addSkippedNodes(network, parsedId2)
                } catch (ex: NumberFormatException) {
                    throw HandledIOException("Node ids must be integers", ex)
                }
            }
        }

        logger.info { "Links have been imported" }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandlerData.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    object SqliteMaster : Table("sqlite_master") {
        val name = varchar("name", 50)
        val tblName = varchar("tbl_name", 50)
    }

    object Nodes : Table(NODES_TABLE_NAME) {
        val id = integer("id")
        val community = integer("community")
        val centrality = double("centrality")
        val x = double("x").nullable()
        val y = double("y").nullable()
    }

    object Links : Table(LINKS_TABLE_NAME) {
        val id1 = integer("id1")
        val id2 = integer("id2")
    }
}

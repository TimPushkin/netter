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


private const val LINKS_TABLE_NAME = "links"
private const val NODES_TABLE_NAME = "nodes"

private val logger = KotlinLogging.logger {}


class SQLiteIOHandler : Controller(), FileIOHandler {


    override fun importNetwork(network: Network, file: File) {
        logger.info { "imports have started" }
        Database.connect("jdbc:sqlite:${file.path}", "org.sqlite.JDBC")
        var checkLinks = false
        var checkNodes = false
        var emptyIndicator = 0L
        try {
            transaction {
                addLogger(StdOutSqlLogger)
                for (tblName in SqliteMaster.selectAll()) {
                    if (tblName[SqliteMaster.tbl_name] == LINKS_TABLE_NAME) {
                        checkLinks = true
                        emptyIndicator += Links.selectAll().count()
                    }
                    if (tblName[SqliteMaster.tbl_name] == NODES_TABLE_NAME) {
                        checkNodes = true
                        emptyIndicator += Nodes.selectAll().count()
                    }
                }
            }
        } catch (ex: ExposedSQLException) {
            throw HandledIOException("File is not a database", ex)
        }
        if (!checkLinks && !checkNodes)
            throw HandledIOException("Could not find tables by these names: $LINKS_TABLE_NAME, $NODES_TABLE_NAME")
        if (emptyIndicator == 0L) {
            logger.info { "The provided network is empty" }
            throw HandledIOException("The provided network is empty")
        }

        if (checkLinks) parseLinks(network)
        if (checkNodes) parseNodes(network)
        logger.info { "Import finished" }
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

            logger.info { "Links exported" }

            for (node in network.nodes) {
                Nodes.insert {
                    it[id] = node.value.id
                    it[community] = node.value.community
                    it[centrality] = node.value.centrality
                    it[x] = node.value.x
                    it[y] = node.value.y
                }
            }
            logger.info { "Nodes exported" }
        }
        logger.info { "Export finished" }
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
                        throw  HandledIOException("community must be not less than ${IOHandlerData.MIN_COMMUNITY}")
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
                        "Node ids and community must be integers, centrality and coordinates must be double",
                        ex
                    )
                }
            }
        }
        logger.info { "Nodes parsed" }
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

                    addSkippedNodes(network, parsedId2)
                    addSkippedNodes(network, parsedId1)
                } catch (ex: NumberFormatException) {
                    throw HandledIOException("Node ids must be integers", ex)
                }
            }
        }
        logger.info { "links imported" }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandlerData.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    object SqliteMaster : Table("sqlite_master") {
        val name = varchar("name", 50)
        val tbl_name = varchar("tbl_name", 50)
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

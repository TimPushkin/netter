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

    object sqlite_master : Table("Sqlite_master") {
        val type = varchar("type", 50)
        val name = varchar("name", 50)
        val tbl_name = varchar("tbl_name", 50)
    }

    object nodes : Table(NODES_TABLE_NAME) {
        val id = integer("id")
        val community = integer("community")
        val centrality = double("centrality")
        val x = double("x").nullable()
        val y = double("y").nullable()
    }

    object links : Table(LINKS_TABLE_NAME) {

        val id1 = integer("id1")
        val id2 = integer("id2")
    }

    override fun importNetwork(network: Network, file: File) {
        Database.connect("jdbc:sqlite:${file.path}", "org.sqlite.JDBC")
        var checkLinks = false
        var checkNodes = false

        transaction {
            addLogger(StdOutSqlLogger)
            for (tblName in sqlite_master.selectAll()) {
                if (tblName[sqlite_master.tbl_name] == LINKS_TABLE_NAME) checkLinks = true
                if (tblName[sqlite_master.tbl_name] == NODES_TABLE_NAME) checkNodes = true
            }
        }
        if (!checkLinks && !checkNodes)
            throw HandledIOException("Could not find tables by these names")
        if (checkLinks)
            parsLinks(network)
        if (checkNodes) {
            parseNodes(network)
        }
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
                SchemaUtils.drop(nodes, links)
                SchemaUtils.create(nodes, links)
            } catch (ex: ExposedSQLException) {
                throw HandledIOException("File is not a database", ex)
            }
            for (link in network.links) {
                links.insert {
                    it[id1] = link.n1.id
                    it[id2] = link.n2.id
                }
            }
            for (node in network.nodes) {
                nodes.insert {
                    it[id] = node.value.id
                    it[community] = node.value.community
                    it[centrality] = node.value.centrality
                    it[x] = node.value.x
                    it[y] = node.value.y

                }
            }
        }
    }

    private fun parseNodes(network: Network) {
        transaction {
            addLogger(StdOutSqlLogger)
            for (node in nodes.selectAll()) {
                try {
                    val parsedId = node[nodes.id]
                    val parsedCommunity = node[nodes.community]
                    val parsedCentrality: Double = node[nodes.centrality]
                    val parsedX: Double? = node[nodes.x]
                    val parsedY: Double? = node[nodes.y]

                    if (parsedId  < IOHandlerData.MIN_NODE_ID)
                        throw HandledIOException("node id must be not less than ${IOHandlerData.MIN_NODE_ID}")
                    if (parsedCommunity < IOHandlerData.MIN_COMMUNITY)
                        throw  HandledIOException("community must be not less than ${IOHandlerData.MIN_COMMUNITY}")
                    if (parsedCentrality < IOHandlerData.MIN_CENTRALITY) {
                        throw HandledIOException("centrality must be not less than ${IOHandlerData.MIN_CENTRALITY}")
                    }
                    if (parsedX == null || parsedY == null){
                        network.addNode(parsedId).apply {
                            community = parsedCommunity
                            centrality = parsedCentrality
                        }
                    }else{
                        network.addNode(parsedId).apply {
                            community = parsedCommunity
                            centrality = parsedCentrality
                            x = parsedX
                            y = parsedY
                        }
                    }
                    addSkippedNodes(network, parsedId)
                } catch (ex: NumberFormatException) {
                    throw HandledIOException("Node ids and community must be integers, centrality and coordinates must be double", ex)
                }
            }
        }

    }

    private fun parsLinks(network: Network) {
        transaction {
            addLogger(StdOutSqlLogger)
            for (link in links.selectAll()) {
                try {
                    val parsedId1 = link[links.id1]
                    val parsedId2 = link[links.id2]
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
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandlerData.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }
}


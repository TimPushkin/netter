package ru.spbu.netter.controller.io

import mu.KotlinLogging
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Result
import org.neo4j.driver.exceptions.*
import org.neo4j.driver.exceptions.value.Uncoercible
import ru.spbu.netter.model.Network
import tornadofx.Controller
import java.io.Closeable
import kotlin.math.max


private val logger = KotlinLogging.logger {}


class Neo4jIOHandler : Controller(), UriIOHandler, Closeable {
    private lateinit var driver: Driver

    override fun importNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        var nodes: Result
        var links: Result

        try {
            session.readTransaction { tx ->
                nodes = tx.run(
                    "MATCH (n:Node) " +
                            "RETURN n.id AS id, n.community AS community, n.centrality AS centrality"
                )

                parseNode(network, nodes)
            }

            logger.info { "Nodes were successfully read" }


            session.readTransaction { tx ->
                links = tx.run(
                    "MATCH (n1)-->(n2) " +
                            "RETURN n1.id AS id1, n2.id AS id2"
                )

                parseLink(network, links)
            }

            logger.info { "Links were successfully read" }

        } catch (ex: Neo4jException) {
            logger.error(ex) { "Unable to read network" }

            throw when (ex) {
                is AuthenticationException -> HandledIOException("Wrong username or password", ex)
                is ClientException -> HandledIOException(
                    "Make sure you are trying to connect to the bolt:// URI scheme",
                    ex
                )
                is ServiceUnavailableException -> HandledIOException(
                    "Unable to connect, ensure the database is running and that there is a working network connection to it.",
                    ex
                )
                else -> ex
            }
        }

        logger.info { "Network was successfully read" }
        close()
    }

    override fun exportNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        try {
            session.writeTransaction { tx ->

                tx.run("MATCH (n) DETACH DELETE n ")

                logger.info { "Database was successfully clean" }

                for (entry in network.nodes) with(entry.value) {
                    tx.run(
                        "CREATE (n:Node{id:\$id, community:\$community, centrality:\$centrality})",
                        mutableMapOf(
                            "id" to id,
                            "community" to community,
                            "centrality" to centrality
                        ) as Map<String, Any>?
                    )
                }

                logger.info { "Nodes were successfully record" }

                for (link in network.links) with(link) {
                    tx.run(
                        "MATCH (n1:Node{id:\$id1})  " +
                                "MATCH (n2:Node{id:\$id2}) " +
                                "CREATE (n1)-[:LINK]->(n2)",
                        mutableMapOf(
                            "id1" to n1.id,
                            "id2" to n2.id
                        ) as Map<String, Any>?
                    )
                }

                logger.info { "Links were successfully record" }
            }
        } catch (ex: Neo4jException) {
            logger.error(ex) { "Unable to record network" }

            throw when (ex) {
                is AuthenticationException -> HandledIOException("Wrong username or password", ex)
                is ClientException -> HandledIOException("Make sure you are trying to connect to the bolt:// URI scheme", ex)
                is ServiceUnavailableException -> HandledIOException(
                    "Unable to connect, ensure the database is running and that there is a working network connection to it.", ex
                )
                else -> ex
            }
        }
        logger.info { "Network was successfully record" }
        close()
    }

    private fun openDriver(uri: String, username: String, password: String) {
        try {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))

        } catch (ex: IllegalArgumentException) {
            logger.error(ex) { "Cannot connect to the server at $uri address" }
            throw HandledIOException("Wrong URI", ex)
        } catch (ex: SessionExpiredException) {
            logger.error(ex) { "Session failed" }
            throw HandledIOException("Session failed, try restarting the app", ex)
        }

        logger.info { "Connect to the server at $uri address" }
    }

    private fun parseNode(network: Network, nodes: Result) {
        try {
            nodes.forEach { node ->
                val parsedId = node["id"].asInt()
                val parsedCommunity = node["community"].asInt()
                val parsedCentrality = node["centrality"].asDouble()

                if (parsedId < IOHandler.MIN_NODE_ID) {
                    throw HandledIOException("id label must be not less than ${IOHandler.MIN_NODE_ID}")
                }

                if (parsedCommunity < IOHandler.MIN_COMMUNITY) {
                    throw HandledIOException("community label must be not less than ${IOHandler.MIN_COMMUNITY}")
                }

                if (parsedCentrality < IOHandler.MIN_CENTRALITY) {
                    throw HandledIOException("centrality label must be not less than ${IOHandler.MIN_CENTRALITY}")
                }

                network.addNode(parsedId).apply {
                    community = parsedCommunity
                    centrality = parsedCentrality
                }

                addSkippedNodes(network, parsedId)
            }

        } catch (ex: Uncoercible) {
            throw HandledIOException("Invalid label nodes in the database", ex)
        }
    }

    private fun parseLink(network: Network, links: Result) {
        try {
            links.forEach { link ->
                val parsedId1 = link["id1"].asInt()
                val parsedId2 = link["id2"].asInt()

                if (parsedId1 < IOHandler.MIN_NODE_ID || parsedId2 < IOHandler.MIN_NODE_ID) {
                    throw HandledIOException("Id labels must be not less than ${IOHandler.MIN_NODE_ID}")
                }

                addSkippedNodes(network, max(parsedId1, parsedId2))

                network.addLink(parsedId1, parsedId2)
            }
        } catch (ex: Uncoercible) {
            throw HandledIOException("Invalid label links in the database", ex)
        }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandler.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    override fun close() {
        driver.close()
        logger.info { "Disconnect from server" }
    }
}

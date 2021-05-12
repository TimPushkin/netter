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

        try {
            session.readTransaction { tx ->
                val nodes = tx.run(
                    "MATCH (n:NODE) " +
                            "RETURN n.id AS id, n.community AS community, n.centrality AS centrality"
                )
                parseNode(network, nodes)
            }
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Authentication error: wrong username or password." }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme." }
            throw HandledIOException("Use the bolt:// URI scheme", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it." }
            throw HandledIOException("Check your network connection", ex)
        }

        logger.info { "Nodes were successfully read" }

        try {
            session.readTransaction { tx ->
                val links = tx.run(
                    "MATCH (n1:NODE)-[:LINK]->(n2:NODE) " +
                            "RETURN n1.id AS id1, n2.id AS id2"
                )
                parseLink(network, links)
            }
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Authentication error: wrong username or password." }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme." }
            throw HandledIOException("Use the bolt:// URI scheme", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it." }
            throw HandledIOException("Check your network connection", ex)
        }

        logger.info { "Links were successfully read" }
        close()
    }

    override fun exportNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        try {
            session.writeTransaction { tx ->
                tx.run("MATCH (n: NODE), (:NODE)-[l:LINK]->(:NODE) DETACH DELETE n, l")
            }
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Authentication error: wrong username or password." }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme." }
            throw HandledIOException("Use the bolt:// URI scheme", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it." }
            throw HandledIOException("Check your network connection", ex)
        }

        logger.info { "Database was successfully cleaned" }

        try {
            session.writeTransaction { tx ->
                for (entry in network.nodes) with(entry.value) {
                    tx.run(
                        "CREATE (n:NODE{id:\$id, community:\$community, centrality:\$centrality})",
                        mutableMapOf(
                            "id" to id,
                            "community" to community,
                            "centrality" to centrality
                        ) as Map<String, Any>?
                    )
                }
            }
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Authentication error: wrong username or password." }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme." }
            throw HandledIOException("Use the bolt:// URI scheme", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it." }
            throw HandledIOException("Check your network connection", ex)
        }

        logger.info { "Nodes were successfully record" }

        try {
            session.writeTransaction { tx ->
                for (link in network.links) with(link) {
                    tx.run(
                        "MATCH (n1:NODE{id:\$id1})  " +
                                "MATCH (n2:NODE{id:\$id2}) " +
                                "CREATE (n1)-[:LINK]->(n2)",
                        mutableMapOf(
                            "id1" to n1.id,
                            "id2" to n2.id
                        ) as Map<String, Any>?
                    )
                }
            }
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Authentication error: wrong username or password." }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme." }
            throw HandledIOException("Use the bolt:// URI scheme", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it." }
            throw HandledIOException("Check your network connection", ex)
        }

        logger.info { "Links were successfully recorded" }
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

                if (parsedId < IOHandlerData.MIN_NODE_ID) {
                    throw HandledIOException("id label must be not less than ${IOHandlerData.MIN_NODE_ID}")
                }
                if (parsedCommunity < IOHandlerData.MIN_COMMUNITY) {
                    throw HandledIOException("community label must be not less than ${IOHandlerData.MIN_COMMUNITY}")
                }
                if (parsedCentrality < IOHandlerData.MIN_CENTRALITY) {
                    throw HandledIOException("centrality label must be not less than ${IOHandlerData.MIN_CENTRALITY}")
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

                if (parsedId1 < IOHandlerData.MIN_NODE_ID || parsedId2 < IOHandlerData.MIN_NODE_ID) {
                    throw HandledIOException("Id labels must be not less than ${IOHandlerData.MIN_NODE_ID}")
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
        while (prevId >= IOHandlerData.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    override fun close() {
        driver.close()
        logger.info { "Disconnect from server" }
    }
}

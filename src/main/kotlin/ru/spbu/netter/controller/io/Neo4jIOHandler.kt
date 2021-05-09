package ru.spbu.netter.controller.io

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Result
import org.neo4j.driver.exceptions.*
import ru.spbu.netter.model.Network
import tornadofx.Controller
import java.io.Closeable
import kotlin.math.max


class Neo4jIOHandler : Controller(), UriIOHandler, Closeable {
    private lateinit var driver: Driver

    override fun importNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        var nodes: Result
        var links: Result

        try {
            session.readTransaction { tx ->
                try {
                    nodes =
                        tx.run(
                            "MATCH (n:Node) " +
                                    "RETURN n.id AS id, n.community AS community, n.centrality AS centrality"
                        )
                    parseNode(network, nodes)
                } catch (exception: Exception) {
                    throw when (exception) {
                        is HandledIOException -> exception
                        else -> {
                            tx.rollback()
                            HandledIOException("Unable to read nodes")
                        }
                    }
                }
            }
            session.readTransaction { tx ->
                try {
                    links =
                        tx.run(
                            "MATCH (n1)-->(n2) " +
                                    "RETURN n1.id AS id1, n2.id AS id2"
                        )
                    parseLink(network, links)
                } catch (exception: Exception) {
                    throw when (exception) {
                        is HandledIOException -> exception
                        else -> {
                            tx.rollback()
                            HandledIOException("Unable to read links")
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            throw when (exception) {
                is HandledIOException -> exception
                is AuthenticationException -> HandledIOException("Wrong username or password")
                is ClientException -> HandledIOException("Make sure you are trying to connect to the bolt:// URI scheme")
                else -> HandledIOException("Server error: ${exception.localizedMessage}", exception)
            }

        }
    }

    override fun exportNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        try {
            session.writeTransaction { tx ->
                try {
                    tx.run("MATCH (n) DETACH DELETE n ")
                } catch (exception: Exception) {
                    tx.rollback()
                    throw HandledIOException("Unable to clean database")
                }

                for (entry in network.nodes) with(entry.value) {
                    try {
                        tx.run(
                            "CREATE (n:Node{id:\$id, community:\$community, centrality:\$centrality})",
                            mutableMapOf(
                                "id" to id,
                                "community" to community,
                                "centrality" to centrality
                            ) as Map<String, Any>?
                        )
                    } catch (exception: Exception) {
                        tx.rollback()
                        throw HandledIOException("Unable to record nodes")
                    }
                }

                for (link in network.links) with(link) {
                    try {
                        tx.run(
                            "MATCH (n1:Node{id:\$id1})  " +
                                    "MATCH (n2:Node{id:\$id2}) " +
                                    "CREATE (n1)-[:LINK]->(n2)",
                            mutableMapOf(
                                "id1" to n1.id,
                                "id2" to n2.id
                            ) as Map<String, Any>?
                        )
                    } catch (exception: Exception) {
                        tx.rollback()
                        throw HandledIOException("Unable to record links")
                    }
                }

                //throw HandledIOException("Network cannot be recorded: ${ex.localizedMessage}")

            }
        } catch (exception: Exception) {
            throw when (exception) {
                is HandledIOException -> exception
                is AuthenticationException -> HandledIOException("Wrong username or password")
                is ClientException -> HandledIOException("Make sure you are trying to connect to the bolt:// URI scheme")
                else -> HandledIOException("Network cannot be recorded: ${exception.localizedMessage}")
            }
        }
    }

    private fun openDriver(uri: String, username: String, password: String) {
        try {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))
        } catch (exception: Exception) {
            throw when (exception) {
                is java.lang.IllegalArgumentException -> HandledIOException("Wrong URI")
                is ServiceUnavailableException -> HandledIOException(
                    "Unable to connect, " +
                            "ensure the database is running and that there is a working network connection to it.",
                    exception
                )
                is SessionExpiredException -> HandledIOException("Session failed, try restarting the app")
                else -> HandledIOException("Connection failed: ${exception.localizedMessage}")
            }
        }
    }

    private fun parseNode(network: Network, nodes: Result) {
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
    }

    private fun parseLink(network: Network, links: Result) {
        links.forEach { link ->
            val parsedId1 = link["id1"].asInt()
            val parsedId2 = link["id2"].asInt()

            if (parsedId1 < IOHandler.MIN_NODE_ID || parsedId2 < IOHandler.MIN_NODE_ID) {
                throw HandledIOException("id labels must be not less than ${IOHandler.MIN_NODE_ID}")
            }

            addSkippedNodes(network, max(parsedId1, parsedId2))

            network.addLink(parsedId1, parsedId2)
        }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandler.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    override fun close() {
        try {
            driver.close()
        } catch (exception: Neo4jException) {
            throw HandledIOException("Unable to close connection")
        }
    }
}

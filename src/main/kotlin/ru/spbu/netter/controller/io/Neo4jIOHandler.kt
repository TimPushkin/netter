package ru.spbu.netter.controller.io

import mu.KotlinLogging
import org.neo4j.driver.*
import org.neo4j.driver.exceptions.*
import org.neo4j.driver.exceptions.value.Uncoercible
import ru.spbu.netter.model.Network
import ru.spbu.netter.model.Node
import tornadofx.Controller
import java.io.Closeable
import kotlin.math.max


private val logger = KotlinLogging.logger {}


class Neo4jIOHandler : Controller(), UriIOHandler, Closeable {
    private lateinit var driver: Driver

    override fun importNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        var nodes: Result? = null
        session.handleTransaction(Session::readTransaction) { tx ->
            nodes = tx.run(
                "MATCH (n:NODE) " +
                        "RETURN n.id AS id, n.community AS community, n.centrality AS centrality, n.x AS x, n.y AS y"
            )
        }
        nodes?.let { parseNodes(network, it) }
            ?: throw IllegalStateException("Unable to parse nodes: nodes reading has been failed")

        logger.info { "Nodes were successfully read" }

        var links: Result? = null
        session.handleTransaction(Session::readTransaction) { tx ->
            links = tx.run(
                "MATCH (n1:NODE)-[:LINK]->(n2:NODE) " +
                        "RETURN n1.id AS id1, n2.id AS id2"
            )
        }
        links?.let { parseLinks(network, it) }
            ?: throw IllegalStateException("Unable to parse links: links reading has been failed")

        logger.info { "Links were successfully read" }

        close()
    }

    override fun exportNetwork(network: Network, uri: String, username: String, password: String) {
        openDriver(uri, username, password)
        val session = driver.session()

        session.handleTransaction(Session::writeTransaction) { tx ->
            tx.run("MATCH (n:NODE), (:NODE)-[l:LINK]->(:NODE) DETACH DELETE n, l")
            logger.info { "Database was successfully cleaned" }

            for (entry in network.nodes) with(entry.value) {
                tx.run(
                    "CREATE (n:NODE{id:$id, community:$community, centrality:$centrality, x:$x, y:$y})"
                )
            }
            logger.info { "Nodes were successfully recorded" }

            for (link in network.links) with(link) {
                tx.run(
                    "MATCH (n1:NODE{id:${n1.id}})  " +
                            "MATCH (n2:NODE{id:${n2.id}}) " +
                            "CREATE (n1)-[:LINK]->(n2)"
                )
            }
            logger.info { "Links were successfully recorded" }
        }

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

    private fun <T> Session.handleTransaction(execute: Session.(TransactionWork<T>) -> T, work: TransactionWork<T>): T {
        try {
            return execute(work)
        } catch (ex: AuthenticationException) {
            logger.error(ex) { "Wrong username or password" }
            throw HandledIOException("Wrong username or password", ex)
        } catch (ex: ClientException) {
            logger.error(ex) { "Make sure you are trying to connect to the bolt:// URI scheme" }
            throw HandledIOException("Use the bolt:// URI scheme or some other expected labels", ex)
        } catch (ex: ServiceUnavailableException) {
            logger.error(ex) { "Unable to connect, ensure the database is running and that there is a working network connection to it" }
            throw HandledIOException("Check your network connection", ex)
        }
    }

    private fun parseNodes(network: Network, nodes: Result) {
        var parsedId = IOHandlerData.MIN_NODE_ID - 1
        var parsedCommunity = Node.DEFAULT_COMMUNITY
        var parsedCentrality = Node.DEFAULT_CENTRALITY
        var parsedX = Node.DEFAULT_X
        var parsedY = Node.DEFAULT_Y

        nodes.forEach { node ->
            try {
                parsedId = node["id"].asInt(parsedId)
                parsedCommunity = node["community"].asInt(parsedCommunity)
                parsedCentrality = node["centrality"].asDouble(parsedCentrality)
                parsedX = node["x"].asDouble(parsedX)
                parsedY = node["y"].asDouble(parsedY)
            } catch (ex: Uncoercible) {
                logger.error(ex) { "Node label type is incompatible" }
                throw HandledIOException("Invalid nodes label in the database", ex)
            }

            if (parsedId < IOHandlerData.MIN_NODE_ID) {
                throw HandledIOException("Node must have id label that is not less than ${IOHandlerData.MIN_NODE_ID}")
            }
            if (parsedCommunity < IOHandlerData.MIN_COMMUNITY) {
                throw HandledIOException("The community label must be not less than ${IOHandlerData.MIN_COMMUNITY}")
            }
            if (parsedCentrality < IOHandlerData.MIN_CENTRALITY) {
                throw HandledIOException("The centrality label must be not less than ${IOHandlerData.MIN_CENTRALITY}")
            }

            network.addNode(parsedId).apply {
                community = parsedCommunity
                centrality = parsedCentrality
                x = parsedX
                y = parsedY
            }
            addSkippedNodes(network, parsedId)
        }
    }

    private fun parseLinks(network: Network, links: Result) {
        var parsedId1 = IOHandlerData.MIN_NODE_ID - 1
        var parsedId2 = IOHandlerData.MIN_NODE_ID - 1

        links.forEach { link ->
            try {
                parsedId1 = link["id1"].asInt(parsedId1)
                parsedId2 = link["id2"].asInt(parsedId2)
            } catch (ex: Uncoercible) {
                logger.error(ex) { "Link label type is incompatible" }
                throw HandledIOException("Invalid links label in the database", ex)
            }

            if (parsedId1 < IOHandlerData.MIN_NODE_ID || parsedId2 < IOHandlerData.MIN_NODE_ID) {
                throw HandledIOException("Id labels must be not less than ${IOHandlerData.MIN_NODE_ID}")
            }

            addSkippedNodes(network, max(parsedId1, parsedId2))
            network.addLink(parsedId1, parsedId2)
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

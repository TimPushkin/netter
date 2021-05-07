package ru.spbu.netter.controller.io

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Result
import ru.spbu.netter.model.Network
import tornadofx.Controller
import java.io.IOException
import kotlin.math.max


class Neo4jIOHandler : Controller(), UriIOHandler {

    override fun importNetwork(network: Network, uri: String, user: String, password: String) {
        val driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))
        val session = driver.session()

        session.readTransaction { tx ->
            try {
                val nodes =
                    tx.run("MATCH (n:Node) " +
                            "RETURN n.id AS id, n.community AS community, n.centrality AS centrality")
                parseNode(network, nodes)

                val links =
                    tx.run("MATCH (n1)-->(n2) " +
                            "RETURN n1.id AS id1, n2.id AS id2")
                parseLink(network, links)
            } catch (ex: Exception) {
                //tx.rollback()
                throw IOException("Network cannot be read: ${ex.localizedMessage}")
            }
        }

        session.close()
        driver.close()
    }

    override fun exportNetwork(network: Network, uri: String, user: String, password: String) {
        val driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))
        val session = driver.session()

        session.writeTransaction { tx ->
            try {
                tx.run("MATCH (n) DETACH DELETE n ")

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
            } catch (ex: Exception) {
                tx.rollback()
                throw IOException("Network cannot be recorded: ${ex.localizedMessage}")
            }
        }

        session.close()
        driver.close()
    }

    private fun parseNode(network: Network, nodes: Result) {
        nodes.forEach { node ->
            val parsedId = node["id"].asInt()
            val parsedCommunity = node["community"].asInt()
            val parsedCentrality = node["centrality"].asDouble()

            if (parsedId < IOHandler.MIN_NODE_ID) {
                handleInputError("id label must be not less than ${IOHandler.MIN_NODE_ID}")
            }

            if (parsedCommunity < IOHandler.MIN_COMMUNITY) {
                handleInputError("community label must be not less than ${IOHandler.MIN_COMMUNITY}")
            }

            if (parsedCentrality < IOHandler.MIN_CENTRALITY) {
                handleInputError("centrality label must be not less than ${IOHandler.MIN_CENTRALITY}")
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
                handleInputError("id labels must be not less than ${IOHandler.MIN_NODE_ID}")
            }

            addSkippedNodes(network, max(parsedId1, parsedId2))

            network.addLink(parsedId1, parsedId2)
        }
    }

    private fun addSkippedNodes(network: Network, addUntilId: Int) {
        var prevId = addUntilId - 1
        while (prevId >= IOHandler.MIN_NODE_ID && !network.nodes.containsKey(prevId)) network.addNode(prevId--)
    }

    private fun handleInputError(message: String): Nothing {
        throw IOException("Incorrect input format: $message")
    }
}

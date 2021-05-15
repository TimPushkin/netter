package ru.spbu.netter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.spbu.netter.controller.centrality.*
import ru.spbu.netter.controller.clustering.*
import ru.spbu.netter.controller.io.*
import ru.spbu.netter.model.Network
import ru.spbu.netter.model.Node
import ru.spbu.netter.model.UndirectedNetwork
import java.io.File
import kotlin.streams.asStream


internal class TxtIOIntegrationTests {
    lateinit var network: Network

    private val txtIOHandler: FileIOHandler = TxtIOHandler()
    private val communityDetector: CommunityDetector = LeidenCommunityDetector()
    private val centralityIdentifier: CentralityIdentifier = HarmonicCentralityIdentifier()

    companion object {
        // Txt IO constants

        private const val COLUMN_DELIMITER = " "

        // Parameterized tests supplementary data

        private const val PARAMETERIZED_TEST_NAME = "{displayName} <-- {arguments}"

        private val OUTPUT_FILE = File("out/test/txt-integration-test.txt")
        private val INPUT_FILES = File("src/test/resources/txt-inputs/").listFiles() ?: emptyArray<File>()

        object InputsProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?) =
                INPUT_FILES.map { Arguments.of(it) }.asSequence().asStream()
        }
    }

    // Helper functions

    private fun Network.getNodesAsTriples() = nodes.map { it.value.run { Triple(id, community, centrality) } }

    private fun readNetwork(inputFile: File): Pair<MutableSet<Pair<Int, Int>>, MutableMap<Int, Triple<Int, Int, Double>>> {
        val links = mutableSetOf<Pair<Int, Int>>()
        val nodes = mutableMapOf<Int, Triple<Int, Int, Double>>()

        inputFile.bufferedReader().use { reader ->
            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                line.split(COLUMN_DELIMITER).run {
                    val id1 = get(0).toInt()
                    val id2 = get(1).toInt()

                    if (!links.contains(Pair(id2, id1))) links.add(Pair(id1, id2))

                    nodes[id1] = Triple(id1, Node.DEFAULT_COMMUNITY, Node.DEFAULT_CENTRALITY)
                    nodes[id2] = Triple(id2, Node.DEFAULT_COMMUNITY, Node.DEFAULT_CENTRALITY)
                }
            }

            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                line.split(COLUMN_DELIMITER).run {
                    val id = get(0).toInt()

                    nodes[id] = Triple(id, get(1).toInt(), get(2).toDouble())
                }
            }
        }

        for (i in 0 until (nodes.keys.maxOfOrNull { it } ?: 0)) {
            if (!nodes.contains(i)) nodes[i] = Triple(i, Node.DEFAULT_COMMUNITY, Node.DEFAULT_CENTRALITY)
        }

        return Pair(links, nodes)
    }

    private fun assertNetworkFileEquals(expected: File, actual: File) {
        val (expectedLinks, expectedNodes) = readNetwork(expected)
        val (actualLinks, actualNodes) = readNetwork(actual)

        assertIterableEquals(
            expectedLinks.sortedWith(compareBy({ it.first }, { it.second })),
            actualLinks.sortedWith(compareBy({ it.first }, { it.second })),
        )

        assertIterableEquals(
            expectedNodes.values.sortedBy { it.first },
            actualNodes.values.sortedBy { it.first },
        )
    }

    // Tests

    @BeforeEach
    fun setUp() {
        network = UndirectedNetwork()
    }


    @Nested
    inner class ImportExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import and export - output file contains imported data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)
            txtIOHandler.exportNetwork(network, OUTPUT_FILE)

            assertNetworkFileEquals(inputFile, OUTPUT_FILE)
        }
    }

    @Nested
    inner class ImportInspectExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect for communities, export - output file contains correct inspection data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            communityDetector.detectCommunities(network)
            val expected = network.getNodesAsTriples()

            txtIOHandler.exportNetwork(network, OUTPUT_FILE)
            val actual = readNetwork(OUTPUT_FILE).run { second.values }

            assertIterableEquals(expected.sortedBy { it.first }, actual.sortedBy { it.first })
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect for centrality, export - output file contains correct inspection data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            centralityIdentifier.identifyCentrality(network)
            val expected = network.getNodesAsTriples()

            txtIOHandler.exportNetwork(network, OUTPUT_FILE)
            val actual = readNetwork(OUTPUT_FILE).run { second.values }

            assertIterableEquals(expected.sortedBy { it.first }, actual.sortedBy { it.first })
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect in combination, export - output file contains correct inspection data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            communityDetector.detectCommunities(network)
            centralityIdentifier.identifyCentrality(network)
            val expected = network.getNodesAsTriples()

            txtIOHandler.exportNetwork(network, OUTPUT_FILE)
            val actual = readNetwork(OUTPUT_FILE).run { second.values }

            assertIterableEquals(expected.sortedBy { it.first }, actual.sortedBy { it.first })
        }
    }
}

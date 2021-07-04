package ru.spbu.netter

import javafx.application.Platform
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.spbu.netter.controller.centrality.*
import ru.spbu.netter.controller.clustering.*
import ru.spbu.netter.controller.io.*
import ru.spbu.netter.controller.layout.*
import ru.spbu.netter.model.*
import java.io.File
import kotlin.reflect.KCallable
import kotlin.streams.asStream


@TestInstance(Lifecycle.PER_CLASS)
internal class TxtIOIntegrationTests {
    lateinit var network: Network

    private val txtIOHandler: FileIOHandler = TxtIOHandler()

    private val simpleLayout = CircularLayout()
    private val smartLayout = ForceAtlas2Layout()
    private val communityDetector = LeidenCommunityDetector()
    private val centralityIdentifier = HarmonicCentralityIdentifier()

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

    // Helper classes and functions

    private data class SimpleNode(
        val id: Int,
        val community: Int = Node.DEFAULT_COMMUNITY,
        val centrality: Double = Node.DEFAULT_CENTRALITY,
        val x: Double = Node.DEFAULT_X,
        val y: Double = Node.DEFAULT_Y,
    )

    private fun Network.getSimpleNodes() = nodes.map { it.value.run { SimpleNode(id, community, centrality, x, y) } }

    private fun Network.getLinksAsPairs() = links.map { Pair(it.n1.id, it.n2.id) }

    private fun readNetwork(file: File): Pair<MutableMap<Int, SimpleNode>, MutableSet<Pair<Int, Int>>> {
        val nodes = mutableMapOf<Int, SimpleNode>()
        val links = mutableSetOf<Pair<Int, Int>>()

        file.bufferedReader().use { reader ->
            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                with(line.split(COLUMN_DELIMITER)) {
                    val id1 = get(0).toInt()
                    val id2 = get(1).toInt()

                    if (!links.contains(Pair(id2, id1))) links.add(Pair(id1, id2))

                    nodes[id1] = SimpleNode(id1)
                    nodes[id2] = SimpleNode(id2)
                }
            }

            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                with(line.split(COLUMN_DELIMITER)) {
                    val id = get(0).toInt()

                    nodes[id] = SimpleNode(
                        id,
                        get(1).toInt(),
                        get(2).toDouble(),
                        getOrNull(3)?.toDouble() ?: Node.DEFAULT_X,
                        getOrNull(4)?.toDouble() ?: Node.DEFAULT_Y,
                    )
                }
            }
        }

        for (i in 0 until (nodes.keys.maxOfOrNull { it } ?: 0)) if (!nodes.contains(i)) nodes[i] = SimpleNode(i)

        return Pair(nodes, links)
    }

    private fun assertCorrectExportWithActions(network: Network, vararg actions: KCallable<Unit>) {
        val flags = MutableList(actions.size) { false }
        actions.forEachIndexed { i, action ->
            with(action) { callBy(mapOf(parameters.first() to network, parameters.last() to { flags[i] = true })) }
        }
        await until { flags.all { it } }

        val expectedNodes = network.getSimpleNodes()
        val expectedLinks = network.getLinksAsPairs()

        txtIOHandler.exportNetwork(network, OUTPUT_FILE)
        val (actualNodes, actualLinks) = readNetwork(OUTPUT_FILE)

        assertIterableEquals(expectedNodes.sortedBy { it.id }, actualNodes.values.sortedBy { it.id })
        assertIterableEquals(
            expectedLinks.sortedWith(compareBy({ it.first }, { it.second })),
            actualLinks.sortedWith(compareBy({ it.first }, { it.second })),
        )
    }

    // Tests

    @BeforeAll
    fun startUp() {
        Platform.startup {}
    }

    @BeforeEach
    fun setUp() {
        network = UndirectedNetwork()
    }

    @Nested
    inner class ImportExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, export - output file contains imported data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(network)
        }
    }

    @Nested
    inner class ImportInspectExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect for communities, export - output file contains correct inspection data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(network, communityDetector::detectCommunities)
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect for centrality, export - output file contains correct inspection data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(network, centralityIdentifier::identifyCentrality)
        }
    }

    @Nested
    inner class ImportLayOutExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, lay out simply, export - output file contains correct layout data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(network, simpleLayout::applyLayout)
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, lay out smartly, export - output file contains correct layout data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(network, smartLayout::applyLayout)
        }
    }

    @Nested
    inner class ImportInspectLayOutExport {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect, lay out simply, export - output file contains correct data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(
                network,
                communityDetector::detectCommunities,
                centralityIdentifier::identifyCentrality,
                simpleLayout::applyLayout
            )
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(InputsProvider::class)
        fun `import, inspect, lay out smartly, export - output file contains correct data`(inputFile: File) {
            txtIOHandler.importNetwork(network, inputFile)

            assertCorrectExportWithActions(
                network,
                communityDetector::detectCommunities,
                centralityIdentifier::identifyCentrality,
                smartLayout::applyLayout
            )
        }
    }
}

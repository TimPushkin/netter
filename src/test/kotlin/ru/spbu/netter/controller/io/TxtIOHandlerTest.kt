package ru.spbu.netter.controller.io

import ru.spbu.netter.model.Network
import ru.spbu.netter.model.UndirectedNetwork

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.spbu.netter.model.Node
import java.io.File
import kotlin.streams.asStream


internal class TxtIOHandlerTest {
    lateinit var network: Network

    companion object {
        // TxtIOHandler constants

        private const val COLUMN_DELIMITER = " "

        // Parameterized tests supplementary data

        private const val PARAMETERIZED_TEST_NAME = "{displayName} <-- {arguments}"

        private const val OUTPUT_FILE_PATH = "out/test/txt-io-handler-test.txt"

        private const val INPUTS_PATH = "src/test/resources/txt-inputs/"

        private const val CORRECT_SINGLE_BLOCK_INPUTS_PATH = INPUTS_PATH + "correct-single-block-inputs/"
        private const val INCORRECT_SINGLE_BLOCK_INPUTS_PATH = INPUTS_PATH + "incorrect-single-block-inputs/"
        private const val CORRECT_DUAL_BLOCK_INPUTS_PATH = INPUTS_PATH + "correct-dual-block-inputs/"
        private const val INCORRECT_DUAL_BLOCK_INPUTS_PATH = INPUTS_PATH + "incorrect-dual-block-inputs/"

        private val CORRECT_SINGLE_BLOCK_INPUTS =
            File(CORRECT_SINGLE_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>()
        private val INCORRECT_SINGLE_BLOCK_INPUTS =
            File(INCORRECT_SINGLE_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>()
        private val CORRECT_DUAL_BLOCK_INPUTS =
            File(CORRECT_DUAL_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>()
        private val INCORRECT_DUAL_BLOCK_INPUTS =
            File(INCORRECT_DUAL_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>()

        private val CORRECT_INPUTS = CORRECT_SINGLE_BLOCK_INPUTS + CORRECT_DUAL_BLOCK_INPUTS
        private val INCORRECT_INPUTS = INCORRECT_SINGLE_BLOCK_INPUTS + INCORRECT_DUAL_BLOCK_INPUTS


        object CorrectInputsProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?) =
                CORRECT_INPUTS.map { Arguments.of(it) }.asSequence().asStream()
        }

        object IncorrectInputsProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?) =
                INCORRECT_INPUTS.map { Arguments.of(it) }.asSequence().asStream()
        }
    }

    // Helper functions

    private fun Network.nodesAsTriples() = nodes.map { it.value.run { Triple(id, community, centrality) } }

    private fun Network.linksAsPairs() = links.map { it.run { Pair(n1.id, n2.id) } }

    private fun readNetworkInput(inputFile: File): Pair<MutableSet<Pair<Int, Int>>, MutableMap<Int, Triple<Int, Int, Double>>> {
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

        var i = 0
        while (i < nodes.size) {
            if (!nodes.contains(i)) nodes[i] = Triple(i, Node.DEFAULT_COMMUNITY, Node.DEFAULT_CENTRALITY)
            i++
        }

        return Pair(links, nodes)
    }

    fun verifyInput(network: Network, inputFile: File) {
        val (expectedLinks, expectedNodes) = readNetworkInput(inputFile)

        assertIterableEquals(
            expectedLinks.sortedWith(compareBy({ it.first }, { it.second })),
            network.linksAsPairs().sortedWith(compareBy({ it.first }, { it.second }))
        )
        assertIterableEquals(
            expectedNodes.values.sortedBy { it.first },
            network.nodesAsTriples().sortedBy { it.first }
        )
    }

    fun verifyOutput(inputFile: File, outputFile: File) {
        val (expectedLinks, expectedNodes) = readNetworkInput(inputFile)
        val (actualLinks, actualNodes) = readNetworkInput(outputFile)

        assertIterableEquals(
            expectedLinks.sortedWith(compareBy({ it.first }, { it.second })),
            actualLinks.sortedWith(compareBy({ it.first }, { it.second }))
        )
        assertIterableEquals(
            expectedNodes.values.sortedBy { it.first },
            actualNodes.values.sortedBy { it.first }
        )
    }

    // Tests

    @BeforeEach
    fun setUp() {
        network = UndirectedNetwork()
    }


    @Nested
    inner class ImportNetwork {
        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(CorrectInputsProvider::class)
        fun `import correct data - the data is in the network`(inputFile: File) {
            TxtIOHandler().importNetwork(network, inputFile)

            verifyInput(network, inputFile)
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(IncorrectInputsProvider::class)
        fun `import incorrect data - throws HandledIOException`(inputFile: File) {
            assertThrows<HandledIOException> { TxtIOHandler().importNetwork(network, inputFile) }
        }
    }

    @Nested
    inner class ExportNetwork {
        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(CorrectInputsProvider::class)
        fun `import and export network - output file contains imported data`(inputFile: File) {
            TxtIOHandler().importNetwork(network, inputFile)
            TxtIOHandler().exportNetwork(network, File(OUTPUT_FILE_PATH))

            verifyOutput(inputFile, File(OUTPUT_FILE_PATH))
        }
    }
}

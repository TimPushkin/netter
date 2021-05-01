package ru.spbu.netter.controller

import ru.spbu.netter.model.Graph
import ru.spbu.netter.model.UndirectedGraph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.spbu.netter.model.Vertex
import java.io.File
import java.io.IOException
import kotlin.streams.asStream


internal class TxtIOHandlerTest {
    lateinit var graph: Graph


    companion object {
        // TxtIOHandler constants

        private const val COLUMN_DELIMITER = " "


        // Parameterized tests supplementary data

        private const val PARAMETERIZED_TEST_NAME = "{displayName} <-- {arguments}"

        private const val INPUTS_PATH = "src/test/resources/txt-inputs/"

        private const val CORRECT_SINGLE_BLOCK_INPUTS_PATH = INPUTS_PATH + "correct-single-block-inputs/"
        private const val INCORRECT_SINGLE_BLOCK_INPUTS_PATH = INPUTS_PATH + "incorrect-single-block-inputs/"
        private const val CORRECT_DUAL_BLOCK_INPUTS_PATH = INPUTS_PATH + "correct-dual-block-inputs/"
        private const val INCORRECT_DUAL_BLOCK_INPUTS_PATH = INPUTS_PATH + "incorrect-dual-block-inputs/"

        private val CORRECT_SINGLE_BLOCK_INPUTS =
            run { File(CORRECT_SINGLE_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>() }.map { it.path }
        private val INCORRECT_SINGLE_BLOCK_INPUTS =
            run { File(INCORRECT_SINGLE_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>() }.map { it.path }
        private val CORRECT_DUAL_BLOCK_INPUTS =
            run { File(CORRECT_DUAL_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>() }.map { it.path }
        private val INCORRECT_DUAL_BLOCK_INPUTS =
            run { File(INCORRECT_DUAL_BLOCK_INPUTS_PATH).listFiles() ?: emptyArray<File>() }.map { it.path }

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

    private fun Graph.verticesAsTriples() = vertices.map { it.value.run { Triple(id, community, centrality) } }

    private fun Graph.edgesAsPairs() = edges.map { it.run { Pair(v1.id, v2.id) } }

    fun verifyCorrectInput(graph: Graph, inputPath: String) {
        val expectedEdges = mutableSetOf<Pair<Int, Int>>()
        val expectedVertices = mutableMapOf<Int, Triple<Int, Int, Double>>()

        File(inputPath).bufferedReader().use { reader ->
            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                line.split(COLUMN_DELIMITER).run {
                    val id1 = get(0).toInt()
                    val id2 = get(1).toInt()

                    if (!expectedEdges.contains(Pair(id2, id1))) expectedEdges.add(Pair(id1, id2))

                    expectedVertices[id1] = Triple(id1, Vertex.DEFAULT_COMMUNITY, Vertex.DEFAULT_CENTRALITY)
                    expectedVertices[id2] = Triple(id2, Vertex.DEFAULT_COMMUNITY, Vertex.DEFAULT_CENTRALITY)
                }
            }

            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isEmpty()) break

                line.split(COLUMN_DELIMITER).run {
                    val id = get(0).toInt()

                    expectedVertices[id] = Triple(id, get(1).toInt(), get(2).toDouble())
                }
            }
        }

        var i = 0
        while (i < expectedVertices.size) {
            if (!expectedVertices.contains(i)) expectedVertices[i] =
                Triple(i, Vertex.DEFAULT_COMMUNITY, Vertex.DEFAULT_CENTRALITY)
            i++
        }

        assertIterableEquals(
            expectedEdges.sortedWith(compareBy({ it.first }, { it.second })),
            graph.edgesAsPairs().sortedWith(compareBy({ it.first }, { it.second }))
        )
        assertIterableEquals(
            expectedVertices.values.sortedBy { it.first },
            graph.verticesAsTriples().sortedBy { it.first }
        )
    }


    // Tests

    @BeforeEach
    fun setUp() {
        graph = UndirectedGraph()
    }


    @Nested
    inner class ImportNetwork {

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(CorrectInputsProvider::class)
        fun `import correct data - the data is in the graph`(inputFilename: String) {
            TxtIOHandler().importNetwork(graph, inputFilename)

            verifyCorrectInput(graph, inputFilename)
        }

        @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
        @ArgumentsSource(IncorrectInputsProvider::class)
        fun `import incorrect data - throws IOException`(inputFilename: String) {
            assertThrows<IOException> { TxtIOHandler().importNetwork(graph, inputFilename) }
        }
    }
}

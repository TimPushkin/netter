package ru.spbu.netter.controller

import javafx.geometry.Point2D
import ru.spbu.netter.model.Graph
import ru.spbu.netter.model.UndirectedGraph
import ru.spbu.netter.controller.LayoutMethod
import ru.spbu.netter.controller.CircularLayout
import ru.spbu.netter.controller.SmartLayout
import tornadofx.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.assertEquals

internal class LayoutMethodTest {
    lateinit var graph: Graph

    companion object {
        private val defaultLayout: LayoutMethod by inject<CircularLayout>()
        private val smartLayout: LayoutMethod by inject<SmartLayout>()
    }

    @BeforeEach
    fun setUp() {
        graph = UndirectedGraph()
    }

    @Nested
    inner class CircularLayoutTest {
        fun `import an empty graph`(){
            assertEquals(defaultLayout.layout(graph), arrayListOf<Point2D>())
        }
    }
}
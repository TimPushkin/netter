package ru.spbu.netter.view

import ru.spbu.netter.controller.LayoutMethod
import ru.spbu.netter.controller.CircularLayout
import ru.spbu.netter.controller.SmartLayout
import ru.spbu.netter.model.Graph
import ru.spbu.netter.model.UndirectedGraph
import tornadofx.*

class MainView : View() {
    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()
    private val graph: Graph = UndirectedGraph()

    override val root = vbox {
        button("Layout").setOnAction {
            smartLayout.layout(graph)
        }
    }

    init {
        defaultLayout.layout(graph)
    }
}

package ru.spbu.netter.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TextField
import ru.spbu.netter.controller.*
import ru.spbu.netter.model.Graph
import ru.spbu.netter.model.UndirectedGraph
import tornadofx.*
import java.io.IOException


class MainView : View("Netter") {
    private val graph: Graph = UndirectedGraph()
    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    override val root = borderpane {
        setPrefSize(800.0, 600.0)

        left = vbox {
            button("Import network").setOnAction {
                find<FilePathInputForm>(
                    mapOf(
                        FilePathInputForm::graph to graph,
                        FilePathInputForm::fileHandlingType to FilePathInputForm.FileHandlingType.IMPORT
                    )
                ).openModal(resizable = false)
            }

            button("Export network").setOnAction {
                find<FilePathInputForm>(
                    mapOf(
                        FilePathInputForm::graph to graph,
                        FilePathInputForm::fileHandlingType to FilePathInputForm.FileHandlingType.EXPORT
                    )
                ).openModal(resizable = false)
            }

            button("Apply smart layout").setOnAction {
                find<RepulsionInputForm>(
                    mapOf(
                        RepulsionInputForm::graph to graph,
                        RepulsionInputForm::layoutMethod to smartLayout
                    )
                ).openModal(resizable = false)
            }

            button("Apply default layout").setOnAction {
                find<RepulsionInputForm>(
                    mapOf(
                        RepulsionInputForm::graph to graph,
                        RepulsionInputForm::layoutMethod to defaultLayout
                    )
                ).openModal(resizable = false)
            }

            button("Display communities").setOnAction {
                println("Todo")
            }

            button("Display centrality").setOnAction {
                println("Todo")
            }
        }

        center = stackpane {
            group {
                println("Todo")
            }
        }
    }
}

class RepulsionInputForm : Fragment("Input repulsion") {
    val graph: Graph by param()
    val layoutMethod: LayoutMethod by param()

    override val root = form {
        var repulsion = TextField()

        fieldset {
            field("Enter repulsion:") {
                repulsion = textfield("10.0")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                layoutMethod.layout(graph, repulsion = repulsion.text.toDouble())
                close()
            }
        }
    }
}

class FilePathInputForm : Fragment("Input file path") {
    val graph: Graph by param()
    val fileHandlingType: FileHandlingType by param()

    enum class FileHandlingType { IMPORT, EXPORT }

    override val root = form {
        val fileIOHandlerNames = FXCollections.observableArrayList("Plain text", "Neo4J", "SQLite")
        val selectedFileIOHandlerName = SimpleStringProperty(fileIOHandlerNames[0])
        var filePath = TextField()

        fieldset {
            field("Select file type:") {
                combobox(selectedFileIOHandlerName, fileIOHandlerNames)
            }

            field("Enter file path:") {
                filePath = textfield()
            }
        }

        buttonbar {
            button("OK").setOnAction {
                val fileIOHandler = when (selectedFileIOHandlerName.get()) {
                    fileIOHandlerNames[0] -> find<TxtIOHandler>()
                    fileIOHandlerNames[1] -> TODO("Neo4J")
                    fileIOHandlerNames[2] -> TODO("SQLite")
                    else -> throw IOException("Unknown file IO handler selected: ${selectedFileIOHandlerName.get()}")
                }

                when (fileHandlingType) {
                    FileHandlingType.IMPORT -> fileIOHandler.importNetwork(graph, filePath.text)
                    FileHandlingType.EXPORT -> fileIOHandler.exportNetwork(graph, filePath.text)
                }

                close()
            }
        }
    }
}

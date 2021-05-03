package ru.spbu.netter.view

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import ru.spbu.netter.controller.*
import ru.spbu.netter.model.UndirectedGraph
import tornadofx.*
import java.io.File


class MainView : View("Netter") {
    private lateinit var graphView: GraphView
    private val availableFileIOHandlerNames = listOf("Plain text", "Neo4J", "SQLite")
    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    override val root = borderpane {
        setPrefSize(960.0, 540.0)

        left = vbox {
            button("Import network").setOnAction {
                val graph = UndirectedGraph()

                val (fileIOHandler, file) = getFile()
                fileIOHandler.importNetwork(graph, file)

                graphView = GraphView(graph).apply {
                    applyLayout(defaultLayout.layOut(graph, repulsion = getRepulsion()))
                }

                center = graphView
            }

            button("Export network").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    val (fileIOHandler, file) = getFile()
                    fileIOHandler.exportNetwork(graphView.graph, file)
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to export",
                    "To export a network you need to import one first"
                )
            }

            button("Apply smart layout").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    graphView.applyLayout(smartLayout.layOut(graphView.graph, repulsion = getRepulsion()))
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first"
                )
            }

            button("Apply default layout").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    graphView.applyLayout(defaultLayout.layOut(graphView.graph, repulsion = getRepulsion()))
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first"
                )
            }

            button("Display communities").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    println("Todo")
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first"
                )
            }

            button("Display centrality").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    println("Todo")
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first"
                )
            }
        }

        center = label("Import a network to be shown here")
    }

    private fun getFile(): Pair<FileIOHandler, File> {
        val fileIOHandlerName = TextField()
        val files = mutableListOf<File>()

        find<FileInputForm>(
            mapOf(
                FileInputForm::availableFileIOHandlerNames to availableFileIOHandlerNames,
                FileInputForm::fileIOHandlerName to fileIOHandlerName,
                FileInputForm::files to files,
            )
        ).openModal(block = true, resizable = false)

        val fileIOHandler = when (fileIOHandlerName.text) {
            availableFileIOHandlerNames[0] -> find<TxtIOHandler>()
            availableFileIOHandlerNames[1] -> TODO("Neo4J")
            availableFileIOHandlerNames[2] -> TODO("SQLite")
            else -> throw IllegalStateException("Unknown file IO handler selected: ${fileIOHandlerName.text}")
        }

        return Pair(fileIOHandler, files[0])
    }

    private fun getRepulsion(): Double {
        with(TextField()) {
            find<RepulsionInputForm>(mapOf(RepulsionInputForm::repulsion to this)).openModal(
                block = true,
                resizable = false
            )
            return text.toDouble()
        }
    }
}

class FileInputForm : Fragment("File path input") {
    val availableFileIOHandlerNames: List<String> by param()
    val fileIOHandlerName: TextField by param()
    val files: MutableList<File> by param()

    override val root = form {
        val selectedFileIOHandlerName = SimpleStringProperty(availableFileIOHandlerNames[0])

        fieldset {
            field("Select file type:") {
                combobox(selectedFileIOHandlerName, availableFileIOHandlerNames)
            }

            button("Choose file...").setOnAction {
                files.addAll(
                    chooseFile(
                        "Choose file to import network from...",
                        emptyArray(),
                        mode = FileChooserMode.Single
                    )
                )
            }
        }

        buttonbar {
            button("OK").setOnAction {
                fileIOHandlerName.text = selectedFileIOHandlerName.value
                close()
            }
        }
    }
}

class RepulsionInputForm : Fragment("Repulsion input") {
    val repulsion: TextField by param()

    override val root = form {
        var enteredRepulsion = TextField()

        fieldset {
            field("Enter repulsion:") {
                enteredRepulsion = textfield("10.0")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                repulsion.text = enteredRepulsion.text
                close()
            }
        }
    }
}

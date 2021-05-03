package ru.spbu.netter.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import ru.spbu.netter.controller.*
import ru.spbu.netter.model.UndirectedGraph
import tornadofx.*
import java.io.File


class MainView : View("Netter") {
    private lateinit var graphView: GraphView
    private val fileIOHandlerNames = listOf("Plain text", "Neo4J", "SQLite")
    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    override val root = borderpane {
        setPrefSize(960.0, 540.0)

        left = vbox {
            button("Import network").setOnAction {
                val graph = UndirectedGraph()
                val (fileIOHandler, file) = getFile()

                if (fileIOHandler != null && file != null) {
                    fileIOHandler.importNetwork(graph, file)

                    getRepulsion()?.let {
                        graphView = GraphView(graph).apply {
                            applyLayout(defaultLayout.layOut(graph, repulsion = it))
                        }
                        center = graphView
                    }
                }
            }

            button("Export network").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    val (fileIOHandler, file) = getFile()
                    if (fileIOHandler != null && file != null) fileIOHandler.exportNetwork(graphView.graph, file)
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to export",
                    "To export a network you need to import one first",
                )
            }

            button("Apply smart layout").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    getRepulsion()?.let { graphView.applyLayout(smartLayout.layOut(graphView.graph, repulsion = it)) }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first",
                )
            }

            button("Apply default layout").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    getRepulsion()?.let { graphView.applyLayout(defaultLayout.layOut(graphView.graph, repulsion = it)) }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first",
                )
            }

            button("Display communities").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    println("Todo")
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first",
                )
            }

            button("Display centrality").setOnAction {
                if (this@MainView::graphView.isInitialized) {
                    println("Todo")
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first",
                )
            }
        }

        center = label("Import a network to be shown here")
    }

    private fun getFile(): Pair<FileIOHandler?, File?> {
        val fileIOHandlerName = TextField()
        val file = SimpleObjectProperty<File?>(null)

        find<FileInputForm>(
            mapOf(
                FileInputForm::fileIOHandlerNames to fileIOHandlerNames,
                FileInputForm::fileIOHandlerName to fileIOHandlerName,
                FileInputForm::file to file,
            )
        ).openModal(block = true, resizable = false)

        return if (file.value != null && fileIOHandlerName.text in fileIOHandlerNames) {
            val fileIOHandler = when (fileIOHandlerName.text) {
                fileIOHandlerNames[0] -> find<TxtIOHandler>()
                fileIOHandlerNames[1] -> TODO("Neo4J")
                fileIOHandlerNames[2] -> TODO("SQLite")
                else -> throw IllegalStateException("Unknown file IO handler selected: ${fileIOHandlerName.text}")
            }
            Pair(fileIOHandler, file.value)
        } else Pair(null, null)
    }

    private fun getRepulsion(): Double? {
        with(TextField()) {
            find<RepulsionInputForm>(mapOf(RepulsionInputForm::repulsion to this)).openModal(
                block = true,
                resizable = false,
            )
            return text.toDoubleOrNull()
        }
    }
}

class FileInputForm : Fragment("File input") {
    val fileIOHandlerNames: List<String> by param()
    val fileIOHandlerName: TextField by param()
    val file: ObjectProperty<File?> by param()

    override val root = form {
        val selectedFileIOHandlerName = SimpleStringProperty(fileIOHandlerNames[0])

        fieldset {
            field("Select file type:") {
                combobox(selectedFileIOHandlerName, fileIOHandlerNames)
            }

            button("Select a file...").setOnAction {
                file.value = chooseFile(
                    "Choose a file to import a network from...",
                    emptyArray(),
                    mode = FileChooserMode.Single,
                ).firstOrNull()
            }
        }

        buttonbar {
            button("OK").setOnAction {
                if (file.value != null) {
                    fileIOHandlerName.text = selectedFileIOHandlerName.value
                    close()
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "No file selected",
                    "You need to select a file to import a network from",
                )
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
                enteredRepulsion = textfield("20.0")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                if (enteredRepulsion.text.isDouble()) {
                    repulsion.text = enteredRepulsion.text
                    close()
                } else alert(Alert.AlertType.INFORMATION, "Wrong repulsion input", "Repulsion must be a decimal number")
            }
        }
    }
}

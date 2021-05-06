package ru.spbu.netter.view

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import ru.spbu.netter.controller.*
import ru.spbu.netter.model.Network
import ru.spbu.netter.model.UndirectedNetwork
import tornadofx.*
import java.io.File
import java.io.IOException


class MainView : View("Netter") {
    private lateinit var networkView: NetworkView

    private val fileIOHandlerNames = listOf("Plain text", "Neo4J", "SQLite")

    private val navigationSpace: NavigationSpace by inject()

    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    private val communityDetector: CommunityDetector by inject<LeidenCommunityDetector>()

    override val root = borderpane {
        setPrefSize(960.0, 540.0)

        center<NavigationSpace>()

        left = vbox {
            button("Import network").setOnAction {
                val network: Network = UndirectedNetwork()
                val (fileIOHandler, file) = getFile()

                if (fileIOHandler != null && file != null) {
                    try {
                        fileIOHandler.importNetwork(network, file)
                    } catch (exception: IOException) {
                        alert(Alert.AlertType.ERROR, "Network import failed", exception.localizedMessage)
                        return@setOnAction
                    }

                    getRepulsion()?.let {
                        networkView = NetworkView(network).apply {
                            applyLayout(defaultLayout.layOut(network, repulsion = it))
                        }
                        navigationSpace.replaceNetwork(networkView)
                    }
                }
            }

            button("Export network").setOnAction {
                if (this@MainView::networkView.isInitialized) {
                    val (fileIOHandler, file) = getFile()
                    if (fileIOHandler != null && file != null) {
                        try {
                            fileIOHandler.exportNetwork(networkView.network, file)
                        } catch (exception: IOException) {
                            alert(Alert.AlertType.ERROR, "Network export failed", exception.localizedMessage)
                            return@setOnAction
                        }
                    }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to export",
                    "To export a network you need to import one first",
                )
            }

            button("Apply smart layout").setOnAction {
                if (this@MainView::networkView.isInitialized) {
                    getRepulsion()?.let {
                        networkView.applyLayout(smartLayout.layOut(networkView.network, repulsion = it))
                    }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first",
                )
            }

            button("Apply default layout").setOnAction {
                if (this@MainView::networkView.isInitialized) {
                    getRepulsion()?.let {
                        networkView.applyLayout(defaultLayout.layOut(networkView.network, repulsion = it))
                    }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to lay out",
                    "To lay out a network you need to import one first",
                )
            }

            button("Display communities").setOnAction {
                if (this@MainView::networkView.isInitialized) {
                    getResolution()?.let { communityDetector.detectCommunities(networkView.network, it) }
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first",
                )
            }

            button("Display centrality").setOnAction {
                if (this@MainView::networkView.isInitialized) {
                    println("Todo")
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Nothing to inspect",
                    "To inspect a network you need to import one first",
                )
            }
        }
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

    private fun getResolution(): Double? {
        with(TextField()) {
            find<ResolutionInputForm>(mapOf(ResolutionInputForm::resolution to this)).openModal(
                block = true,
                resizable = false,
            )
            return text.toDoubleOrNull()
        }
    }
}

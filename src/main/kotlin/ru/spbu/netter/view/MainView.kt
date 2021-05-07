package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import ru.spbu.netter.controller.clustering.CommunityDetector
import ru.spbu.netter.controller.clustering.LeidenCommunityDetector
import ru.spbu.netter.controller.io.FileIOHandler
import ru.spbu.netter.controller.io.TxtIOHandler
import ru.spbu.netter.controller.layout.CircularLayout
import ru.spbu.netter.controller.layout.LayoutMethod
import ru.spbu.netter.controller.layout.SmartLayout
import ru.spbu.netter.model.Network
import ru.spbu.netter.model.UndirectedNetwork
import tornadofx.*
import java.io.IOException


class MainView : View("Netter") {
    private lateinit var networkView: NetworkView
    private val isNetworkImportedProperty = SimpleBooleanProperty(this, "isNetworkImported", false)
    private var isNetworkImported by isNetworkImportedProperty

    private val navigationSpace: NavigationSpace by inject()

    private val txtIOHandler: FileIOHandler by inject<TxtIOHandler>()

    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    private val communityDetector: CommunityDetector by inject<LeidenCommunityDetector>()

    override val root = borderpane {
        setPrefSize(960.0, 540.0)

        center<NavigationSpace>()

        top = menubar {
            menu("File") {
                menu("Import") {
                    item("As plain text").action { importFromFile(txtIOHandler) }

                    item("As Neo4J database").action { importFromNeo4J() }

                    item("As SQLite database").action { TODO("SQLite") }
                }

                menu("Export") {
                    item("As plain text").action { exportFromFile(txtIOHandler) }

                    item("As Neo4J database").action { TODO("Neo4J") }

                    item("As SQLite database").action { TODO("SQLite") }
                }.apply { disableProperty().bind(!isNetworkImportedProperty) }
            }

            menu("Network") {
                item("Default layout").action {
                    getRepulsion()?.let {
                        networkView.applyLayout(defaultLayout.layOut(networkView.network, repulsion = it))
                    }
                }

                item("Smart layout").action {
                    getRepulsion()?.let {
                        networkView.applyLayout(smartLayout.layOut(networkView.network, repulsion = it))
                    }
                }

                item("Detect communities").action {
                    getResolution()?.let { communityDetector.detectCommunities(networkView.network, it) }
                }

                item("Detect centrality").action {
                    println("Todo")
                }
            }.apply { disableProperty().bind(!isNetworkImportedProperty) }

            menu("Help") {
                item("Netter at GitHub").action { hostServices.showDocument("https://github.com/TimPushkin/netter") }
            }
        }
    }

    private fun importFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile("Select a file to import...", emptyArray(), mode = FileChooserMode.Single).firstOrNull()

        if (file == null) {
            alert(Alert.AlertType.INFORMATION, "No file selected", "You need to select a file to import")
            return
        }

        val network: Network = UndirectedNetwork()

        try {
            fileIOHandler.importNetwork(network, file)
        } catch (exception: IOException) {
            alert(Alert.AlertType.ERROR, "Network import failed", exception.localizedMessage)
            return
        }

        getRepulsion()?.let {
            networkView = NetworkView(network).apply { applyLayout(defaultLayout.layOut(network, repulsion = it)) }
            isNetworkImported = true
            navigationSpace.replaceNetwork(networkView)
        }
    }

    private fun importFromNeo4J() {
        val connectURL = SimpleStringProperty()
        val username = SimpleStringProperty()
        val password = SimpleStringProperty()

        find<Neo4JCredentialsInputForm>(
            mapOf(
                Neo4JCredentialsInputForm::connectURL to connectURL,
                Neo4JCredentialsInputForm::username to username,
                Neo4JCredentialsInputForm::password to password,
            )
        ).openModal(block = true, resizable = false)

        if (connectURL.value != null && username.value != null && password.value != null) {
            println("${connectURL.value} ${username.value} ${password.value}")
        }
    }

    private fun exportFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile("Select a file for export...", emptyArray(), mode = FileChooserMode.Single).firstOrNull()

        if (file == null) {
            alert(Alert.AlertType.INFORMATION, "No file selected", "You need to select a file for export")
            return
        }

        try {
            fileIOHandler.exportNetwork(networkView.network, file)
        } catch (exception: IOException) {
            alert(Alert.AlertType.ERROR, "Network export failed", exception.localizedMessage)
        }
    }

    private fun getRepulsion(): Double? {
        with(SimpleStringProperty()) {
            find<RepulsionInputForm>(mapOf(RepulsionInputForm::repulsion to this)).openModal(
                block = true,
                resizable = false,
            )
            return value?.toDoubleOrNull()
        }
    }

    private fun getResolution(): Double? {
        with(SimpleStringProperty()) {
            find<ResolutionInputForm>(mapOf(ResolutionInputForm::resolution to this)).openModal(
                block = true,
                resizable = false,
            )
            return value?.toDoubleOrNull()
        }
    }
}

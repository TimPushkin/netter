package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import ru.spbu.netter.controller.clustering.*
import ru.spbu.netter.controller.io.*
import ru.spbu.netter.controller.layout.*
import ru.spbu.netter.model.Network
import ru.spbu.netter.model.UndirectedNetwork
import tornadofx.*


class MainView : View("Netter") {
    private lateinit var networkView: NetworkView
    private val isNetworkImportedProperty = SimpleBooleanProperty(this, "isNetworkImported", false)
    private var isNetworkImported by isNetworkImportedProperty

    private val navigationSpace: NavigationSpace by inject()

    private val txtIOHandler: FileIOHandler by inject<TxtIOHandler>()
    private val neo4jIOHandler: UriIOHandler by inject<Neo4jIOHandler>()

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

                    item("As Neo4j database").action { importFromUri(neo4jIOHandler) }

                    item("As SQLite database").action { TODO("SQLite") }
                }

                menu("Export") {
                    item("As plain text").action { exportFromFile(txtIOHandler) }

                    item("As Neo4j database").action { exportFromUri(neo4jIOHandler) }

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

    // NetworkView initialization

    private fun initNetworkView(network: Network) {
        getRepulsion()?.let {
            networkView = NetworkView(network).apply { applyLayout(defaultLayout.layOut(network, repulsion = it)) }
            isNetworkImported = true
            navigationSpace.replaceNetwork(networkView)
        }
    }

    // Input forms handling

    private fun getUriCredentials(): Triple<String?, String?, String?> {
        val uri = SimpleStringProperty()
        val username = SimpleStringProperty()
        val password = SimpleStringProperty()

        find<UriCredentialsInputForm>(
            mapOf(
                UriCredentialsInputForm::uri to uri,
                UriCredentialsInputForm::username to username,
                UriCredentialsInputForm::password to password,
            )
        ).openModal(block = true, resizable = false)

        return Triple(uri.value, username.value, password.value)
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

    // IO using a file

    private fun importFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile("Select a file to import...", emptyArray(), mode = FileChooserMode.Single).firstOrNull()

        if (file == null) {
            alert(Alert.AlertType.INFORMATION, "No file selected", "You need to select a file to import")
            return
        }

        val network: Network = UndirectedNetwork()
        try {
            fileIOHandler.importNetwork(network, file)
        } catch (exception: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network import failed", exception.localizedMessage)
            return
        }
        initNetworkView(network)
    }

    private fun exportFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile("Select a file for export...", emptyArray(), mode = FileChooserMode.Single).firstOrNull()

        if (file == null) {
            alert(Alert.AlertType.INFORMATION, "No file selected", "You need to select a file for export")
            return
        }

        try {
            fileIOHandler.exportNetwork(networkView.network, file)
        } catch (exception: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network export failed", exception.localizedMessage)
        }
    }

    // IO using URI

    private fun importFromUri(uriIOHandler: UriIOHandler) {
        val (uri, username, password) = getUriCredentials()

        if (uri == null || username == null || password == null) {
            alert(
                Alert.AlertType.INFORMATION,
                "Credentials not provided",
                "You need to provide all credentials to import from URI",
            )
            return
        }

        val network: Network = UndirectedNetwork()
        try {
            uriIOHandler.importNetwork(network, uri, username, password)
        } catch (exception: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network import failed", exception.localizedMessage)
            return
        }
        initNetworkView(network)
    }

    private fun exportFromUri(uriIOHandler: UriIOHandler) {
        val (uri, username, password) = getUriCredentials()

        if (uri == null || username == null || password == null) {
            alert(
                Alert.AlertType.INFORMATION,
                "Credentials not provided",
                "You need to provide all credentials to import from URI",
            )
            return
        }

        try {
            uriIOHandler.exportNetwork(networkView.network, uri, username, password)
        } catch (exception: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network export failed", exception.localizedMessage)
        }
    }
}

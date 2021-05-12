package ru.spbu.netter.view

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import ru.spbu.netter.controller.io.*
import ru.spbu.netter.model.*
import tornadofx.*


class MainView : View("Netter") {
    private val navigationSpace: NavigationSpace by inject()

    private val txtIOHandler: FileIOHandler by inject<TxtIOHandler>()
    private val neo4jIOHandler: UriIOHandler by inject<Neo4jIOHandler>()

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
                }.apply { disableProperty().bind(!navigationSpace.isNetworkImportedProperty) }
            }

            menu("Network") {
                item("Default layout").action {
                    getRepulsion()?.let { navigationSpace.applyDefaultLayout(it) }
                }

                item("Smart layout").action {
                    getRepulsion()?.let { navigationSpace.applySmartLayout(it) }
                }

                item("Inspect for communities").action {
                    getResolution()?.let { navigationSpace.inspectForCommunities(it) }
                }

                item("Inspect for centrality").action {
                    navigationSpace.inspectForCentrality()
                }
            }.apply { disableProperty().bind(!navigationSpace.isNetworkImportedProperty) }

            menu("Help") {
                item("Netter at GitHub").action { hostServices.showDocument("https://github.com/TimPushkin/netter") }
            }
        }
    }

    // NavigationSpace initialization

    private fun initNavigationSpace(network: Network) {
        getRepulsion()?.let { navigationSpace.initNetworkView(network, it) }
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
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network import failed", ex.localizedMessage)
            return
        }
        initNavigationSpace(network)
    }

    private fun exportFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile("Select a file for export...", emptyArray(), mode = FileChooserMode.Single).firstOrNull()

        if (file == null) {
            alert(Alert.AlertType.INFORMATION, "No file selected", "You need to select a file for export")
            return
        }

        try {
            fileIOHandler.exportNetwork(navigationSpace.networkView.network, file)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network export failed", ex.localizedMessage)
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
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network import failed", ex.localizedMessage)
            return
        }
        initNavigationSpace(network)
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
            uriIOHandler.exportNetwork(navigationSpace.networkView.network, uri, username, password)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, "Network export failed", ex.localizedMessage)
        }
    }
}

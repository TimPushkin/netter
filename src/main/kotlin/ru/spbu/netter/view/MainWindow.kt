package ru.spbu.netter.view

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import ru.spbu.netter.controller.io.*
import ru.spbu.netter.model.*
import tornadofx.*
import java.util.*


class MainWindow : View("Netter") {
    private val navigationSpace: NavigationSpace by inject()

    private val txtIOHandler: FileIOHandler by inject<TxtIOHandler>()
    private val neo4jIOHandler: UriIOHandler by inject<Neo4jIOHandler>()
    private val sqliteIHandler: FileIOHandler by inject<SQLiteIOHandler>()

    override val root = borderpane {
        setPrefSize(960.0, 540.0)

        center<NavigationSpace>()

        top = menubar {
            menu(messages["Menu_File"]) {
                menu(messages["Menu_Import"]) {
                    item(messages["MenuItem_TextIO"]).action { importFromFile(txtIOHandler) }

                    item(messages["MenuItem_Neo4jIO"]).action { importFromUri(neo4jIOHandler) }

                    item(messages["MenuItem_SQLiteIO"]).action { importFromFile(sqliteIHandler) }
                }

                menu(messages["Menu_Export"]) {
                    disableProperty().bind(!navigationSpace.isNetworkImportedProperty)

                    item(messages["MenuItem_TextIO"]).action { exportFromFile(txtIOHandler) }

                    item(messages["MenuItem_Neo4jIO"]).action { exportFromUri(neo4jIOHandler) }

                    item(messages["MenuItem_SQLiteIO"]).action { exportFromFile(sqliteIHandler) }
                }
            }

            menu(messages["Menu_Network"]) {
                disableProperty().bind(!navigationSpace.isNetworkImportedProperty)

                item(messages["MenuItem_CommunityInspection"]).action {
                    getResolution()?.let { navigationSpace.inspectForCommunities(it) }
                }

                item(messages["MenuItem_CentralityInspection"]).action {
                    navigationSpace.inspectForCentrality()
                }
            }

            menu(messages["Menu_Appearance"]) {
                menu(messages["Menu_Language"]) {
                    item(messages["MenuItem_English"]).action {
                        FX.locale = Locale.ENGLISH
                        reload()
                    }

                    item(messages["MenuItem_Russian"]).action {
                        FX.locale = Locale("ru")
                        reload()
                    }
                }
            }

            menu(messages["Menu_Help"]) {
                item(messages["MenuItem_NetterGitHub"]).action { hostServices.showDocument("https://github.com/TimPushkin/netter") }
            }
        }

        left<LeftMenu>()
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
        val file = chooseFile(
            messages["FileChooser_ImportSelection"],
            emptyArray(),
            mode = FileChooserMode.Single
        ).firstOrNull()

        if (file == null) {
            alert(
                Alert.AlertType.INFORMATION,
                messages["AlertHeader_NoFileSelected"],
                messages["AlertContent_NoFileSelectedImport"]
            )
            return
        }

        val network: Network = UndirectedNetwork()
        try {
            fileIOHandler.importNetwork(network, file)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, messages["AlertHeader_ImportFailed"], ex.localizedMessage)
            return
        }

        navigationSpace.network = network
    }

    private fun exportFromFile(fileIOHandler: FileIOHandler) {
        val file = chooseFile(
            messages["FileChooser_ExportSelection"],
            emptyArray(),
            mode = FileChooserMode.Single
        ).firstOrNull()

        if (file == null) {
            alert(
                Alert.AlertType.INFORMATION,
                messages["AlertHeader_NoFileSelected"],
                messages["AlertContent_NoFileSelectedExport"]
            )
            return
        }

        try {
            fileIOHandler.exportNetwork(navigationSpace.network, file)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, messages["AlertHeader_ExportFailed"], ex.localizedMessage)
        }
    }

    // IO using URI

    private fun importFromUri(uriIOHandler: UriIOHandler) {
        val (uri, username, password) = getUriCredentials()

        if (uri == null || username == null || password == null) {
            alert(
                Alert.AlertType.INFORMATION,
                messages["AlertHeader_NoCredentials"],
                messages["AlertContent_NoCredentialsImport"]
            )
            return
        }

        val network: Network = UndirectedNetwork()
        try {
            uriIOHandler.importNetwork(network, uri, username, password)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, messages["AlertHeader_ImportFailed"], ex.localizedMessage)
            return
        }

        navigationSpace.network = network
    }

    private fun exportFromUri(uriIOHandler: UriIOHandler) {
        val (uri, username, password) = getUriCredentials()

        if (uri == null || username == null || password == null) {
            alert(
                Alert.AlertType.INFORMATION,
                messages["AlertHeader_NoCredentials"],
                messages["AlertContent_NoCredentialsExport"]
            )
            return
        }

        try {
            uriIOHandler.exportNetwork(navigationSpace.network, uri, username, password)
        } catch (ex: HandledIOException) {
            alert(Alert.AlertType.ERROR, messages["AlertHeader_ExportFailed"], ex.localizedMessage)
        }
    }

    // Reload the window (the loaded network, if any, is saved)

    private fun reload() {
        if (navigationSpace.isNetworkImported) {
            val network = navigationSpace.network
            root.scene.findUIComponents().forEach { FX.replaceComponent(it) }
            navigationSpace.network = network
        } else root.scene.findUIComponents().forEach { FX.replaceComponent(it) }
    }
}

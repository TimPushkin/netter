package ru.spbu.netter.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import tornadofx.*
import java.io.File


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

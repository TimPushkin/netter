package ru.spbu.netter.view

import javafx.scene.control.Alert
import javafx.scene.control.TextField
import tornadofx.*


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
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Wrong repulsion input",
                    "Repulsion must be a decimal number",
                )
            }
        }
    }
}

class ResolutionInputForm : Fragment("Resolution input") {
    val resolution: TextField by param()

    override val root = form {
        var enteredResolution = TextField()

        fieldset {
            field("Enter resolution:") {
                enteredResolution = textfield("0.2")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                if (enteredResolution.text.isDouble() && enteredResolution.text.toDouble() > 0) {
                    resolution.text = enteredResolution.text
                    close()
                } else alert(
                    Alert.AlertType.INFORMATION,
                    "Wrong resolution input",
                    "Resolution must be a positive decimal number",
                )
            }
        }
    }
}

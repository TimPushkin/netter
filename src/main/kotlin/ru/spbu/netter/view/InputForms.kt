package ru.spbu.netter.view

import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import tornadofx.*


class UriCredentialsInputForm : Fragment("URI credentials input") {
    val uri: StringProperty by param()
    val username: StringProperty by param()
    val password: StringProperty by param()

    private lateinit var enteredUri: TextField
    private lateinit var enteredUsername: TextField
    private lateinit var enteredPassword: PasswordField

    override val root = form {
        fieldset {
            field("URI:") {
                enteredUri = textfield()
            }

            field("Username:") {
                enteredUsername = textfield()
            }

            field("Password:") {
                enteredPassword = passwordfield()
            }
        }

        buttonbar {
            button("OK").setOnAction {
                when {
                    enteredUri.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        "Wrong URI input",
                        "URI cannot be empty",
                    )
                    enteredUsername.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        "Wrong username input",
                        "Username cannot be empty",
                    )
                    enteredPassword.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        "Wrong password input",
                        "Password cannot be empty",
                    )
                    else -> {
                        uri.value = enteredUri.text
                        username.value = enteredUsername.text
                        password.value = enteredPassword.text
                        close()
                    }
                }
            }
        }
    }
}

class RepulsionInputForm : Fragment("Repulsion input") {
    val repulsion: StringProperty by param()

    private lateinit var enteredRepulsion: TextField

    override val root = form {
        fieldset {
            field("Enter repulsion:") {
                enteredRepulsion = textfield("20.0")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                if (enteredRepulsion.text.isDouble()) {
                    repulsion.value = enteredRepulsion.text
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
    val resolution: StringProperty by param()

    private lateinit var enteredResolution: TextField

    override val root = form {
        fieldset {
            field("Enter resolution:") {
                enteredResolution = textfield("0.2")
            }
        }

        buttonbar {
            button("OK").setOnAction {
                if (enteredResolution.text.isDouble() && enteredResolution.text.toDouble() > 0) {
                    resolution.value = enteredResolution.text
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

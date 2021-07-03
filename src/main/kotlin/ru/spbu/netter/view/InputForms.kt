package ru.spbu.netter.view

import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import tornadofx.*
import java.util.*


class UriCredentialsInputForm : Fragment("URI credentials input") {
    val uri: StringProperty by param()
    val username: StringProperty by param()
    val password: StringProperty by param()

    private lateinit var enteredUri: TextField
    private lateinit var enteredUsername: TextField
    private lateinit var enteredPassword: PasswordField

    init {
        FX.localeProperty().onChange {
            messages = ResourceBundle.getBundle(FX.messagesNameProvider(javaClass), FX.locale)
        }
    }

    override val root = form {
        fieldset {
            field({ messages["Field_URI"] }) {
                enteredUri = textfield()
            }

            field({ messages["Field_Username"] }) {
                enteredUsername = textfield()
            }

            field({ messages["Field_Password"] }) {
                enteredPassword = passwordfield()
            }
        }

        buttonbar {
            button({ messages["Button_OK"] }).setOnAction {
                when {
                    enteredUri.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        messages["AlertHeader_WrongURI"],
                        messages["AlertContent_WrongURI"],
                    )
                    enteredUsername.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        messages["AlertHeader_WrongUsername"],
                        messages["AlertContent_WrongUsername"],
                    )
                    enteredPassword.text.isEmpty() -> alert(
                        Alert.AlertType.INFORMATION,
                        messages["AlertHeader_WrongPassword"],
                        messages["AlertContent_WrongPassword"],
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

class ResolutionInputForm : Fragment("Resolution input") {
    val resolution: StringProperty by param()

    private lateinit var enteredResolution: TextField

    init {
        FX.localeProperty().onChange {
            messages = ResourceBundle.getBundle(FX.messagesNameProvider(javaClass), FX.locale)
        }
    }

    override val root = form {
        fieldset {
            field({ messages["Field_Resolution"] }) {
                enteredResolution = textfield("0.2")
            }
        }

        buttonbar {
            button({ messages["Button_OK"] }).setOnAction {
                if (enteredResolution.text.isDouble() && enteredResolution.text.toDouble() > 0) {
                    resolution.value = enteredResolution.text
                    close()
                } else alert(
                    Alert.AlertType.INFORMATION,
                    messages["AlertHeader_WrongResolution"],
                    messages["AlertContent_WrongResolution"],
                )
            }
        }
    }
}

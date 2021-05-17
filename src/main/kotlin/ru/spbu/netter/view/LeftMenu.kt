package ru.spbu.netter.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import tornadofx.*


class LeftMenu : View() {
    private val navigationSpace: NavigationSpace by inject()

    private val repulsion = SimpleStringProperty(this, "repulsion", "20.0")

    private val loopsNum = SimpleStringProperty(this, "loopsNum", "100")
    private val adjustSizes = SimpleBooleanProperty(this, "adjustSizes", true)
    private val barnesHut = SimpleBooleanProperty(this, "barnesHut", true)
    private val linLog = SimpleBooleanProperty(this, "linLog", false)
    private val strongGravity = SimpleBooleanProperty(this, "strongGravity", false)
    private val jitterTolerance = SimpleStringProperty(this, "jitterTolerance", "1.0")
    private val scaling = SimpleStringProperty(this, "scaling", "10.0")
    private val gravity = SimpleStringProperty(this, "gravity", "1.0")
    private val barnesHutTheta = SimpleStringProperty(this, "barnesHutTheta", "1.2")

    override val root = form {
        background = Background(BackgroundFill(Color.WHITE, null, null))
        disableProperty().bind(!navigationSpace.isNetworkImportedProperty)

        fieldset("Default layout") {
            field("Repulsion") { textfield(repulsion) }

            buttonbar {
                button("Start").setOnAction {
                    if (repulsion.value.isDouble()) {
                        navigationSpace.applyDefaultLayout(repulsion.value.toDouble())
                    } else alert(
                        Alert.AlertType.INFORMATION,
                        "Wrong repulsion input",
                        "Repulsion must be a decimal number",
                    )
                }
            }
        }

        fieldset("Smart layout") {
            field("Loops number") { textfield(loopsNum) }

            checkbox("Adjust sizes", adjustSizes)

            checkbox("Barnes-Hut optimization", barnesHut)

            checkbox("LinLog mode", linLog)

            checkbox("Strong gravity mode", strongGravity)

            field("Jitter tolerance") { textfield(jitterTolerance) }

            field("Scaling ratio") { textfield(scaling) }

            field("Gravity") { textfield(gravity) }

            field("Barnes-Hut theta") { textfield(barnesHutTheta) }

            buttonbar {
                button("Start").setOnAction {
                    when {
                        !loopsNum.value.isInt() || loopsNum.value.toInt() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            "Wrong loops number input",
                            "Loops number must be a positive integer",
                        )
                        !jitterTolerance.value.isDouble() || jitterTolerance.value.toDouble() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            "Wrong jitter tolerance input",
                            "Jitter tolerance must be a positive decimal number",
                        )
                        !scaling.value.isDouble() || scaling.value.toDouble() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            "Wrong scaling input",
                            "Scaling must be a positive decimal number",
                        )
                        !gravity.value.isDouble() -> alert(
                            Alert.AlertType.INFORMATION,
                            "Wrong gravity input",
                            "Gravity must be a decimal number",
                        )
                        !barnesHutTheta.value.isDouble() -> alert(
                            Alert.AlertType.INFORMATION,
                            "Wrong Barnes-Hut theta input",
                            "Barnes-Hut theta must be a decimal number",
                        )
                        else -> navigationSpace.applySmartLayout(
                            loopsNum.value.toInt(),
                            adjustSizes.value,
                            barnesHut.value,
                            linLog.value,
                            strongGravity.value,
                            jitterTolerance.value.toDouble(),
                            scaling.value.toDouble(),
                            gravity.value.toDouble(),
                            barnesHutTheta.value.toDouble(),
                        )
                    }
                }
            }
        }
    }
}

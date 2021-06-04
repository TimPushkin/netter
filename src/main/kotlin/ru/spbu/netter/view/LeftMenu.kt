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

        fieldset(messages["FieldSet_DefaultLayout"]) {
            field(messages["Field_Repulsion"]) { textfield(repulsion) }

            buttonbar {
                button(messages["Button_Start"]).setOnAction {
                    if (repulsion.value.isDouble()) {
                        navigationSpace.applyDefaultLayout(repulsion.value.toDouble())
                    } else alert(
                        Alert.AlertType.INFORMATION,
                        messages["AlertHeader_WrongRepulsion"],
                        messages["AlertContent_WrongRepulsion"],
                    )
                }
            }
        }

        fieldset(messages["FieldSet_SmartLayout"]) {
            field(messages["Field_LoopsNum"]) { textfield(loopsNum) }

            checkbox(messages["Checkbox_AdjustSizes"], adjustSizes)

            checkbox(messages["Checkbox_BarnesHut"], barnesHut)

            checkbox(messages["Checkbox_LinLog"], linLog)

            checkbox(messages["Checkbox_StrongGravity"], strongGravity)

            field(messages["Field_JitterTolerance"]) { textfield(jitterTolerance) }

            field(messages["Field_ScalingRatio"]) { textfield(scaling) }

            field(messages["Field_Gravity"]) { textfield(gravity) }

            field(messages["Field_BarnesHutTheta"]) { textfield(barnesHutTheta) }

            buttonbar {
                button(messages["Button_Start"]).setOnAction {
                    when {
                        !loopsNum.value.isInt() || loopsNum.value.toInt() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            messages["AlertHeader_WrongLoopsNum"],
                            messages["AlertContent_WrongLoopsNum"],
                        )
                        !jitterTolerance.value.isDouble() || jitterTolerance.value.toDouble() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            messages["AlertHeader_WrongJitterTolerance"],
                            messages["AlertContent_WrongJitterTolerance"],
                        )
                        !scaling.value.isDouble() || scaling.value.toDouble() <= 0 -> alert(
                            Alert.AlertType.INFORMATION,
                            messages["AlertHeader_WrongScalingRatio"],
                            messages["AlertContent_WrongScalingRatio"],
                        )
                        !gravity.value.isDouble() -> alert(
                            Alert.AlertType.INFORMATION,
                            messages["AlertHeader_WrongGravity"],
                            messages["AlertContent_WrongGravity"],
                        )
                        !barnesHutTheta.value.isDouble() -> alert(
                            Alert.AlertType.INFORMATION,
                            messages["AlertHeader_WrongBarnesHutTheta"],
                            messages["AlertContent_WrongBarnesHutTheta"],
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

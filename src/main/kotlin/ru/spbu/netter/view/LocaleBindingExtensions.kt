package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.control.*
import tornadofx.*


fun MenuBar.menu(func: () -> String, op: Menu.() -> Unit = {}) = menu {
    textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun Menu.menu(func: () -> String, op: Menu.() -> Unit = {}) = menu {
    textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun Menu.item(func: () -> String, op: MenuItem.() -> Unit = {}) = item(func()) {
    textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun EventTarget.fieldset(func: () -> String, op: Fieldset.() -> Unit = {}) = fieldset {
    textProperty.bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun EventTarget.field(func: () -> String, op: Field.() -> Unit = {}) = field {
    textProperty.bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun EventTarget.checkbox(func: () -> String, property: Property<Boolean>? = null, op: CheckBox.() -> Unit = {}) =
    checkbox(func(), property) {
        textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
        op(this)
    }

fun EventTarget.label(func: () -> String, op: Label.() -> Unit = {}) = label {
    textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

fun ButtonBar.button(func: () -> String, op: Button.() -> Unit = {}) = button {
    textProperty().bind(Bindings.createStringBinding(func, FX.localeProperty()))
    op(this)
}

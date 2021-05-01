package ru.spbu.netter.view

import ru.spbu.netter.controller.LayoutMethod
import ru.spbu.netter.controller.CircularLayout
import ru.spbu.netter.controller.SmartLayout
import tornadofx.*

class MainView : View() {
    private val defaultLayout: LayoutMethod by inject<CircularLayout>()
    private val smartLayout: LayoutMethod by inject<SmartLayout>()

    override val root = vbox {
        button("Layout").setOnAction {
            smartLayout.layout()
        }
    }

    init {
        defaultLayout.layout()
    }
}

package ru.spbu.netter.controller

import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent


interface NetworkNavigationHandler {

    fun handleMouseDragged(event: MouseEvent)

    fun handleScroll(event: ScrollEvent)
}

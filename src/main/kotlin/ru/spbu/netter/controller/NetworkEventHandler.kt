package ru.spbu.netter.controller

import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent


interface NetworkEventHandler {

    fun handleMousePressed(event: MouseEvent)

    fun handleMouseDragged(event: MouseEvent)

    fun handleScroll(event: ScrollEvent)
}

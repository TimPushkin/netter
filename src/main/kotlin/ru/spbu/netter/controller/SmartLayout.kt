package ru.spbu.netter.controller

import tornadofx.*


class SmartLayout : Controller(), LayoutMethod {
    override fun layout() {
        println("Placing vertices in a nice shape...")
    }
}

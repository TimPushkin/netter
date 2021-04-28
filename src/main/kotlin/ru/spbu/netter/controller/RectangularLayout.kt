package ru.spbu.netter.controller

import tornadofx.*


class RectangularLayout : Controller(), LayoutMethod {
    override fun layout() {
        println("Placing vertices in a rectangular shape...")
    }
}

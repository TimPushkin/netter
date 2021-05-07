package ru.spbu.netter.view

import javafx.scene.shape.Line


class LinkView(n1: NodeView, n2: NodeView) : Line() {

    init {
        startXProperty().bind(n1.centerXProperty())
        startYProperty().bind(n1.centerYProperty())
        endXProperty().bind(n2.centerXProperty())
        endYProperty().bind(n2.centerYProperty())
    }
}

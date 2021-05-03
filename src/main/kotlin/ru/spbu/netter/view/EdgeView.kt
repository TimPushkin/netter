package ru.spbu.netter.view

import javafx.scene.shape.Line


class EdgeView(v1: VertexView, v2: VertexView) : Line() {

    init {
        startXProperty().bind(v1.centerXProperty())
        startYProperty().bind(v1.centerYProperty())
        endXProperty().bind(v2.centerXProperty())
        endYProperty().bind(v2.centerYProperty())
    }
}

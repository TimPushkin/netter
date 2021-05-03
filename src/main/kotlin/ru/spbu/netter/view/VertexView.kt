package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import ru.spbu.netter.model.Vertex


class VertexView(val vertex: Vertex, x: Double, y: Double, var colorsNum: IntegerProperty) : Circle() {

    init {
        super.setCenterX(x)
        super.setCenterY(y)
        super.radiusProperty().bind(Bindings.createDoubleBinding(::calculateRadius, vertex.centralityProperty))
        super.fillProperty().bind(Bindings.createObjectBinding(::calculateColor, colorsNum, vertex.communityProperty))
    }

    companion object {
        private const val MIN_RADIUS = 1.0
        private const val RADIUS_SCALING = 1.0

        private const val WEB_COLOR_RADIX = 16
        private const val MAX_WEB_COLOR = 0xFFFFFF
        private const val MAX_WEB_COLOR_LEN = 6
    }

    private fun calculateRadius() = MIN_RADIUS + vertex.centrality * RADIUS_SCALING

    private fun calculateColor(): Color =
        Color.web(
            "#" + (MAX_WEB_COLOR / colorsNum.value * vertex.community)
                .toString(WEB_COLOR_RADIX)
                .padStart(MAX_WEB_COLOR_LEN, '0')
        )
}

package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.TextBoundsType
import ru.spbu.netter.model.Node
import tornadofx.*


class NodeView(private val node: Node, x: Double, y: Double, var colorsNum: IntegerProperty) : Circle(x, y, 0.0) {
    val label = text(node.id.toString()) {
        scaleXProperty().bind(radiusProperty() * LABEL_SCALING)
        scaleYProperty().bind(radiusProperty() * LABEL_SCALING)

        boundsType = TextBoundsType.LOGICAL_VERTICAL_CENTER
        xProperty().bind(centerXProperty() - layoutBounds.width / 2)

        boundsType = TextBoundsType.VISUAL
        yProperty().bind(centerYProperty() + layoutBounds.height / 2)
    }

    init {
        radiusProperty().bind(Bindings.createDoubleBinding(::calculateRadius, node.centralityProperty))
        strokeWidthProperty().bind(Bindings.createDoubleBinding(::calculateStrokeWidth, radiusProperty()))
        fillProperty().bind(Bindings.createObjectBinding(::calculateFillColor, colorsNum, node.communityProperty))
        strokeProperty().bind(Bindings.createObjectBinding(::calculateStrokeColor, fillProperty()))
    }

    companion object {
        private const val LABEL_SCALING = 0.12

        private const val MIN_RADIUS = 5.0
        private const val RADIUS_SCALING = 1.5

        private const val STROKE_SCALING = 0.2

        private const val WEB_COLOR_RADIX = 16
        private const val MAX_WEB_COLOR = 0xFFFFFF
        private const val MAX_WEB_COLOR_LEN = 6
    }

    private fun calculateRadius() = MIN_RADIUS + node.centrality * RADIUS_SCALING

    private fun calculateStrokeWidth() = radius * STROKE_SCALING

    private fun calculateFillColor() = Color.web(
        "#" + (MAX_WEB_COLOR / (colorsNum.value + 1) * (node.community + 1))
            .toString(WEB_COLOR_RADIX)
            .padStart(MAX_WEB_COLOR_LEN, '0')
    )

    private fun calculateStrokeColor() = calculateFillColor().darker()
}

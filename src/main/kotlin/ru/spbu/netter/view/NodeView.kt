package ru.spbu.netter.view

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.TextBoundsType
import ru.spbu.netter.model.Node
import tornadofx.*


private const val LABEL_SCALING = 0.12

private const val MIN_RADIUS = 5.0
private const val RADIUS_SCALING = 1.5

private const val STROKE_SCALING = 0.2

private const val MAX_HUE = 360.0
private const val BRIGHTNESS_BOUNDARY = 0.25


class NodeView(val node: Node, private val colorsNum: IntegerProperty) : Circle() {
    val label = text(node.id.toString()) {
        scaleXProperty().bind(radiusProperty() * LABEL_SCALING)
        scaleYProperty().bind(radiusProperty() * LABEL_SCALING)

        boundsType = TextBoundsType.LOGICAL_VERTICAL_CENTER
        xProperty().bind(centerXProperty() - layoutBounds.width / 2)

        boundsType = TextBoundsType.VISUAL
        yProperty().bind(centerYProperty() + layoutBounds.height / 2)
    }

    init {
        centerXProperty().bind(node.xProperty)
        centerYProperty().bind(node.yProperty)

        radiusProperty().bind(Bindings.createDoubleBinding(::calculateRadius, node.centralityProperty))

        strokeWidthProperty().bind(Bindings.createDoubleBinding(::calculateStrokeWidth, radiusProperty()))
        fillProperty().bind(Bindings.createObjectBinding(::calculateFillColor, colorsNum, node.communityProperty))
        strokeProperty().bind(Bindings.createObjectBinding(::calculateStrokeColor, fillProperty()))

        label.fillProperty().bind(Bindings.createObjectBinding(::calculateLabelColor, fillProperty()))
    }

    private fun calculateRadius() = MIN_RADIUS + node.centrality * RADIUS_SCALING

    private fun calculateStrokeWidth() = radius * STROKE_SCALING

    private fun calculateFillColor() = Color.hsb(MAX_HUE / (colorsNum.value + 1) * (node.community + 1), 1.0, 1.0)

    private fun calculateStrokeColor() = calculateFillColor().darker()

    private fun calculateLabelColor() =
        if (calculateFillColor().grayscale().brightness >= BRIGHTNESS_BOUNDARY) Color.BLACK else Color.WHITE
}

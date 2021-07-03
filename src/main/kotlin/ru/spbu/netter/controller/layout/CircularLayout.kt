package ru.spbu.netter.controller.layout

import ru.spbu.netter.model.Network
import javafx.geometry.Point2D
import mu.KotlinLogging
import tornadofx.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


private val logger = KotlinLogging.logger {}

private const val CENTER_X = 0.0
private const val CENTER_Y = 0.0


class CircularLayout : Controller(), SimpleLayoutMethod {
    override val status = TaskStatus()

    init {
        FX.localeProperty().onChange {
            messages = ResourceBundle.getBundle(FX.messagesNameProvider(javaClass), FX.locale)
        }
    }

    override fun applyLayout(network: Network, repulsion: Double, executeOnSuccess: () -> Unit) {
        if (network.isEmpty()) {
            logger.info { "There is nothing to lay out in a circle: network $network is empty" }
            return
        }

        logger.info { "Placing nodes in a circular shape with repulsion $repulsion..." }

        val center = Point2D(CENTER_X, CENTER_Y)
        val angle = Math.toRadians(360.0 / network.nodes.size)
        val radius = (repulsion * sqrt(2 / (1 - cos(angle)))).takeIf { it != Double.POSITIVE_INFINITY } ?: 0.0
        var curr = Point2D(0.0, radius)

        runAsync(true, status) {
            updateMessage(messages["ProgressMessage"])

            List(network.nodes.size) { Point2D(curr.x, curr.y).also { curr = curr.rotated(center, angle) } }
        } success { coordinates ->
            network.nodes.values.forEachIndexed { i, node ->
                node.x = coordinates[i].x
                node.y = coordinates[i].y
            }

            logger.info { "Placing nodes in a circular shape has been finished" }

            executeOnSuccess()
        } fail { ex ->
            throw RuntimeException("Placing nodes in a circular shape has been failed", ex)
        }
    }

    private fun Point2D.rotated(center: Point2D, angle: Double): Point2D {
        val sin = sin(angle)
        val cos = cos(angle)

        val tmp = subtract(center)
        val rotated = Point2D(
            tmp.x * cos - tmp.y * sin,
            tmp.x * sin + tmp.y * cos,
        )

        return rotated.add(center)
    }
}

package org.gephi.layout.plugin.forceAtlas2

import kotlin.math.ln
import kotlin.math.sqrt


class ForceFactory {

    fun buildRepulsion(adjustBySize: Boolean, coefficient: Double) =
        if (adjustBySize) LinRepulsionAntiCollision(coefficient) else LinRepulsion(coefficient)

    fun getStrongGravity(coefficient: Double): RepulsionForce = StrongGravity(coefficient)

    fun buildAttraction(
        logAttraction: Boolean,
        distributedAttraction: Boolean,
        adjustBySize: Boolean,
        coefficient: Double,
    ) =
        if (adjustBySize) {
            if (logAttraction) {
                if (distributedAttraction) LogAttractionDegreeDistributedAntiCollision(coefficient)
                else LogAttractionAntiCollision(coefficient)
            } else {
                if (distributedAttraction) LinAttractionDegreeDistributedAntiCollision(coefficient)
                else LinAttractionAntiCollision(coefficient)
            }
        } else {
            if (logAttraction) {
                if (distributedAttraction) LogAttractionDegreeDistributed(coefficient)
                else LogAttraction(coefficient)
            } else {
                if (distributedAttraction) LinAttractionMassDistributed(coefficient)
                else LinAttraction(coefficient)
            }
        }

    abstract class AttractionForce {

        abstract fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double)
    }

    abstract class RepulsionForce {

        abstract fun apply(faNode1: FaNode, faNode2: FaNode)

        abstract fun apply(faNode: FaNode, region: Region)

        abstract fun apply(faNode: FaNode, gravitation: Double)
    }

    private class LinRepulsion(private val coefficient: Double) : RepulsionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = coefficient * faNode1.mass * faNode2.mass / distance / distance
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }

        override fun apply(faNode: FaNode, region: Region) {
            val xDist = faNode.x - region.massCenterX
            val yDist = faNode.y - region.massCenterY
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = coefficient * faNode.mass * region.mass / distance / distance
                faNode.dx += xDist * factor
                faNode.dy += yDist * factor
            }
        }

        override fun apply(faNode: FaNode, gravitation: Double) {
            val xDist = faNode.x
            val yDist = faNode.y

            val distance = sqrt(xDist * xDist + yDist * yDist)
            if (distance > 0) {
                val factor = coefficient * faNode.mass * gravitation / distance
                faNode.dx -= xDist * factor
                faNode.dy -= yDist * factor
            }
        }
    }

    private class LinRepulsionAntiCollision(private val coefficient: Double) : RepulsionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist) - faNode1.size - faNode2.size

            if (distance > 0) {
                val factor = coefficient * faNode1.mass * faNode2.mass / distance / distance
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            } else if (distance < 0) {
                val factor = 100 * coefficient * faNode1.mass * faNode2.mass
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }

        override fun apply(faNode: FaNode, region: Region) {
            val xDist = faNode.x - region.massCenterX
            val yDist = faNode.y - region.massCenterY
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = coefficient * faNode.mass * region.mass / distance / distance
                faNode.dx += xDist * factor
                faNode.dy += yDist * factor
            } else if (distance < 0) {
                val factor = -coefficient * faNode.mass * region.mass / distance
                faNode.dx += xDist * factor
                faNode.dy += yDist * factor
            }
        }

        override fun apply(faNode: FaNode, gravitation: Double) {
            val xDist = faNode.x
            val yDist = faNode.y
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = coefficient * faNode.mass * gravitation / distance
                faNode.dx -= xDist * factor
                faNode.dy -= yDist * factor
            }
        }
    }

    private class StrongGravity(private val coefficient: Double) : RepulsionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode) {
            // Not Relevant
        }

        override fun apply(faNode: FaNode, region: Region) {
            // Not Relevant
        }

        override fun apply(faNode: FaNode, gravitation: Double) {
            val xDist = faNode.x
            val yDist = faNode.y
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = coefficient * faNode.mass * gravitation
                faNode.dx -= xDist * factor
                faNode.dy -= yDist * factor
            }
        }
    }

    private class LinAttraction(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y

            val factor = -coefficient * edgeWeight
            faNode1.dx += xDist * factor
            faNode1.dy += yDist * factor
            faNode2.dx -= xDist * factor
            faNode2.dy -= yDist * factor
        }
    }

    private class LinAttractionMassDistributed(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y

            val factor = -coefficient * edgeWeight / faNode1.mass
            faNode1.dx += xDist * factor
            faNode1.dy += yDist * factor
            faNode2.dx -= xDist * factor
            faNode2.dy -= yDist * factor
        }
    }

    private class LogAttraction(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = -coefficient * edgeWeight * ln(1 + distance) / distance
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }

    private class LogAttractionDegreeDistributed(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist)

            if (distance > 0) {
                val factor = -coefficient * edgeWeight * ln(1 + distance) / distance / faNode1.mass
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }

    private class LinAttractionAntiCollision(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist) - faNode1.size - faNode2.size

            if (distance > 0) {
                val factor = -coefficient * edgeWeight
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }

    private class LinAttractionDegreeDistributedAntiCollision(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist) - faNode1.size - faNode2.size

            if (distance > 0) {
                val factor = -coefficient * edgeWeight / faNode1.mass
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }

    private class LogAttractionAntiCollision(private val coefficient: Double) : AttractionForce() {

        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist) - faNode1.size - faNode2.size

            if (distance > 0) {
                val factor = -coefficient * edgeWeight * ln(1 + distance) / distance
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }

    private class LogAttractionDegreeDistributedAntiCollision(private val coefficient: Double) : AttractionForce() {
        override fun apply(faNode1: FaNode, faNode2: FaNode, edgeWeight: Double) {
            val xDist = faNode1.x - faNode2.x
            val yDist = faNode1.y - faNode2.y
            val distance = sqrt(xDist * xDist + yDist * yDist) - faNode1.size - faNode2.size

            if (distance > 0) {
                val factor = -coefficient * edgeWeight * ln(1 + distance) / distance / faNode1.mass
                faNode1.dx += xDist * factor
                faNode1.dy += yDist * factor
                faNode2.dx -= xDist * factor
                faNode2.dy -= yDist * factor
            }
        }
    }
}

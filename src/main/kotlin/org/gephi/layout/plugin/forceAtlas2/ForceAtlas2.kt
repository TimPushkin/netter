package org.gephi.layout.plugin.forceAtlas2

import ru.spbu.netter.model.Network
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt


class ForceAtlas2(
    network: Network,
    private val outboundAttractionDistribution: Boolean = true,
    private val adjustSizes: Boolean = true,
    private val barnesHutOptimize: Boolean = true,
    private val linLogMode: Boolean = false,
    private val strongGravityMode: Boolean = false,
    private val edgeWeightInfluence: Double = 1.0,
    private val jitterTolerance: Double = 1.0,
    private val scalingRatio: Double = 2.0,
    private val gravity: Double = 1.0,
    private val barnesHutTheta: Double = 1.0,
) {
    private val faNodes = mutableListOf<FaNode>()
    private val faLinks = mutableListOf<FaLink>()
    private val degrees = Array(network.nodes.size) { 0 }

    private var speed = 1.0
    private var speedEfficiency = 1.0
    private var outboundAttCompensation = 1.0
    private lateinit var rootRegion: Region

    private val threadCount = 4.coerceAtMost(1.coerceAtLeast(Runtime.getRuntime().availableProcessors() - 1))
    private val currentThreadCount = threadCount
    private val pool = Executors.newFixedThreadPool(threadCount)

    init {
        for (link in network.links) {
            faLinks += FaLink(link)

            degrees[link.n1.id]++
            if (link.n1.id != link.n2.id) degrees[link.n2.id]++
        }

        for (node in network.nodes.values) faNodes += FaNode(node).apply { mass = 1.0 + degrees[node.id] }
    }

    fun start() {
        for (faNode in faNodes) faNode.apply {
            mass = 1.0 + degrees[node.id]
            oldDx = dx
            oldDy = dy
            dx = 0.0
            dy = 0.0
        }

        if (barnesHutOptimize) rootRegion = Region(faNodes).apply { buildSubRegions() }

        if (outboundAttractionDistribution) {
            outboundAttCompensation = 0.0
            for (nodeLayout in faNodes) outboundAttCompensation += nodeLayout.mass
            outboundAttCompensation /= faNodes.size
        }

        val repulsionForce = ForceFactory().buildRepulsion(adjustSizes, scalingRatio)

        val taskCount = 8 * currentThreadCount
        val threads = mutableListOf<Future<*>>()
        for (taskNum in taskCount downTo 1) threads.add(
            pool.submit(
                NodesThread(
                    faNodes,
                    floor((faNodes.size * (taskNum - 1) / taskCount).toDouble()).toInt(),
                    floor((faNodes.size * taskNum / taskCount).toDouble()).toInt(),
                    barnesHutOptimize,
                    barnesHutTheta,
                    gravity,
                    if (strongGravityMode) ForceFactory().getStrongGravity(scalingRatio) else repulsionForce,
                    scalingRatio,
                    rootRegion,
                    repulsionForce,
                )
            )
        )
        for (future in threads) {
            try {
                future.get()
            } catch (ex: Exception) {
                throw RuntimeException("Unable to layout " + this.javaClass.simpleName + ".", ex)
            }
        }

        val attractionForce = ForceFactory().buildAttraction(
            linLogMode, outboundAttractionDistribution, adjustSizes,
            1.0 * if (outboundAttractionDistribution) outboundAttCompensation else 1.0,
        )
        when (edgeWeightInfluence) {
            0.0 -> for (faLink in faLinks) attractionForce.apply(
                faNodes[faLink.link.n1.id],
                faNodes[faLink.link.n2.id],
                1.0
            )
            1.0 -> for (faLink in faLinks) attractionForce.apply(
                faNodes[faLink.link.n1.id],
                faNodes[faLink.link.n2.id],
                faLink.weight
            )
            else -> for (faLink in faLinks) attractionForce.apply(
                faNodes[faLink.link.n1.id],
                faNodes[faLink.link.n2.id],
                faLink.weight.pow(edgeWeightInfluence)
            )
        }

        var totalSwinging = 0.0
        var totalEffectiveTraction = 0.0
        for (node in faNodes) {
            if (node.isFixed) continue
            totalSwinging += node.mass * sqrt((node.oldDx - node.dx).pow(2.0) + (node.oldDy - node.dy).pow(2.0))
            totalEffectiveTraction +=
                node.mass * 0.5 * sqrt((node.oldDx + node.dx).pow(2.0) + (node.oldDy + node.dy).pow(2.0))
        }

        val estimatedOptimalJitterTolerance = 0.05 * sqrt(faNodes.size.toDouble())
        val minJT = sqrt(estimatedOptimalJitterTolerance)
        val maxJT = 10.0
        var jt = jitterTolerance * minJT.coerceAtLeast(
            maxJT.coerceAtMost(
                estimatedOptimalJitterTolerance * totalEffectiveTraction / faNodes.size.toDouble().pow(2.0)
            )
        )

        val minSpeedEfficiency = 0.05

        if (totalSwinging / totalEffectiveTraction > 2.0) {
            if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= 0.5
            jt = jt.coerceAtLeast(jitterTolerance)
        }

        val targetSpeed = jt * speedEfficiency * totalEffectiveTraction / totalSwinging

        if (totalSwinging > jt * totalEffectiveTraction) {
            if (speedEfficiency > minSpeedEfficiency) speedEfficiency *= 0.7
        } else if (speed < 1000) speedEfficiency *= 1.3

        val maxRise = 0.5
        speed += (targetSpeed - speed).coerceAtMost(maxRise * speed)

        if (adjustSizes) {
            for (node in faNodes) {
                if (node.isFixed) continue

                val swinging =
                    node.mass * sqrt((node.oldDx - node.dx) * (node.oldDx - node.dx) + (node.oldDy - node.dy) * (node.oldDy - node.dy))
                val df = sqrt(node.dx.pow(2.0) + node.dy.pow(2.0))
                val factor = (0.1 * speed / (1f + sqrt(speed * swinging)) * df).coerceAtMost(10.0) / df

                node.x = node.x + node.dx * factor
                node.y = node.y + node.dy * factor
            }
        } else {
            for (node in faNodes) {
                if (node.isFixed) continue

                val swinging: Double =
                    node.mass * sqrt((node.oldDx - node.dx) * (node.oldDx - node.dx) + (node.oldDy - node.dy) * (node.oldDy - node.dy))
                val factor = speed / (1f + sqrt(speed * swinging))

                node.x = node.x + node.dx * factor
                node.y = node.y + node.dy * factor
            }
        }
    }

    fun getResults(): List<FaNode> {
        pool.shutdown()
        return faNodes
    }
}

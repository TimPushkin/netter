package org.gephi.layout.plugin.forceAtlas2

import kotlin.math.sqrt


class Region(private val faNodes: List<FaNode>) {
    private val subregions = mutableListOf<Region>()

    var mass = 0.0
    var massCenterX = 0.0
    var massCenterY = 0.0
    private var size = 0.0

    init {
        updateMassAndGeometry()
    }

    private fun updateMassAndGeometry() {
        if (faNodes.size <= 1) return

        mass = 0.0
        var massSumX = 0.0
        var massSumY = 0.0

        for (faNode in faNodes) {
            mass += faNode.mass
            massSumX += faNode.x * faNode.mass
            massSumY += faNode.y * faNode.mass
        }

        massCenterX = massSumX / mass
        massCenterY = massSumY / mass

        size = Double.MIN_VALUE
        for (faNode in faNodes) with(faNode) {
            size =
                size.coerceAtLeast(2 * sqrt((x - massCenterX) * (x - massCenterX) + (y - massCenterY) * (y - massCenterY)))
        }
    }

    @Synchronized
    fun buildSubRegions() {
        if (faNodes.size <= 1) return

        val leftNodes = mutableListOf<FaNode>()
        val rightNodes = mutableListOf<FaNode>()
        for (faNode in faNodes) (if (faNode.x < massCenterX) leftNodes else rightNodes).add(faNode)

        val topLeftNodes = mutableListOf<FaNode>()
        val bottomLeftNodes = mutableListOf<FaNode>()
        for (faNode in leftNodes) (if (faNode.y < massCenterY) bottomLeftNodes else topLeftNodes).add(faNode)

        val bottomRightNodes = mutableListOf<FaNode>()
        val topRightNodes = mutableListOf<FaNode>()
        for (faNode in rightNodes) (if (faNode.y < massCenterY) bottomRightNodes else topRightNodes).add(faNode)

        addSubRegion(topLeftNodes)
        addSubRegion(bottomLeftNodes)
        addSubRegion(bottomRightNodes)
        addSubRegion(topRightNodes)

        for (subregion in subregions) subregion.buildSubRegions()
    }

    private fun addSubRegion(contents: List<FaNode>) {
        if (contents.isEmpty()) return

        if (contents.size < faNodes.size) subregions.add(Region(contents))
        else for (nodeLayout in contents) subregions.add(Region(listOf(nodeLayout)))
    }

    fun applyForce(faNode: FaNode, force: ForceFactory.RepulsionForce, theta: Double) {
        if (faNodes.size < 2) force.apply(faNode, faNodes[0])
        else {
            val distance =
                sqrt((faNode.x - massCenterX) * (faNode.x - massCenterX) + (faNode.y - massCenterY) * (faNode.y - massCenterY))
            if (distance * theta > size) force.apply(faNode, this)
            else for (subregion in subregions) subregion.applyForce(faNode, force, theta)
        }
    }
}

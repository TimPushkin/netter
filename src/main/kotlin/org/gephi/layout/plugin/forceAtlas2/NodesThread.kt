package org.gephi.layout.plugin.forceAtlas2


class NodesThread(
    private val faNodes: List<FaNode>,
    private val from: Int,
    private val to: Int,
    private val barnesHutOptimize: Boolean,
    private val barnesHutTheta: Double,
    private val gravity: Double,
    private val GravityForce: ForceFactory.RepulsionForce,
    private val scaling: Double,
    private val rootRegion: Region,
    private val repulsionForce: ForceFactory.RepulsionForce
) : Runnable {

    override fun run() {
        if (barnesHutOptimize) {
            for (i in from until to) rootRegion.applyForce(faNodes[i], repulsionForce, barnesHutTheta)
        } else {
            for (i in from until to) {
                for (j in 0 until i) repulsionForce.apply(faNodes[i], faNodes[j])
            }
        }

        for (i in from until to) GravityForce.apply(faNodes[i], gravity / scaling)
    }
}


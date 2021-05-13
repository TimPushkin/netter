package ru.spbu.netter.controller.layout

import ru.spbu.netter.model.Network


interface SimpleLayoutMethod {

    fun applyLayout(
        network: Network,
        repulsion: Double = 20.0,
    )
}

interface SmartLayoutMethod {

    fun applyLayout(
        network: Network,
        loopsNum: Int = 100,
        applyOutboundAttrDistr: Boolean = false,
        applyAdjustSizes: Boolean = true,
        applyBarnesHut: Boolean = true,
        applyLinLogMode: Boolean = false,
        applyStrongGravityMode: Boolean = false,
        withJitterTolerance: Double = 1.0,
        withScalingRatio: Double = 10.0,
        withGravity: Double = 1.0,
        withBarnesHutTheta: Double = 1.2,
    )
}
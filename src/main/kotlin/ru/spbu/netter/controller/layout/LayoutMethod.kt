package ru.spbu.netter.controller.layout

import ru.spbu.netter.model.Network
import javafx.geometry.Point2D


interface SimpleLayoutMethod {

    fun calculateLayout(
        network: Network,
        repulsion: Double = 10.0,
    ): List<Point2D>
}

interface SmartLayoutMethod {

    fun calculateLayout(
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
    ): List<Point2D>
}
package org.gephi.layout.plugin.forceAtlas2

import ru.spbu.netter.model.Node


class FaNode(val node: Node) {
    var size = 1.0

    var x = ((0.01 + Math.random()) * 1000) - 500
    var y = ((0.01 + Math.random()) * 1000) - 500

    var dx = 0.0
    var dy = 0.0
    var oldDx = 0.0
    var oldDy = 0.0

    var mass = 0.0

    var isFixed = false
}

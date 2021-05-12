package org.gephi.graph.api

import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData


class Node(val id: Int, var layoutData: ForceAtlas2LayoutData?) {
    var x = 0.0
    var y = 0.0
    var size = 1.0
    var isFixed = false

    fun x() = x

    fun y() = y

    fun size() = size
}

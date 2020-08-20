package io.github.chrislo27.witnessclone.puzzle



sealed class Edge(val x: Int, val y: Int, val isVertical: Boolean) : HasHexagon {

    var posX: Float = 0f
    var posY: Float = 0f
    var startPosX: Float = 0f
    var startPosY: Float = 0f
    var endPosX: Float = 0f
    var endPosY: Float = 0f

    override var hasHexagon: Boolean = false
    override var hexagonColor: ElementColour = HasHexagon.DEFAULT_HEXAGON_COLOUR

    class Normal(x: Int, y: Int, isVertical: Boolean) : Edge(x, y, isVertical)
    class None(x: Int, y: Int, isVertical: Boolean) : Edge(x, y, isVertical)
    class Broken(x: Int, y: Int, isVertical: Boolean) : Edge(x, y, isVertical)
}

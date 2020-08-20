package io.github.chrislo27.witnessclone.puzzle


sealed class Vertex(val x: Int, val y: Int) : HasHexagon {
    
    var posX: Float = 0f
    var posY: Float = 0f
    var putCircleCorner = true
    
    override var hasHexagon: Boolean = false
    override var hexagonColor: ElementColour = HasHexagon.DEFAULT_HEXAGON_COLOUR
    
    class Normal(x: Int, y: Int) : Vertex(x, y)
    class Startpoint(x: Int, y: Int) : Vertex(x, y)
    class Endpoint(x: Int, y: Int, val endpointDir: EndpointDirection) : Vertex(x, y)
}

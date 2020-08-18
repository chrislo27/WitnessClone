package io.github.chrislo27.witnessclone.puzzle


sealed class Tile(val x: Int, val y: Int) {
    
    var posX: Float = 0f
    var posY: Float = 0f
    
    class Blank(x: Int, y: Int) : Tile(x, y)
}
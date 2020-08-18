package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.graphics.Color
import kotlin.math.max


class Puzzle(val width: Int, val height: Int,
             val material: PuzzleMaterial = PuzzleMaterial.NORMAL,
             val bgColor: Color = Color.valueOf("828C20"), val lineColor: Color = Color.valueOf("212B27"),
             val traceColor: Color = Color.valueOf("F3CF0B")) {
    val tileWidth: Int = width
    val tileHeight: Int = height
    val edgeBottomWidth: Int = width
    val edgeBottomHeight: Int = height + 1
    val edgeLeftWidth: Int = width + 1
    val edgeLeftHeight: Int = height
    val vertWidth: Int = width + 1
    val vertHeight: Int = height + 1

    val tiles: Array<Array<Tile>> = Array(tileWidth) { x -> Array(tileHeight) { y -> Tile.Blank(x, y) as Tile } }
    val vertices: Array<Array<Vertex>> = Array(vertWidth) { x -> Array(vertHeight) { y -> Vertex.Normal(x, y) as Vertex } }
    val edgesBottom: Array<Array<Edge>> = Array(edgeBottomWidth) { x -> Array(edgeBottomHeight) { y -> Edge.Normal(x, y, false) as Edge } }
    val edgesLeft: Array<Array<Edge>> = Array(edgeLeftWidth) { x -> Array(edgeLeftHeight) { y -> Edge.Normal(x, y, true) as Edge } }

    var lineThickness = 1f
    var gapX = 1f
    var gapY = 1f
    
    var currentTrace: Trace? = null

    init {
        layout()
    }

    fun layout() {
        // Lays out the tiles, vertices, and edges.

        val gap = 1f / (max(width, height) + 2)
        val usableArea = 1f - gap * 2
        val gapSizeX = usableArea / (width)
        val gapSizeY = usableArea / (height)
        gapX = gapSizeX
        gapY = gapSizeY

        lineThickness = max(gapSizeX, gapSizeY) / 6f

        for (x in 0 until vertWidth) {
            for (y in 0 until vertHeight) {
                if (x < width && y < height) {
                    tiles[x][y].run {
                        posX = gap + (gapSizeX / 2) + x * gapSizeX
                        posY = (gap + (gapSizeY / 2) + y * gapSizeY)
                    }
                }
                vertices[x][y].run {
                    posX = gap + gapSizeX * x
                    posY = (gap + gapSizeY * y)
                    // Only render the circle tex if 2 or more edges are connected
                    var edges = 0
                    // Top vertical edge
                    if (getEdgeLeftOrNull(x, y)?.takeUnless { it is Edge.None } != null)
                        edges++
                    // Bottom vertical edge
                    if (getEdgeLeftOrNull(x, y - 1)?.takeUnless { it is Edge.None } != null)
                        edges++
                    // Left horizontal edge
                    if (edges < 2 && getEdgeBottomOrNull(x - 1, y)?.takeUnless { it is Edge.None } != null)
                        edges++
                    // Right horizontal edge
                    if (edges < 2 && getEdgeBottomOrNull(x, y)?.takeUnless { it is Edge.None } != null)
                        edges++
                    putCircleCorner = edges >= 2
                }
            }
        }

        for (x in 0 until edgeBottomWidth) {
            for (y in 0 until edgeBottomHeight) {
                edgesBottom[x][y].run {
                    posX = gap + (gapSizeX / 2) + x * gapSizeX
                    posY = (gap + gapSizeY * y)
                    startPosX = gap + gapSizeX * x
                    startPosY = posY
                    endPosX = gap + gapSizeX * (x + 1)
                    endPosY = posY
                }
            }
        }
        for (x in 0 until edgeLeftWidth) {
            for (y in 0 until edgeLeftHeight) {
                edgesLeft[x][y].run {
                    posX = (gap + gapSizeX * x)
                    posY = (gap + (gapSizeY / 2) + y * gapSizeY)
                    startPosX = posX
                    startPosY = (gap + gapSizeY * y)
                    endPosX = posX
                    endPosY = (gap + gapSizeY * (y + 1))
                }
            }
        }
    }
    
    fun getTileOrNull(x: Int, y: Int): Tile? = tiles.getOrNull(x)?.getOrNull(y)
    fun getVertexOrNull(x: Int, y: Int): Vertex? = vertices.getOrNull(x)?.getOrNull(y)
    fun getEdgeBottomOrNull(x: Int, y: Int): Edge? = edgesBottom.getOrNull(x)?.getOrNull(y)
    fun getEdgeLeftOrNull(x: Int, y: Int): Edge? = edgesLeft.getOrNull(x)?.getOrNull(y)
    
    inner class Trace(startpoint: Vertex) {
        /**
         * The first item is always the startpoint and is always present.
         */
        val vertexList: MutableList<Vertex> = mutableListOf(startpoint)
        var progressDir: TraceDirection = TraceDirection.NONE
        /**
         * Measured in units
         */
        var progress: Float = 0f
        var maxProgress: Float = 1f
        /**
         * Limited = cannot go further once maxProgress is hit
         */
        var isProgressLimited: Boolean = false
        
        var pointX: Float = startpoint.posX
        var pointY: Float = startpoint.posY
    }
    
    enum class TraceDirection(val isVertical: Boolean, val isHorizontal: Boolean, val vectorX: Int, val vectorY: Int) {
        NONE(false, false, 0, 0),
        UP(true, false, 0, 1), DOWN(true, false, 0, -1),
        LEFT(false, true, -1, 0), RIGHT(false, true, 1, 0);
        
        fun isPerpendicularTo(other: TraceDirection): Boolean {
            if (this == NONE || other == NONE) return false
            return this.isVertical != other.isVertical && this.isHorizontal != other.isHorizontal
        }
        fun isParallelTo(other: TraceDirection): Boolean {
            if (this == NONE || other == NONE) return false
            return this.isVertical == other.isVertical && this.isHorizontal == other.isHorizontal
        }
    }
}
package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors


class PuzzleVerification(val puzzle: Puzzle, val trace: Puzzle.Trace) {
    
    val errorsTile: Set<Tile> = emptySet()
    val errorsEdge: Set<Edge>
    val errorsVertex: Set<Vertex>
    
    val partitions: List<Partition>
    
    val isValid: Boolean
    val timeVerified: Long = System.currentTimeMillis()
    
    val COLORS: List<Color> = Colors.getColors().values().toList()
    
    init {
        var isInvalid = false
        
        // Partition the grid.
        val traceEdges: Set<Edge> = trace.segmentList.map { it.edge }.toSet()
        val tilesToPartition: MutableMap<Tile, Partition> = linkedMapOf()
        fun partitionFill(tile: Tile): Partition {
            val usedTiles: MutableSet<Tile> = tilesToPartition.keys.toMutableSet()
            val tileSet: LinkedHashSet<Tile> = linkedSetOf()
            fun rec(t: Tile) {
                if (t !in usedTiles) {
                    usedTiles += t
                    tileSet += t
                } else return
                if (t.x > 0) {
                    if (puzzle.getEdgeLeftOrNull(t.x, t.y) !in traceEdges)
                        rec(puzzle.tiles[t.x - 1][t.y])
                }
                if (t.y > 0) {
                    if (puzzle.getEdgeBottomOrNull(t.x, t.y) !in traceEdges)
                        rec(puzzle.tiles[t.x][t.y - 1])
                }
                if (t.x < puzzle.tileWidth - 1) {
                    if (puzzle.getEdgeLeftOrNull(t.x + 1, t.y) !in traceEdges)
                        rec(puzzle.tiles[t.x + 1][t.y])
                }
                if (t.y < puzzle.tileHeight - 1) {
                    if (puzzle.getEdgeBottomOrNull(t.x, t.y + 1) !in traceEdges)
                        rec(puzzle.tiles[t.x][t.y + 1])
                }
            }
            rec(tile)
            val p = Partition(tilesToPartition.size, tileSet)
            p.tiles.forEach { t -> 
                tilesToPartition[t] = p
            }
            return p
        }
        puzzle.tiles.flatten().forEach { tile ->
            if (!tilesToPartition.containsKey(tile)) {
                partitionFill(tile)
            }
        }
        partitions = tilesToPartition.values.distinct().toList()
        
        // Check hexagons.
        val hexagonsInVertices: Set<Vertex> = puzzle.vertices.flatten().filter { it.hasHexagon }.toSet()
        val hexagonsInEdges: Set<Edge> = (puzzle.edgesBottom.flatten() + puzzle.edgesLeft.flatten()).filter { it.hasHexagon }.toSet()
        val hexagonsInTraceVerts: Set<Vertex> = trace.segmentList.map { it.end }.filter { it.hasHexagon }.toSet() + (if (trace.startpoint.hasHexagon) setOf(trace.startpoint) else emptySet())
        val hexagonsInTraceEdges: Set<Edge> = trace.segmentList.map { it.edge }.filter { it.hasHexagon }.toSet()
        val leftoverHexVert: Set<Vertex> = hexagonsInVertices - hexagonsInTraceVerts
        val leftoverHexEdge: Set<Edge> = hexagonsInEdges - hexagonsInTraceEdges
        if (leftoverHexVert.isNotEmpty() || leftoverHexEdge.isNotEmpty())
            isInvalid = true
        errorsEdge = leftoverHexEdge
        errorsVertex = leftoverHexVert
        
        isValid = !isInvalid
    }
    
}

class Partition(val index: Int, val tiles: LinkedHashSet<Tile>)

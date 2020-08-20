package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors


class PuzzleVerification(val puzzle: Puzzle, val trace: Puzzle.Trace) {
    
    val errorsTile: Set<Tile>
    val errorsEdge: Set<Edge>
    val errorsVertex: Set<Vertex>
    
    val partitions: List<Partition>
    val tilePartitions: Map<Tile, Partition>
    val vertexPartitions: Map<Vertex, Partition>
    val edgePartitions: Map<Edge, Partition>
    
    val isValid: Boolean
    val timeVerified: Long = System.currentTimeMillis()
    
    val COLORS: List<Color> = Colors.getColors().values().toList()
    
    init {
        var isInvalid = false
        
        // Partition the grid.
        val traceEdges: Set<Edge> = trace.segmentList.map { it.edge }.toSet()
        val traceVerts: Set<Vertex> = trace.segmentList.map { it.end }.toSet() + trace.startpoint
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
        edgePartitions = mutableMapOf()
        vertexPartitions = mutableMapOf()
        puzzle.tiles.flatten().forEach { tile ->
            if (!tilesToPartition.containsKey(tile)) {
                partitionFill(tile)
            }
            val thisPartition = tilesToPartition.getValue(tile)
            var v = puzzle.vertices[tile.x][tile.y]
            if (v !in vertexPartitions && v !in traceVerts)
                vertexPartitions[v] = thisPartition
            v = puzzle.vertices[tile.x + 1][tile.y]
            if (v !in vertexPartitions && v !in traceVerts)
                vertexPartitions[v] = thisPartition
            v = puzzle.vertices[tile.x][tile.y + 1]
            if (v !in vertexPartitions && v !in traceVerts)
                vertexPartitions[v] = thisPartition
            v = puzzle.vertices[tile.x + 1][tile.y + 1]
            if (v !in vertexPartitions && v !in traceVerts)
                vertexPartitions[v] = thisPartition
            var e = puzzle.edgesLeft[tile.x][tile.y]
            if (e !in edgePartitions && e !in traceEdges)
                edgePartitions[e] = thisPartition
            e = puzzle.edgesLeft[tile.x + 1][tile.y]
            if (e !in edgePartitions && e !in traceEdges)
                edgePartitions[e] = thisPartition
            e = puzzle.edgesBottom[tile.x][tile.y]
            if (e !in edgePartitions && e !in traceEdges)
                edgePartitions[e] = thisPartition
            e = puzzle.edgesBottom[tile.x][tile.y + 1]
            if (e !in edgePartitions && e !in traceEdges)
                edgePartitions[e] = thisPartition
        }
        tilePartitions = tilesToPartition.toMap()
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
        errorsTile = mutableSetOf()
        
        // Check colour blocks.
        partitions.forEach { part ->
            val colourBlocksByColor: Map<ElementColour, List<Tile.ColourBlock>> = part.tiles.filterIsInstance<Tile.ColourBlock>().groupBy { it.elementColour }
            if (colourBlocksByColor.keys.size >= 2) {
                errorsTile.addAll(colourBlocksByColor.values.flatten())
                isInvalid = true
            }
        }
        
        isValid = !isInvalid
    }
    
}

class Partition(val index: Int, val tiles: LinkedHashSet<Tile>)

package io.github.chrislo27.witnessclone.puzzle


class PuzzleVerification(val puzzle: Puzzle, val trace: Puzzle.Trace) {
    
    val errorsTile: Set<Tile> = emptySet()
    val errorsEdge: Set<Edge>
    val errorsVertex: Set<Vertex>
    val isValid: Boolean
    
    val timeVerified: Long = System.currentTimeMillis()
    
    init {
        var isInvalid = false
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

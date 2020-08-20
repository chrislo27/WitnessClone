package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt


class PuzzleHandler(val puzzle: Puzzle) : Disposable {

    companion object {
        private val TMP_MTX = Matrix4()
    }

    val bufferSize: Int = 1024
    val bufferSizef: Float = bufferSize.toFloat()
    val buffer: FrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, bufferSize, bufferSize, false, false).apply {
        this.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1f, 1f)
        update()
    }
    
    private val tmpHexColor: Color = Color(1f, 1f, 1f, 1f)

    var isTracing: Boolean = false
    
    private var timeSinceTraceStarted: Float = 0f

    private fun Vertex.drawVertex(batch: SpriteBatch, line: Float, halfLine: Float, circleTex: Texture) {
        if (this is Vertex.Startpoint) {
            batch.draw(circleTex, posX - line, posY - line, line * 2, line * 2)
        } else if (this is Vertex.Endpoint) {
            batch.draw(circleTex, posX - halfLine, posY - halfLine, line, line)
            when (this.endpointDir) {
                EndpointDirection.UP -> {
                    batch.fillRect(posX - halfLine, posY, line, line * 1.5f)
                    batch.draw(circleTex, posX - halfLine, posY + line, line, line)
                }
                EndpointDirection.DOWN -> {
                    batch.fillRect(posX - halfLine, posY, line, -line * 1.5f)
                    batch.draw(circleTex, posX - halfLine, posY - line - line, line, line)
                }
                EndpointDirection.LEFT -> {
                    batch.fillRect(posX, posY - halfLine, -line * 1.5f, line)
                    batch.draw(circleTex, posX - line - line, posY - halfLine, line, line)
                }
                EndpointDirection.RIGHT -> {
                    batch.fillRect(posX, posY - halfLine, line * 1.5f, line)
                    batch.draw(circleTex, posX + line, posY - halfLine, line, line)
                }
            }
        } else {
            if (putCircleCorner) {
                batch.draw(circleTex, posX - halfLine, posY - halfLine, line, line)
            } else {
                batch.fillRect(posX - halfLine, posY - halfLine, line, line)
            }
        }
    }

    private fun Edge.drawBottomEdge(batch: SpriteBatch, line: Float, halfLine: Float) {
        if (this is Edge.Normal) {
            batch.fillRect(startPosX, startPosY - halfLine, endPosX - startPosX, line)
        } else if (this is Edge.Broken) {
            val w = line * 2
            batch.fillRect(startPosX, startPosY - halfLine, w, line)
            batch.fillRect(endPosX - w, startPosY - halfLine, w, line)
        }
    }

    private fun Edge.drawLeftEdge(batch: SpriteBatch, line: Float, halfLine: Float) {
        if (this is Edge.Normal) {
            batch.fillRect(startPosX - halfLine, startPosY, line, endPosY - startPosY)
        } else if (this is Edge.Broken) {
            val h = line * 2
            batch.fillRect(startPosX - halfLine, startPosY, line, h)
            batch.fillRect(startPosX - halfLine, endPosY - h, line, h)
        }
    }

    /**
     * Batch must be ended before calling this function.
     */
    fun renderToBuffer(batch: SpriteBatch) {
        TMP_MTX.set(batch.projectionMatrix)
        batch.projectionMatrix = camera.combined
        buffer.begin()
        batch.begin()

        Gdx.gl.glClearColor(1f, 1f, 1f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val circleTex = AssetRegistry.get<Texture>("circle")
        val hexagonTex = AssetRegistry.get<Texture>("hexagon")
        batch.color = puzzle.bgColor
        batch.fillRect(0f, 0f, bufferSizef, bufferSizef)
        batch.color = puzzle.lineColor
        val line = puzzle.lineThickness
        val halfLine = line / 2f
        val verif = puzzle.lastVerification
        for (x in 0 until puzzle.vertWidth) {
            for (y in 0 until puzzle.vertHeight) {
                puzzle.vertices[x][y].drawVertex(batch, line, halfLine, circleTex)
            }
        }
        for (x in 0 until puzzle.edgeBottomWidth) {
            for (y in 0 until puzzle.edgeBottomHeight) {
                puzzle.edgesBottom[x][y].run {
                    drawBottomEdge(batch, line, halfLine)
                    if (this.hasHexagon) {
                        val w = (endPosX - startPosX)
                        if (verif != null && this in verif.errorsEdge) {
                            batch.color = tmpHexColor.set(this.hexagonColor).lerp(Color.RED, MathHelper.getTriangleWave(System.currentTimeMillis() - verif.timeVerified, 0.333f))
                        } else {
                            batch.color = this.hexagonColor
                        }
                        batch.draw(hexagonTex, startPosX + w / 2 - halfLine, startPosY - halfLine, line, line)
                        batch.color = puzzle.lineColor
                    }
                }
            }
        }
        for (x in 0 until puzzle.edgeLeftWidth) {
            for (y in 0 until puzzle.edgeLeftHeight) {
                puzzle.edgesLeft[x][y].run {
                    drawLeftEdge(batch, line, halfLine)
                    if (this.hasHexagon) {
                        val h = (endPosY - startPosY)
                        if (verif != null && this in verif.errorsEdge) {
                            batch.color = tmpHexColor.set(this.hexagonColor).lerp(Color.RED, MathHelper.getTriangleWave(System.currentTimeMillis() - verif.timeVerified, 0.333f))
                        } else {
                            batch.color = this.hexagonColor
                        }
                        batch.draw(hexagonTex, startPosX - halfLine, startPosY + h / 2f - halfLine, line, line)
                        batch.color = puzzle.lineColor
                    }
                }
            }
        }
        for (x in 0 until puzzle.vertWidth) {
            for (y in 0 until puzzle.vertHeight) {
                puzzle.vertices[x][y].run {
                    if (this.hasHexagon) {
                        if (verif != null && this in verif.errorsVertex) {
                            batch.color = tmpHexColor.set(this.hexagonColor).lerp(Color.RED, MathHelper.getTriangleWave(System.currentTimeMillis() - verif.timeVerified, 0.333f))
                        } else {
                            batch.color = this.hexagonColor
                        }
                        batch.draw(hexagonTex, posX - halfLine, posY - halfLine, line, line)
                        batch.color = puzzle.lineColor
                    }
                }
            }
        }
        batch.setColor(1f, 1f, 1f, 1f)

        val currentTrace = puzzle.currentTrace
        if (currentTrace != null) {
            if (currentTrace.segmentLimitation == Puzzle.TraceLimit.ENDPOINT && currentTrace.progress == currentTrace.maxProgress) {
                if (isTracing) {
                    currentTrace.mutableTraceColor.set(puzzle.traceColor).lerp(puzzle.traceDoneColor, MathHelper.getTriangleWave(System.currentTimeMillis() - currentTrace.timeWhenGlowStarted, 0.5f))
                } else if (verif != null && !verif.isValid) {
                    currentTrace.mutableTraceColor.set(puzzle.traceColor).lerp(puzzle.lineColor, ((System.currentTimeMillis() - verif.timeVerified) / 10000f).coerceIn(0f, 1f))
                }
            }
            batch.color = currentTrace.mutableTraceColor

            val startpt = currentTrace.startpoint
            batch.draw(circleTex, startpt.posX - line, startpt.posY - line, line * 2, line * 2)
            currentTrace.segmentList.forEach { segment ->
                val v: Vertex = segment.end
                batch.draw(circleTex, v.posX - halfLine, v.posY - halfLine, line, line)
                val prev = segment.start
                val useLeftEdge = v.x == prev.x
                val edge = if (useLeftEdge) {
                    puzzle.getEdgeLeftOrNull(v.x, if (prev.y > v.y) v.y else (v.y - 1))
                } else puzzle.getEdgeBottomOrNull(if (prev.x > v.x) v.x else (v.x - 1), v.y)
                if (edge != null) {
                    if (useLeftEdge) {
                        edge.drawLeftEdge(batch, line, halfLine)
                    } else {
                        edge.drawBottomEdge(batch, line, halfLine)
                    }
                }
            }
            val lastVertex: Vertex = currentTrace.lastVertex
            when (currentTrace.progressDir) {
                Puzzle.TraceDirection.UP -> {
                    batch.fillRect(lastVertex.posX - halfLine, lastVertex.posY, line, currentTrace.progress)
                }
                Puzzle.TraceDirection.DOWN -> {
                    batch.fillRect(lastVertex.posX - halfLine, lastVertex.posY - currentTrace.progress, line, currentTrace.progress)
                }
                Puzzle.TraceDirection.LEFT -> {
                    batch.fillRect(lastVertex.posX - currentTrace.progress, lastVertex.posY - halfLine, currentTrace.progress, line)
                }
                Puzzle.TraceDirection.RIGHT -> {
                    batch.fillRect(lastVertex.posX, lastVertex.posY - halfLine, currentTrace.progress, line)
                }
            }
            batch.draw(circleTex, currentTrace.pointX - halfLine, currentTrace.pointY - halfLine, line, line)

            batch.setColor(1f, 1f, 1f, 1f)
        }
        
        if (verif != null && Toolboks.debugMode) {
            verif.partitions.forEachIndexed { index, partition -> 
                val c = verif.COLORS[index % verif.COLORS.size]
                batch.setColor(c.r, c.g, c.b, 0.4f)
                partition.tiles.forEach { t ->
                    batch.fillRect(t.posX - puzzle.gapX / 2, t.posY - puzzle.gapY / 2, puzzle.gapX, puzzle.gapY)
                }
            }
        }

        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
        buffer.end()
        batch.projectionMatrix = TMP_MTX
    }

    fun renderBufferTexture(batch: SpriteBatch, x: Float, y: Float, w: Float, h: Float) {
        batch.draw(buffer.colorBufferTexture, x, y, w, h, 0, 0, buffer.width, buffer.height, false, true)
    }

    fun cancelTrace() {
        println("Cancelled trace")
        puzzle.currentTrace = null
        puzzle.lastVerification = null
        isTracing = false
        AssetRegistry.get<Sound>(puzzle.material.abortTracing).play()
    }

    fun onClick(button: Int, mouseX: Float, mouseY: Float) {
        if (!isTracing) {
            if (button == Input.Buttons.LEFT) {
                val startpoints: List<Pair<Vertex.Startpoint, Float>> = puzzle.vertices.flatMap {
                    it.filterIsInstance<Vertex.Startpoint>()
                }.map {
                    it to sqrt((mouseX - it.posX) * (mouseX - it.posX) + (mouseY - it.posY) * (mouseY - it.posY))
                }.sortedBy { it.second }
                println(startpoints.joinToString { "[${it.first.x}, ${it.first.y}]: ${it.second}" })
                if (startpoints.isNotEmpty() && mouseX in 0.0f..1.0f && mouseY in 0.0f..1.0f) {
                    val pt = startpoints.first()
                    if (pt.second <= puzzle.lineThickness) {
                        isTracing = true
                        timeSinceTraceStarted = 0f
                        puzzle.currentTrace = puzzle.Trace(pt.first)
                        puzzle.lastVerification = null
                        AssetRegistry.get<Sound>(puzzle.material.startTracing).play(0.9f, MathUtils.random(0.975f, 1.025f), 0f)
                        println("Started trace")
                    }
                }
            }
        } else {
            if (button == Input.Buttons.LEFT) {
                val trace = puzzle.currentTrace
                if (trace != null) {
                    if (trace.segmentLimitation == Puzzle.TraceLimit.ENDPOINT && trace.progress == trace.maxProgress) {
                        isTracing = false
                        trace.mutableTraceColor.set(puzzle.traceDoneColor)
                        // FIXME better validation
                        val verification = PuzzleVerification(puzzle, trace)
                        puzzle.lastVerification = verification
                        AssetRegistry.get<Sound>(if (verification.isValid) puzzle.material.success else puzzle.material.failure).play()
                    } else {
                        cancelTrace()
                    }
                }
            } else if (button == Input.Buttons.RIGHT) {
                cancelTrace()
            }
        }
    }
    
    fun update(delta: Float) {
        if (isTracing) {
            timeSinceTraceStarted += delta
        }
    }

    fun updateMouse(deltaX: Float, deltaY: Float) {
        val trace = puzzle.currentTrace
        val line = puzzle.lineThickness
        if (isTracing && trace != null) {
            var forceX = deltaX
            var forceY = deltaY

            val intendedDir: Puzzle.TraceDirection = when {
                deltaX == 0f && deltaY == 0f -> return
                deltaX < 0f && abs(deltaX) >= abs(deltaY) * 2f -> Puzzle.TraceDirection.LEFT
                deltaX > 0f && abs(deltaX) >= abs(deltaY) * 2f -> Puzzle.TraceDirection.RIGHT
                deltaY > 0f && abs(deltaY) >= abs(deltaX) * 2f -> Puzzle.TraceDirection.UP
                deltaY < 0f && abs(deltaY) >= abs(deltaX) * 2f -> Puzzle.TraceDirection.DOWN
                deltaX < 0f && abs(deltaX) >= abs(deltaY) -> Puzzle.TraceDirection.LEFT
                deltaX >= 0f && abs(deltaX) >= abs(deltaY) -> Puzzle.TraceDirection.RIGHT
                deltaY >= 0f && abs(deltaY) >= abs(deltaX) -> Puzzle.TraceDirection.UP
                deltaY < 0f && abs(deltaY) >= abs(deltaX) -> Puzzle.TraceDirection.DOWN
                else -> Puzzle.TraceDirection.UP // Shouldn't happen
            }

            // We want the opportunity to change directions as easily as possible.
            // It is unlikely that the direction will change on the first iteration.
            // By moving in the axis of concern, the trace is encouraged to move towards an intersection.
            // Then, the progress may line up to be 0 and on the next iteration, the direction may change.
            for (index in 0 until 2) {
                // When progress = 0, change direction based on the intended dir
                if (trace.progress == 0f) {
                    // Direction should change to the intended dir and max progress should be set
                    trace.progressDir = intendedDir
                    trace.segmentLimitation = Puzzle.TraceLimit.NOT_LIMITED
                    // If the last direction is the NEGATIVE DIR of the last segment, then pop the last
                    // segment and use its data
                    if (trace.segmentList.isNotEmpty() && trace.segmentList.last().dir.isNegativeTo(intendedDir)) {
                        val lastSeg = trace.segmentList.last()
                        trace.maxProgress = lastSeg.maxProgress
                        trace.progress = trace.maxProgress
                        trace.progressDir = lastSeg.dir
                        trace.segmentList.removeAt(trace.segmentList.size - 1)
                    } else {
                        val lastV = trace.lastVertex
                        val nextEdge: Edge? = if (intendedDir.isVertical) {
                            puzzle.getEdgeLeftOrNull(lastV.x, lastV.y + (if (intendedDir == Puzzle.TraceDirection.UP) 0 else -1))
                        } else {
                            puzzle.getEdgeBottomOrNull(lastV.x + (if (intendedDir == Puzzle.TraceDirection.RIGHT) 0 else -1), lastV.y)
                        }
                        if (lastV is Vertex.Endpoint && lastV.endpointDir.isSameToTraceDir(intendedDir)) {
                            // Check if this last vertex is an endpoint in the correct direction
                            trace.segmentLimitation = Puzzle.TraceLimit.ENDPOINT
                            trace.maxProgress = line * 1.5f
                        } else {
                            trace.maxProgress = when (nextEdge) {
                                null -> 0f
                                is Edge.None -> 0f
                                is Edge.Broken -> {
                                    trace.segmentLimitation = Puzzle.TraceLimit.BROKEN_EDGE
                                    line * 1.5f
                                }
                                else -> {
                                    val vertexAhead: Vertex? = if (intendedDir.isVertical) {
                                        puzzle.getVertexOrNull(lastV.x, lastV.y + (if (intendedDir == Puzzle.TraceDirection.UP) 1 else -1))
                                    } else puzzle.getVertexOrNull(lastV.x + (if (intendedDir == Puzzle.TraceDirection.RIGHT) 1 else -1), lastV.y)
                                    val subtract: Float = when {
                                        vertexAhead == null -> 0f
                                        vertexAhead == trace.startpoint -> line * 1.5f
                                        trace.segmentList.any { vertexAhead == it.end } -> line
                                        else -> 0f
                                    }
                                    if (subtract > 0f) {
                                        trace.segmentLimitation = Puzzle.TraceLimit.SELF_INTERSECTION
                                    }
                                    (if (intendedDir.isVertical) puzzle.gapY else puzzle.gapX) - subtract
                                }
                            }
                        }
                    }
                    if (trace.maxProgress == 0f && !trace.isSegmentLimited)
                        trace.segmentLimitation = Puzzle.TraceLimit.NOTHING_THERE
                }

                // Move along the segment axis. Cap out at 0 and maxProgress.
                // If hitting 0, repeat this process (continue). (The segment will be popped on the next iteration.)
                // If hitting maxProgress, add this segment to the list and repeat this process (continue).
                // Otherwise, finish.

                val axis: Puzzle.TraceDirection = trace.progressDir
                // If intendedDir is perpendicular, AND we are within a threshold of the intersection,
                // then go in the direction of the nearest intersection.
                // Otherwise, go in the intended direction.
                val intersectionThreshold = line * 2.5f
                val withinIxnThreshold = trace.progress < intersectionThreshold || (trace.maxProgress - trace.progress) < intersectionThreshold
                val realDirection: Puzzle.TraceDirection = if (intendedDir.isPerpendicularTo(axis) && trace.maxProgress > 0f) {
                    if (!withinIxnThreshold) {
                        break
                    } else (if (trace.progress / trace.maxProgress >= 0.5f) trace.progressDir else trace.progressDir.negative)
                } else intendedDir
                val isRetracting = realDirection.isNegativeTo(axis)
                if (isRetracting) {
                    // trace.progress should go to 0
                    val remaining = trace.progress
                    val totalMomentum = abs(forceX) + abs(forceY)
                    if (trace.segmentLimitation == Puzzle.TraceLimit.ENDPOINT && remaining == trace.maxProgress) {
                        trace.mutableTraceColor.set(puzzle.traceColor)
                        AssetRegistry.get<Sound>(puzzle.material.abortFinishTracing).play(0.5f)
                    }
                    if (totalMomentum >= remaining) {
                        // Subtract momentum in priority order.
                        if (axis.isVertical) {
                            // Subtract forceY first
                            if (abs(forceY) >= remaining) {
                                forceY -= sign(forceY) * remaining
                            } else {
                                val newRemaining = remaining - abs(forceY)
                                forceY = 0f
                                // Some remaining is left. Use forceX. We are guaranteed to go to 0.
                                forceX -= sign(forceX) * newRemaining
                            }
                        } else {
                            // Subtract forceX first
                            if (abs(forceX) >= remaining) {
                                forceX -= sign(forceX) * remaining
                            } else {
                                val newRemaining = remaining - abs(forceX)
                                forceX = 0f
                                // Some remaining is left. Use forceY. We are guaranteed to go to 0.
                                forceY -= sign(forceY) * newRemaining
                            }
                        }
                        trace.progress = 0f
                        continue
                    } else {
                        trace.progress -= totalMomentum
                    }
                } else {
                    // trace.progress should go to trace.maxProgress
                    val remaining = trace.maxProgress - trace.progress
                    val totalMomentum = abs(forceX) + abs(forceY)
                    if (totalMomentum >= remaining) {
                        // Subtract momentum in priority order. See retracting version for comments.
                        if (axis.isVertical) {
                            if (abs(forceY) >= remaining) {
                                forceY -= sign(forceY) * remaining
                            } else {
                                val newRemaining = remaining - abs(forceY)
                                forceY = 0f
                                forceX -= sign(forceX) * newRemaining
                            }
                        } else {
                            if (abs(forceX) >= remaining) {
                                forceX -= sign(forceX) * remaining
                            } else {
                                val newRemaining = remaining - abs(forceX)
                                forceX = 0f
                                forceY -= sign(forceY) * newRemaining
                            }
                        }
                        val lastProgress = trace.progress
                        trace.progress = trace.maxProgress
                        // Add segment to list if not limited.
                        val lastVert: Vertex = trace.lastVertex
                        val nextVertex: Vertex? = if (trace.progressDir.isVertical) {
                            puzzle.getVertexOrNull(lastVert.x, lastVert.y + (if (trace.progressDir == Puzzle.TraceDirection.UP) 1 else -1))
                        } else {
                            puzzle.getVertexOrNull(lastVert.x + (if (trace.progressDir == Puzzle.TraceDirection.RIGHT) 1 else -1), lastVert.y)
                        }
                        val edge: Edge? = if (trace.progressDir.isVertical) {
                            puzzle.getEdgeLeftOrNull(lastVert.x, lastVert.y + (if (trace.progressDir == Puzzle.TraceDirection.UP) 0 else -1))
                        } else {
                            puzzle.getEdgeBottomOrNull(lastVert.x + (if (trace.progressDir == Puzzle.TraceDirection.RIGHT) 0 else -1), lastVert.y)
                        }
                        if (!trace.isSegmentLimited && nextVertex != null && edge != null) {
                            val s = Segment(lastVert, nextVertex, edge, trace.progressDir, trace.maxProgress)
//                            println("Added segment [start=(${s.start.x}, ${s.start.y}), end=(${s.end.x}, ${s.end.y}), edge=(${s.edge.x}, ${s.edge.y}), ${s.dir  }, maxP=${s.maxProgress}]")
                            trace.segmentList.add(s)
                            trace.progress = 0f
                            // The continue and next iteration will handle the new direction
                        } else if (trace.segmentLimitation == Puzzle.TraceLimit.ENDPOINT && lastProgress != trace.progress) {
                            trace.timeWhenGlowStarted = System.currentTimeMillis()
                            AssetRegistry.get<Sound>(puzzle.material.finishTracing).play(0.5f)
                        }
                        continue
                    } else {
                        trace.progress += totalMomentum
                    }
                }

                break // Don't loop again unless one of the above conditions is met.
            }

            // Update rendering point
            val lastVert: Vertex = trace.lastVertex
            trace.pointX = lastVert.posX + (if (trace.progressDir.isHorizontal) (trace.progress * trace.progressDir.vectorX) else 0f)
            trace.pointY = lastVert.posY + (if (trace.progressDir.isVertical) (trace.progress * trace.progressDir.vectorY) else 0f)
        }
    }

    fun getDebugString(): String {
        return """isTracing: $isTracing
trace: ${puzzle.currentTrace?.run {
            """
startpt: (${startpoint.x}, ${startpoint.y})
segments: ${segmentList.size}
progressDir: $progressDir
progress: $progress / $maxProgress
limit: $segmentLimitation
point: ($pointX, $pointY)
"""
        }}
line: ${puzzle.lineThickness}
gap: (${puzzle.gapX}, ${puzzle.gapY})
"""
    }

    override fun dispose() {
        buffer.dispose()
    }
}
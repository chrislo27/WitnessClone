package io.github.chrislo27.witnessclone.puzzle.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.witnessclone.puzzle.Edge
import io.github.chrislo27.witnessclone.puzzle.EndpointDirection
import io.github.chrislo27.witnessclone.puzzle.Puzzle
import io.github.chrislo27.witnessclone.puzzle.Vertex
import kotlin.math.abs
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

    var isTracing: Boolean = false

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
                        batch.color = this.hexagonColor
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
                        batch.color = this.hexagonColor
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
                        batch.color = this.hexagonColor
                        batch.draw(hexagonTex, posX - halfLine, posY - halfLine, line, line)
                        batch.color = puzzle.lineColor
                    }
                }
            }
        }
        batch.setColor(1f, 1f, 1f, 1f)

        val currentTrace = puzzle.currentTrace
        if (currentTrace != null) {
            batch.color = puzzle.traceColor

            currentTrace.vertexList.forEachIndexed { index, v ->
                if (index == 0) {
                    batch.draw(circleTex, v.posX - line, v.posY - line, line * 2, line * 2)
                } else {
                    batch.draw(circleTex, v.posX - halfLine, v.posY - halfLine, line, line)
                    val prev = currentTrace.vertexList[index - 1]
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
            }
            val lastVertex: Vertex = currentTrace.vertexList.last()
            when (currentTrace.progressDir) {
                Puzzle.TraceDirection.NONE -> {
                }
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
        isTracing = false
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
                        puzzle.currentTrace = puzzle.Trace(pt.first)
                        println("Started trace")
                    }
                }
            }
        } else {
            if (button == Input.Buttons.LEFT) {
                // TODO check if on endpoint
            } else if (button == Input.Buttons.RIGHT) {
                cancelTrace()
            }
        }
    }

    fun updateMouse(mouseX: Float, mouseY: Float) {
        val trace = puzzle.currentTrace
        val line = puzzle.lineThickness
        if (isTracing && trace != null) {
            // Update the trace point position
            val diffX = mouseX - trace.pointX
            val diffY = mouseY - trace.pointY
            val nextDir: Puzzle.TraceDirection = when {
                diffX == 0f && diffY == 0f -> Puzzle.TraceDirection.NONE
                diffX < 0f && abs(diffX) >= abs(diffY) -> Puzzle.TraceDirection.LEFT
                diffX >= 0f && abs(diffX) >= abs(diffY) -> Puzzle.TraceDirection.RIGHT
                diffY >= 0f && abs(diffY) >= abs(diffX) -> Puzzle.TraceDirection.UP
                diffY < 0f && abs(diffY) >= abs(diffX) -> Puzzle.TraceDirection.DOWN
                else -> Puzzle.TraceDirection.NONE // Shouldn't happen
            }
            if (nextDir == Puzzle.TraceDirection.NONE)
                return
            // A vertex is added to the trace's list when the progress is reset
            // Within a quarter line width of a vertex v's intersection:
            //  - If the direction changes to be perpendicular to the PROGRESS direction, then the direction is changed
            //    and v is added to the list.
            //  - If the direction is parallel AND we're past the v point for that dir., then v is added to the list.

            fun doDirectionChange() {
                val thisVertex: Vertex = trace.vertexList.last()
//                println(trace.vertexList.size)
//                println("Last vertex: ${thisVertex.x}  ${thisVertex.y}")
                // Direction change. Check what's ahead in the nextDir direction.
                // If this CURRENT vertex is an endpoint, then:
                //  - Check that the endpoint's direction matches nextDir
                //  - Set maxProgress accordingly isProgressLimited = true
                // If there is no edge ahead then don't do anything.
                // If there is an edge e then:
                //  - If e is broken then set the maxProgress accordingly and isProgressLimited = true
                //  - Otherwise set maxProgress and isProgressLimited = false

                if (thisVertex is Vertex.Endpoint && thisVertex.endpointDir.isSameToTraceDir(nextDir)) {
                    // Travel onto the endpoint
                    trace.progress = 0f
                    trace.maxProgress = line * 1.5f
                    trace.isProgressLimited = true
                    trace.progressDir = nextDir
                } else {
                    val edgeAhead: Edge? = if (nextDir.isVertical) {
                        puzzle.getEdgeLeftOrNull(thisVertex.x, if (nextDir == Puzzle.TraceDirection.UP) thisVertex.y else (thisVertex.y - 1))
                    } else {
                        puzzle.getEdgeBottomOrNull(if (nextDir == Puzzle.TraceDirection.RIGHT) thisVertex.x else (thisVertex.x - 1), thisVertex.y)
                    }
                    if (edgeAhead != null) {
                        trace.progress = 0f
                        trace.progressDir = nextDir
//                        println("Edge ahead: ${edgeAhead.x} ${edgeAhead.y}")
                        if (edgeAhead is Edge.Broken) {
                            trace.maxProgress = line * 1.5f
                            trace.isProgressLimited = true
                        } else {
                            trace.maxProgress = if (nextDir.isVertical) puzzle.gapY else puzzle.gapX
                            trace.isProgressLimited = if (nextDir.isVertical) {
                                puzzle.getEdgeLeftOrNull(thisVertex.x, if (nextDir == Puzzle.TraceDirection.UP) (thisVertex.y + 1) else (thisVertex.y))
                            } else {
                                puzzle.getEdgeBottomOrNull(if (nextDir == Puzzle.TraceDirection.RIGHT) (thisVertex.x + 1) else (thisVertex.x), thisVertex.y)
                            } == null
                        }
                    }
                }
            }
            if (trace.progressDir == Puzzle.TraceDirection.NONE) {
                doDirectionChange()
            } else if (trace.progressDir.isParallelTo(nextDir)) {
                // Move the progress forward or backward
                val thisVertex: Vertex = trace.vertexList.last()
                val newProgress = trace.progress +
                        (if (nextDir.isVertical) diffY else diffX) * (if (nextDir == Puzzle.TraceDirection.DOWN || nextDir == Puzzle.TraceDirection.LEFT) -1 else 1) * (if (nextDir != trace.progressDir) -1 else 1)
                val vertAhead: Vertex? = if (nextDir.isVertical) {
                    puzzle.getVertexOrNull(thisVertex.x, if (nextDir == Puzzle.TraceDirection.UP) (thisVertex.y + 1) else (thisVertex.y - 1))
                } else {
                    puzzle.getVertexOrNull(if (nextDir == Puzzle.TraceDirection.RIGHT) (thisVertex.x + 1) else (thisVertex.x - 1), thisVertex.y)
                }
                if (newProgress > trace.maxProgress) {
                    // Add the next vertex if not limited
                    if (vertAhead != null && !trace.isProgressLimited) {
                        trace.progress = 0f
                        trace.progressDir = nextDir
//                        println("Vert ahead: ${vertAhead.x}  ${vertAhead.y}")
                        if (vertAhead !in trace.vertexList)
                            trace.vertexList.add(vertAhead)
                        doDirectionChange()
                    }
                } else if (newProgress < 0f) {
                    // Remove the previous vertex
                    if (trace.vertexList.size >= 2) {
                        trace.maxProgress = if (nextDir.isVertical) puzzle.gapY else puzzle.gapX
                        trace.progress = (trace.maxProgress + newProgress).coerceIn(0f, trace.maxProgress)
                        trace.isProgressLimited = false
                        trace.vertexList.removeAt(trace.vertexList.size - 1)
                    } else {
                        doDirectionChange()
                    }
                } else {
                    trace.progress = newProgress
                }
            } else if (trace.progressDir.isPerpendicularTo(nextDir)) {
                val thisVertex: Vertex = trace.vertexList.last()
                val vertAhead: Vertex? = if (trace.progressDir.isVertical) {
                    puzzle.getVertexOrNull(thisVertex.x, if (nextDir == Puzzle.TraceDirection.UP) (thisVertex.y + 1) else (thisVertex.y - 1))
                } else {
                    puzzle.getVertexOrNull(if (nextDir == Puzzle.TraceDirection.RIGHT) (thisVertex.x + 1) else (thisVertex.x - 1), thisVertex.y)
                }
//                val vertNext: Vertex? = if (vertAhead == null) null else if (trace.progressDir.isVertical) {
//                    puzzle.getVertexOrNull(vertAhead.x, if (trace.progressDir == Puzzle.TraceDirection.UP) (vertAhead.y + 1) else (vertAhead.y - 1))
//                } else {
//                    puzzle.getVertexOrNull(if (trace.progressDir == Puzzle.TraceDirection.RIGHT) (vertAhead.x + 1) else (vertAhead.x - 1), vertAhead.y)
//                }
//                if (vertAhead != null && (trace.maxProgress - trace.progress) < line / 2) {
//                    println("Attempt turn ${nextDir}")
//                    trace.progress = trace.maxProgress
//                    if (vertAhead !in trace.vertexList)
//                        trace.vertexList.add(vertAhead)
//                    doDirectionChange()
//                }
            }

            val lastVert: Vertex = trace.vertexList.last()
            trace.pointX = lastVert.posX + (if (trace.progressDir.isHorizontal) (trace.progress * trace.progressDir.vectorX) else 0f)
            trace.pointY = lastVert.posY + (if (trace.progressDir.isVertical) (trace.progress * trace.progressDir.vectorY) else 0f)
        }
    }

    fun getDebugString(): String {
        return """isTracing: $isTracing
trace: ${puzzle.currentTrace?.run {
            """
startpt: (${vertexList.first().x}, ${vertexList.first().y})
vertices: ${vertexList.size}
progressDir: $progressDir
progress: $progress / $maxProgress ${if (isProgressLimited) "(lim.)" else ""}
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
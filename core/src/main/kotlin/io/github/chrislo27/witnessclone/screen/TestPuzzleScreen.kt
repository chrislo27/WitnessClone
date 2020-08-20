package io.github.chrislo27.witnessclone.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.witnessclone.WitnessApp
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.witnessclone.puzzle.*


class TestPuzzleScreen(main: WitnessApp) : ToolboksScreen<WitnessApp, TestPuzzleScreen>(main) {

    val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
        position.x = viewportWidth / 2f
        position.y = viewportHeight / 2f
        update()
    }

    val puzzle = Puzzle(7, 7, material = PuzzleMaterial.NORMAL).apply {
        vertices[0][0] = Vertex.Startpoint(0, 0)
        vertices[1][1] = Vertex.Startpoint(1, 1)
        vertices[4][3] = Vertex.Startpoint(4, 3)
        edgesBottom[1][2] = Edge.None(1, 2, false)
        edgesBottom[1][3] = Edge.None(1, 3, false)
        edgesBottom[3][4] = Edge.None(3, 4, false)
        edgesBottom[3][5] = Edge.Broken(3, 5, false)
        edgesBottom[3][5] = Edge.Broken(3, 5, false)
        vertices[4][5].hasHexagon = true
        edgesLeft[0][0] = Edge.Broken(0, 0, true)
        edgesLeft[2][1] = Edge.None(2, 1, true)
        edgesLeft[3][1] = Edge.None(3, 1, true)
        edgesLeft[2][3] = Edge.None(2, 3, true)
        edgesLeft[3][3] = Edge.None(3, 3, true)
        edgesLeft[2][5] = Edge.None(2, 5, true)
        edgesLeft[3][5] = Edge.None(3, 5, true)
        edgesLeft[1][6] = Edge.Broken(1, 6, true)
        edgesLeft[2][2].hasHexagon = true

        edgesBottom[5][2] = Edge.None(5, 2, false)
        edgesBottom[5][3] = Edge.None(5, 3, false)
        edgesBottom[5][4] = Edge.None(5, 4, false)
        edgesBottom[5][5] = Edge.None(5, 5, false)

        edgesBottom[0][this.edgeBottomHeight - 1] = Edge.None(0, this.edgeBottomHeight - 1, false)
        
        vertices[this.vertWidth - 1][this.vertHeight - 1] = Vertex.Endpoint(this.vertWidth - 1, this.vertHeight - 1, EndpointDirection.UP)
        vertices[this.vertWidth - 1][0] = Vertex.Endpoint(this.vertWidth - 1, 0, EndpointDirection.RIGHT)
        vertices[1][0] = Vertex.Endpoint(1, 0, EndpointDirection.DOWN)
        vertices[0][1] = Vertex.Endpoint(0, 1, EndpointDirection.LEFT)
        vertices[6][0] = Vertex.Endpoint(6, 0, EndpointDirection.UP)
        edgesLeft[6][0] = Edge.None(6, 0, true)
        layout()
    }
    
    val handler = PuzzleHandler(puzzle)
    
    val deltas: MutableList<Float> = mutableListOf(0f, 0f, 0f, 0f, 0f, 0f)

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val bufSize = 700f
        val panelMouseX = (camera.getInputX() - (640f - bufSize / 2f)) / bufSize
        val panelMouseY = (camera.getInputY() - (360f - bufSize / 2f)) / bufSize
        if (handler.isTracing) {
            if (Gdx.input.isCursorCatched) {
                val dx = Gdx.input.deltaX * 3.5f
                val dy = -1 * Gdx.input.deltaY * 3.5f
                while (deltas.size < 4)
                    deltas.add(0f)
                deltas.add(dx)
                deltas.add(dy)
                while (deltas.size > 6)
                    deltas.removeAt(0)
                val avgDx = (deltas[0] + deltas[2] + deltas[4]) / 3f
                val avgDy = (deltas[1] + deltas[3] + deltas[5]) / 3f
                handler.updateMouse(avgDx / bufSize, avgDy / bufSize)
            }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                handler.onClick(Input.Buttons.LEFT, panelMouseX, panelMouseY)
                if (!handler.isTracing) {
                    Gdx.input.isCursorCatched = false
                    Gdx.input.setCursorPosition(Gdx.graphics.width / 2, Gdx.graphics.height / 2)
                }
            } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                handler.onClick(Input.Buttons.RIGHT, panelMouseX, panelMouseY)
                if (!handler.isTracing) {
                    Gdx.input.isCursorCatched = false
                    Gdx.input.setCursorPosition(Gdx.graphics.width / 2, Gdx.graphics.height / 2)
                }
            }
        } else {
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                handler.onClick(Input.Buttons.LEFT, panelMouseX, panelMouseY)
                if (handler.isTracing) {
                    Gdx.input.isCursorCatched = true
                }
            }
        }
        
        val batch = main.batch
        
        handler.renderToBuffer(batch)
        
        batch.projectionMatrix = camera.combined
        batch.begin()

        batch.setColor(0f, 1f, 1f, 1f)
        batch.drawRect(0f, 0f, 1280f, 720f, 2f)

        batch.setColor(1f, 1f, 1f, 1f)
        handler.renderBufferTexture(batch, 640f - bufSize / 2f, 360f - bufSize / 2f, bufSize, bufSize)
        
        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        
        
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            puzzle.layout()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            main.screen = TestPuzzleScreen(main)
            Gdx.app.postRunnable {
                this.dispose()
            }
        }
    }

    override fun getDebugString(): String? {
        return """deltas: $deltas
${handler.getDebugString()}
"""
    }

    override fun show() {
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        handler.dispose()
    }

}
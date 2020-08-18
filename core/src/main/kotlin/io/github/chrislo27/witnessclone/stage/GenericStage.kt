package io.github.chrislo27.witnessclone.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.witnessclone.Witness
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


open class GenericStage<S : ToolboksScreen<*, *>>(override var palette: UIPalette, parent: UIElement<S>?,
                                                  camera: OrthographicCamera) : Stage<S>(parent, camera), Palettable {

    companion object {
        const val SCREEN_WIDTH = 0.85f
        const val SCREEN_HEIGHT = 0.9f
        val AREA = (Witness.WIDTH * SCREEN_WIDTH) to (Witness.HEIGHT * SCREEN_HEIGHT)

        const val PADDING: Float = 32f
        const val ICON_SIZE: Float = 96f
        const val FONT_SCALE: Float = (ICON_SIZE / 128f)
        const val BOTTOM_SIZE: Float = 96f

        val PADDING_RATIO = (PADDING / AREA.first) to (PADDING / AREA.second)
        val ICON_SIZE_RATIO = (ICON_SIZE / AREA.first) to (ICON_SIZE / AREA.second)
        val BOTTOM_RATIO = (BOTTOM_SIZE / AREA.first) to (BOTTOM_SIZE / AREA.second)
    }

    var titleIcon: ImageLabel<S> = ImageLabel(palette, this, this).apply {
        this.alignment = Align.topLeft
        this.location.set(0f, 0f,
                          ICON_SIZE_RATIO.first, ICON_SIZE_RATIO.second,
                          0f, 0f, 0f, 0f)
        this.location.screenY += this.location.screenHeight
    }
    var titleLabel: TextLabel<S> = object : TextLabel<S>(palette, this, this) {
        override fun getFont(): BitmapFont {
            return palette.titleFont
        }
    }.apply {
        this.alignment = Align.topLeft
        this.location.set(PADDING_RATIO.first * 0.5f + ICON_SIZE_RATIO.first,
                          ICON_SIZE_RATIO.second)
        this.location.set(screenWidth = 1f - this.location.screenX, screenHeight = ICON_SIZE_RATIO.second)

        this.textAlign = Align.left + Align.center
        this.textWrapping = false
        this.background = false
        this.fontScaleMultiplier = FONT_SCALE
    }

    var centreStage: Stage<S> = Stage(this, camera).apply {
        this.alignment = Align.bottomLeft
        this.location.set(0f, BOTTOM_RATIO.second + PADDING_RATIO.second / 2f, 1f,
                          1f - (PADDING_RATIO.second / 2f + ICON_SIZE_RATIO.second) - (PADDING_RATIO.second / 2f + BOTTOM_RATIO.second))
    }

    var bottomStage: Stage<S> = Stage(this, camera).apply {
        this.alignment = Align.bottomLeft
        this.location.set(0f, 0f, 1f, BOTTOM_RATIO.second)
    }

    var onBackButtonClick: () -> Unit = {}
    val backButton: Button<S> = object : Button<S>(palette, bottomStage, bottomStage) {
        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            onBackButtonClick()
        }
    }.apply {
        this.visible = false
        this.stage.updatePositions()
        this.location.set(screenHeight = 1f)
        this.location.set(screenWidth = this.stage.percentageOfWidth(this.stage.location.realHeight))
        this.addLabel(ImageLabel(palette, this, this.stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_back"))
        })
        bottomStage.elements += this
    }

    init {
        this.location.screenWidth = SCREEN_WIDTH - PADDING_RATIO.first * 2
        this.location.screenHeight = SCREEN_HEIGHT - PADDING_RATIO.second * 2
        this.location.screenX = 0.5f - this.location.screenWidth / 2f
        this.location.screenY = 0.5f - this.location.screenHeight / 2f

        elements.apply {
            add(bottomStage)
            add(centreStage)
            add(titleIcon)
            add(titleLabel)
        }
    
        tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.85f), fontScale = 0.75f), this, this).apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.center
        }
    
        this.updatePositions()
    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        val oldColor: Float = batch.packedColor
        batch.setColor(0f, 0f, 0f, 0.65f)
        batch.fillRect(location.realX - PADDING_RATIO.first * camera.viewportWidth,
                       location.realY - PADDING_RATIO.second * camera.viewportHeight,
                       location.realWidth + PADDING_RATIO.first * camera.viewportWidth * 2,
                       location.realHeight + PADDING_RATIO.second * camera.viewportHeight * 2)
        batch.setColor(1f, 1f, 1f, 1f)
        val height = (8f / Witness.WIDTH) * camera.viewportHeight
        batch.fillRect(location.realX + height,
                       centreStage.location.realY - PADDING_RATIO.second * camera.viewportHeight / 4f,
                       location.realWidth - height * 2,
                       height)

        batch.fillRect(location.realX + height,
                       titleLabel.location.realY - PADDING_RATIO.second * camera.viewportHeight / 4f,
                       location.realWidth - height * 2,
                       height)
        batch.packedColor = oldColor
        super.render(screen, batch, shapeRenderer)
    }
}
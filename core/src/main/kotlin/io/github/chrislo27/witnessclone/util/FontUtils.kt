package io.github.chrislo27.witnessclone.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.github.chrislo27.witnessclone.WitnessApp


fun BitmapFont.scaleFont(camera: OrthographicCamera) {
    this.setUseIntegerPositions(false)
    this.data.setScale(camera.viewportWidth / WitnessApp.instance.defaultCamera.viewportWidth,
                       camera.viewportHeight / WitnessApp.instance.defaultCamera.viewportHeight)
}

fun BitmapFont.unscaleFont() {
    this.setUseIntegerPositions(true)
    this.data.setScale(1f)
}
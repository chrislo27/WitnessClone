package io.github.chrislo27.witnessclone

import com.badlogic.gdx.Preferences


class Settings(private val main: WitnessApp) {
    
    private val preferences: Preferences get() = main.preferences
    
    fun load() {
    }
    
    fun persist() {
        preferences
                .flush()
    }
}
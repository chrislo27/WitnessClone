package io.github.chrislo27.witnessclone

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.version.Version


object Witness {

    const val TITLE = "WitnessClone"
    val VERSION: Version = Version(1, 0, 0, "DEVELOPMENT")
    val EXPERIMENTAL: Boolean = VERSION.suffix.matches("DEVELOPMENT|SNAPSHOT(?:.)*|RC\\d+".toRegex())
    const val WIDTH = 1280
    const val HEIGHT = 720
    val DEFAULT_SIZE = WIDTH to HEIGHT
    val MINIMUM_SIZE: Pair<Int, Int> = 640 to 360
    val MK_FOLDER: FileHandle by lazy { Gdx.files.external(".witnessclone/").apply(FileHandle::mkdirs) }

    val tmpMusic: FileHandle by lazy {
        MK_FOLDER.child("tmpMusic/").apply {
            mkdirs()
        }
    }

    const val GITHUB: String = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
    var logMissingLocalizations: Boolean = false
    lateinit var launchArguments: List<String>

}
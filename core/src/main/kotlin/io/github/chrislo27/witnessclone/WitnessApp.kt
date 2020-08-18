package io.github.chrislo27.witnessclone

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.witnessclone.init.DefaultAssetLoader
import io.github.chrislo27.witnessclone.screen.*
import io.github.chrislo27.toolboks.ResizeAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.CloseListener
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import io.github.chrislo27.toolboks.version.Version
import org.lwjgl.glfw.GLFW
import java.io.File
import java.util.*


class WitnessApp(logger: Logger, logToFile: File?)
    : ToolboksGame(logger, logToFile, Witness.VERSION, Witness.DEFAULT_SIZE, ResizeAction.KEEP_ASPECT_RATIO, Witness.MINIMUM_SIZE), CloseListener {

    companion object {
        lateinit var instance: WitnessApp
            private set

        private val TMP_MATRIX = Matrix4()
        private const val RAINBOW_STR = "RAINBOW"

        init {
            Colors.put("X", Color.CLEAR)
            Colors.put("PICOSONG", Color.valueOf("26AB57"))
        }
    }

    val defaultFontLargeKey = "default_font_large"
    val defaultFontMediumKey = "default_font_medium"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"
    val timeSignatureFontKey = "time_signature"

    val defaultFontFTF: FreeTypeFont
        get() = fonts[defaultFontKey]
    val defaultBorderedFontFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontKey]
    val defaultFontLargeFTF: FreeTypeFont
        get() = fonts[defaultFontLargeKey]
    val defaultFontMediumFTF: FreeTypeFont
        get() = fonts[defaultFontMediumKey]
    val defaultBorderedFontLargeFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontLargeKey]
    val timeSignatureFontFTF: FreeTypeFont
        get() = fonts[timeSignatureFontKey]

    val defaultFontLarge: BitmapFont
        get() = defaultFontLargeFTF.font!!
    val defaultFontMedium: BitmapFont
        get() = defaultFontMediumFTF.font!!
    val defaultBorderedFontLarge: BitmapFont
        get() = defaultBorderedFontLargeFTF.font!!
    val timeSignatureFont: BitmapFont
        get() = timeSignatureFontFTF.font!!

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin_lat_cy_ja_ko_spec.ttf") }
    private val fontAfterLoadFunction: FreeTypeFont.() -> Unit = {
        this.font!!.apply {
            setFixedWidthGlyphs("1234567890")
            data.setLineHeight(lineHeight * 0.9f)
            setUseIntegerPositions(true)
            data.markupEnabled = true
            data.missingGlyph = data.getGlyph('☒')
        }
    }

    val uiPalette: UIPalette by lazy {
        UIPalette(defaultFontFTF, defaultFontLargeFTF, 1f,
                  Color(1f, 1f, 1f, 1f),
                  Color(0f, 0f, 0f, 0.75f),
                  Color(0.25f, 0.25f, 0.25f, 0.75f),
                  Color(0f, 0.5f, 0.5f, 0.75f))
    }

    lateinit var preferences: Preferences
        private set

    var versionTextWidth: Float = -1f
        private set

    @Volatile
    var githubVersion: Version = Version.RETRIEVING
        private set
    var secondsElapsed: Float = 0f
        private set

    @Volatile
    var liveUsers: Int = -1
        private set

    val settings: Settings = Settings(this)
    private var lastWindowed: Pair<Int, Int> = Witness.DEFAULT_SIZE.copy()

    private val rainbowColor: Color = Color(1f, 1f, 1f, 1f)

    lateinit var hueBar: Texture
        private set

    override val programLaunchArguments: List<String>
        get() = Witness.launchArguments

    override fun getTitle(): String =
            "${Witness.TITLE} $versionString"

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("${Witness.TITLE} $versionString is starting...")
        val javaVersion = System.getProperty("java.version").trim()
        Toolboks.LOGGER.info("Running on JRE $javaVersion")

        instance = this

        val windowHandle = (Gdx.graphics as Lwjgl3Graphics).window.windowHandle
        GLFW.glfwSetWindowAspectRatio(windowHandle, 16, 9)

        // localization stuff
        run {
            Localization.loadBundlesFromLangFile()
            if (Witness.logMissingLocalizations) {
                Localization.logMissingLocalizations()
            }
        }

        // font stuff
        run {
            fonts[defaultFontLargeKey] = createDefaultLargeFont()
            fonts[defaultFontMediumKey] = createDefaultMediumFont()
            fonts[defaultBorderedFontLargeKey] = createDefaultLargeBorderedFont()
            fonts[timeSignatureFontKey] = FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter().apply {
                size *= 6
                characters = "0123456789-"
                incremental = false
            }).setAfterLoad {
                this.font!!.apply {
                    setFixedWidthGlyphs("0123456789")
                }
            }
            fonts.loadUnloaded(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
            Toolboks.LOGGER.info("Loaded fonts (initial)")
        }
        
        // Generate hue bar
        run {
            val pixmap = Pixmap(360, 1, Pixmap.Format.RGBA8888)
            val tmpColor = Color(1f, 1f, 1f, 1f)
            for (i in 0 until 360) {
                tmpColor.fromHsv(i.toFloat(), 1f, 1f)
                pixmap.setColor(tmpColor)
                pixmap.drawPixel(i, 0)
            }
            hueBar = Texture(pixmap).apply {
                this.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
            Toolboks.LOGGER.info("Generated hue bar texture")
        }

        // preferences
        preferences = Gdx.app.getPreferences("MountainKing")
        Toolboks.LOGGER.info("Loaded preferences")

        val lastVersion = Version.fromStringOrNull(preferences.getString(PreferenceKeys.LAST_VERSION, null) ?: "")

        settings.load()
        Toolboks.LOGGER.info("Loaded settings instance")
        Toolboks.LOGGER.info("Loaded persistent data from preferences")

        preferences.flush()

        // Asset registry
        AssetRegistry.addAssetLoader(DefaultAssetLoader())

        // screens
        run {
            ScreenRegistry += "assetLoad" to AssetRegistryLoadingScreen(this)

            fun addOtherScreens() {

            }

            val nextScreenLambda: (() -> ToolboksScreen<*, *>?) = nextScreenLambda@{
                defaultCamera.viewportWidth = Witness.WIDTH.toFloat()
                defaultCamera.viewportHeight = Witness.HEIGHT.toFloat()
                defaultCamera.update()

                addOtherScreens()
                loadWindowSettings()
                dontShowResizeInfo = false
                // TODO
                val nextScreen = TestPuzzleScreen(this)
//                if (preferences.getString(PreferenceKeys.LAST_VERSION, null) == null) {
//                    Gdx.net.openURI("https://rhre.readthedocs.io/en/latest/")
//                }
                return@nextScreenLambda nextScreen
            }
            setScreen(ScreenRegistry.getNonNullAsType<AssetRegistryLoadingScreen>("assetLoad")
                              .setNextScreen(nextScreenLambda))
        }
    }

    override fun preRender() {
        secondsElapsed += Gdx.graphics.deltaTime
        rainbowColor.fromHsv(MathHelper.getSawtoothWave(2f) * 360f, 0.8f, 0.8f)
        Colors.put(RAINBOW_STR, rainbowColor)
        super.preRender()
    }

    private var timeSinceResize: Float = 2f
    private var dontShowResizeInfo = true

    override fun postRender() {
        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = defaultCamera.combined
        batch.begin()

        if (timeSinceResize < 1.5f && !dontShowResizeInfo) {
            val font = defaultBorderedFont
            font.setColor(1f, 1f, 1f, 1f)
            font.draw(batch, "${Gdx.graphics.width}x${Gdx.graphics.height}",
                      0f,
                      defaultCamera.viewportHeight * 0.5f + font.capHeight,
                      defaultCamera.viewportWidth, Align.center, false)
        }
        timeSinceResize += Gdx.graphics.deltaTime

        batch.end()
        batch.projectionMatrix = TMP_MATRIX
        super.postRender()
    }

    override fun dispose() {
        super.dispose()
        settings.persist()
        persistWindowSettings()
        Witness.tmpMusic.emptyDirectory()
    }

    fun persistWindowSettings() {
        val isFullscreen = Gdx.graphics.isFullscreen
        if (isFullscreen) {
            preferences.putString(PreferenceKeys.WINDOW_STATE, "fs")
        } else {
            preferences.putString(PreferenceKeys.WINDOW_STATE, "${Gdx.graphics.width}x${Gdx.graphics.height}")
        }

        Toolboks.LOGGER.info("Persisting window settings as ${preferences.getString(PreferenceKeys.WINDOW_STATE)}")

        preferences.flush()
    }

    fun loadWindowSettings() {
        val str: String = preferences.getString(PreferenceKeys.WINDOW_STATE,
                                                "${Witness.WIDTH}x${Witness.HEIGHT}").toLowerCase(Locale.ROOT)
        if (str == "fs") {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            val width: Int
            val height: Int
            if (!str.matches("\\d+x\\d+".toRegex())) {
                width = Witness.WIDTH
                height = Witness.HEIGHT
            } else {
                width = str.substringBefore('x').toIntOrNull()?.coerceAtLeast(160) ?: Witness.WIDTH
                height = str.substringAfter('x').toIntOrNull()?.coerceAtLeast(90) ?: Witness.HEIGHT
            }

            Gdx.graphics.setWindowedMode(width, height)
        }
    }

    fun attemptFullscreen() {
        lastWindowed = Gdx.graphics.width to Gdx.graphics.height
        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
    }

    fun attemptEndFullscreen() {
        val last = lastWindowed
        Gdx.graphics.setWindowedMode(last.first, last.second)
    }

    fun attemptResetWindow() {
        Gdx.graphics.setWindowedMode(Witness.DEFAULT_SIZE.first, Witness.DEFAULT_SIZE.second)
    }

    override fun keyDown(keycode: Int): Boolean {
        val res = super.keyDown(keycode)
        if (!res) {
            if (!Gdx.input.isControlDown() && !Gdx.input.isAltDown()) {
                if (keycode == Input.Keys.F11) {
                    if (!Gdx.input.isShiftDown()) {
                        if (Gdx.graphics.isFullscreen) {
                            attemptEndFullscreen()
                        } else {
                            attemptFullscreen()
                        }
                    } else {
                        attemptResetWindow()
                    }
                    persistWindowSettings()
                    return true
                }
            }
        }
        return res
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        if (!dontShowResizeInfo) timeSinceResize = 0f
    }

    private fun createDefaultTTFParameter(): FreeTypeFontGenerator.FreeTypeFontParameter {
        return FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            magFilter = Texture.TextureFilter.Linear
            minFilter = Texture.TextureFilter.Linear
            genMipMaps = false
            incremental = true
            size = 24
            color = Color(1f, 1f, 1f, 1f)
            borderColor = Color(0f, 0f, 0f, 1f)
            characters = ""
            hinting = FreeTypeFontGenerator.Hinting.AutoFull
        }
    }

    override fun createDefaultFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter())
                .setAfterLoad(fontAfterLoadFunction)
    }

    override fun createDefaultBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultMediumFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    size *= 2
                    borderWidth *= 2
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f

                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    override fun attemptClose(): Boolean {
        return true
    }
}
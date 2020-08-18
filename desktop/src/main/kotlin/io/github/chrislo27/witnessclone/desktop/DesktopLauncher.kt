package io.github.chrislo27.witnessclone.desktop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import io.github.chrislo27.witnessclone.Witness
import io.github.chrislo27.witnessclone.WitnessApp
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher3
import io.github.chrislo27.toolboks.logging.Logger
import java.io.File

object DesktopLauncher {
    
    private fun printHelp(jCommander: JCommander) {
        println("${Witness.TITLE} ${Witness.VERSION}\n${Witness.GITHUB}\n\n${StringBuilder().apply { jCommander.usage(this) }}")
    }
    
    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/chrislo27/RhythmHeavenRemixEditor/issues/273
        System.setProperty("jna.nosys", "true")
        
        Witness.launchArguments = args.toList()
        
        try {
            // Check for bad arguments but don't cause a full crash
            JCommander.newBuilder().acceptUnknownOptions(false).addObject(Arguments()).build().parse(*args)
        } catch (e: ParameterException) {
            println("WARNING: Failed to parse arguments. Check below for details and help documentation. You may have strange parse results from ignoring unknown options.\n")
            e.printStackTrace()
            println("\n\n")
            printHelp(JCommander(Arguments()))
            println("\n\n")
        }
        
        val arguments = Arguments()
        val jcommander = JCommander.newBuilder().acceptUnknownOptions(true).addObject(arguments).build()
        jcommander.parse(*args)
        
        if (arguments.printHelp) {
            printHelp(jcommander)
            return
        }
        
        val logger = Logger()
        val app = WitnessApp(logger, File(System.getProperty("user.home") + "/.mtnking/logs/"))
        ToolboksDesktopLauncher3(app)
                .editConfig {
                    this.setAutoIconify(true)
                    this.setWindowedMode(app.emulatedSize.first, app.emulatedSize.second)
                    this.setWindowSizeLimits(Witness.MINIMUM_SIZE.first, Witness.MINIMUM_SIZE.second, -1, -1)
                    this.setTitle(app.getTitle())
                    this.setIdleFPS(arguments.fps.coerceAtLeast(30))
                    this.setResizable(true)
                    this.useVsync(arguments.fps <= 60)
                    this.setInitialBackgroundColor(Color(0f, 0f, 0f, 1f))
                    this.setAudioConfig(100, 16384, 32)
                    this.setHdpiMode(HdpiMode.Logical)
                    
                    Witness.logMissingLocalizations = arguments.logMissingLocalizations
                    
                    this.setWindowIcon()
                    val sizes: List<Int> = listOf(256, 128, 64, 32, 24, 16)
//                    this.setWindowIcon(Files.FileType.Internal, *sizes.map { "images/icon/$it.png" }.toTypedArray())
                }
                .launch()
    }
    
}

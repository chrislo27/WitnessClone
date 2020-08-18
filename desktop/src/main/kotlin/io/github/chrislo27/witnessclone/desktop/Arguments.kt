package io.github.chrislo27.witnessclone.desktop

import com.beust.jcommander.Parameter

class Arguments {
    
    @Parameter(names = ["--help", "-h", "-?"], description = "Prints this usage menu.", help = true)
    var printHelp: Boolean = false
    
    // -----------------------------------------------------------
    
    @Parameter(names = ["--fps"], description = "Manually sets the target FPS. Will always be at least 30.")
    var fps: Int = 60
    
    @Parameter(names = ["--log-missing-localizations"], description = "Logs any missing localizations. Other locales are checked against the default properties file.")
    var logMissingLocalizations: Boolean = false
    
    
}
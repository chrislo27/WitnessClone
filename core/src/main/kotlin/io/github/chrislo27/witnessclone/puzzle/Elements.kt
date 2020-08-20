package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.graphics.Color


interface HasHexagon {
    companion object {
        val DEFAULT_HEXAGON_COLOUR: ElementColour = ElementColour.GREY
    }

    var hasHexagon: Boolean
    var hexagonColor: ElementColour
}

enum class EndpointDirection {
    UP, DOWN, LEFT, RIGHT;

    fun isSameToTraceDir(trace: Puzzle.TraceDirection): Boolean {
        return when (this) {
            UP -> trace == Puzzle.TraceDirection.UP
            DOWN -> trace == Puzzle.TraceDirection.DOWN
            LEFT -> trace == Puzzle.TraceDirection.LEFT
            RIGHT -> trace == Puzzle.TraceDirection.RIGHT
        }
    }
}

enum class ElementColour(val color: Color) {
    WHITE(Color.valueOf("ffffff")), BLACK(Color.valueOf("000000")), GREY(Color.valueOf("505050")),
    /*RED(Color.valueOf("ff0000")),*/ GREEN(Color.valueOf("00ff00")), BLUE(Color.valueOf("0000ff")),
    CYAN(Color.valueOf("00ffff")), MAGENTA(Color.valueOf("ff00ff")), YELLOW(Color.valueOf("ffff00")),
    ORANGE(Color.valueOf("ff7000"))
}

interface HasElementColour {
    val elementColour: ElementColour
}

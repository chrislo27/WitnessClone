package io.github.chrislo27.witnessclone.puzzle

import com.badlogic.gdx.graphics.Color


interface HasHexagon {
    companion object {
        val HEXAGON_COLOR: Color = Color.valueOf("4B4C49")
    }

    var hasHexagon: Boolean
    var hexagonColor: Color
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

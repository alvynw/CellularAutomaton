package gol

import kotlinx.coroutines.*
import processing.awt.PSurfaceAWT
import processing.core.*
import java.awt.Toolkit
import javax.swing.JFrame
import kotlin.coroutines.EmptyCoroutineContext

class GameOfLife(
    private val visibleBoardWidth: Int = Toolkit.getDefaultToolkit().screenSize.width,
    private val visibleBoardHeight: Int = Toolkit.getDefaultToolkit().screenSize.height,
    private val cellSize: Int = 5,
    private val fps: Float = 30f,
    private val actualBoardWidthCells: Int = 5000,
    private val actualBoardHeightCells: Int = 5000
) : PApplet(), CoroutineScope by CoroutineScope(EmptyCoroutineContext) {

    private val BLACK = color(0, 0, 0)
    private val WHITE = color(255, 255, 255)

    private val visibleBoardWidthCells = visibleBoardWidth / cellSize
    private val visibleBoardHeightCells = visibleBoardHeight / cellSize

    private var visibleBoardCellY = visibleBoardHeightCells / 2
    private var visibleBoardCellX = visibleBoardWidthCells / 2

    private var grid =
        Array(actualBoardHeightCells) { Array(actualBoardWidthCells) { /*if (Math.random() > 0.5) 1 else 0*/ 0 } }

    override fun settings() {
        size(visibleBoardWidth - 1, visibleBoardHeight - 1)
    }

    override fun setup() {
        frameRate(fps)
        colorMode(RGB)
    }

    override fun draw() {
        background(WHITE)
        setNextGeneration()
        displayGeneration()
    }

    override fun mouseDragged() {
        grid[r(visibleBoardCellY + mouseY / cellSize)][c(visibleBoardCellX + mouseX / cellSize)] = 1
    }

    override fun keyPressed() {
        if (key.toInt() == CODED) {
            when (keyCode) {
                UP -> visibleBoardCellY--
                DOWN -> visibleBoardCellY++
                LEFT -> visibleBoardCellX--
                RIGHT -> visibleBoardCellX++
            }
        }
    }

    private fun displayGeneration() {
        stroke(BLACK)
        fill(BLACK)

        synchronized(grid) {
            for (row: Int in visibleBoardCellY until visibleBoardCellY + visibleBoardHeightCells) {
                for (col: Int in visibleBoardCellX until visibleBoardCellX + visibleBoardWidthCells) {
                    if (grid[r(row)][c(col)] == 1) {
                        rect(
                            (col - visibleBoardCellX) * cellSize.toFloat(),
                            (row - visibleBoardCellY) * cellSize.toFloat(),
                            cellSize.toFloat(),
                            cellSize.toFloat()
                        )
                    }
                }
            }
        }
    }

    private fun setNextGeneration() {
        val newGrid = Array(actualBoardHeightCells) { Array(actualBoardWidthCells) { 0 } }

        val threads = mutableListOf<Thread>()

        for (row: Int in 0 until actualBoardHeightCells step visibleBoardHeightCells) {
            for (col: Int in 0 until actualBoardWidthCells step visibleBoardWidthCells) {
                threads += kotlin.concurrent.thread {
                    for (r: Int in row until min(row + visibleBoardHeightCells, actualBoardHeightCells)) {
                        for (c: Int in col until min(col + visibleBoardWidthCells, actualBoardWidthCells)) {
                            val numNeighbors = numberOfNeighbors(r, c)
                            when {
                                grid[r][c] == 1 ->
                                    when {
                                        numNeighbors < 2 -> newGrid[r][c] = 0
                                        numNeighbors <= 3 -> newGrid[r][c] = 1
                                        else -> newGrid[r][c] = 0
                                    }
                                numNeighbors == 3 -> newGrid[r][c] = 1
                                else -> newGrid[r][c] = grid[r][c]
                            }
                        }
                    }
                }
            }
        }

        while(threads.any { it.state != Thread.State.TERMINATED }) {  }

        synchronized(grid) {
            grid = newGrid
        }
    }

    private fun r(row: Int): Int {
        val x = row % actualBoardHeightCells
        return if (x >= 0) x else x + actualBoardHeightCells
    }

    private fun c(col: Int): Int {
        val x = col % actualBoardWidthCells
        return if (x >= 0) x else x + actualBoardWidthCells
    }

    private fun numberOfNeighbors(row: Int, col: Int): Int {
        return grid[r(row - 1)][c(col - 1)] + grid[r(row - 1)][c(col)] + grid[r(row - 1)][c(col + 1)] +
                grid[r(row)][c(col - 1)] + grid[r(row)][c(col + 1)] +
                grid[r(row + 1)][c(col - 1)] + grid[r(row + 1)][c(col)] + grid[r(row + 1)][c(col + 1)]
    }

    fun initializeSurface(): PSurface {
        val ps = initSurface()
        ps.setSize(visibleBoardWidth, visibleBoardHeight)
        return ps
    }

}

fun main(args: Array<String>) {

    val frame = JFrame("Game of Life")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;

    val gol = GameOfLife()

    val golPs = gol.initializeSurface()

    val smoothCanvas = golPs.native as PSurfaceAWT.SmoothCanvas

    frame.add(smoothCanvas)

    frame.pack()
    frame.isVisible = true

    golPs.startThread()
}




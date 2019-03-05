package gol

import processing.awt.PSurfaceAWT
import processing.core.*
import javax.swing.JFrame

class GameOfLife(private val boardWidth: Int = 100,
                 private val boardHeight: Int = 100,
                 private val cellSize: Int = 5,
                 private val fps: Float = 30f) : PApplet() {

    private val BLACK = color(0, 0, 0)
    private val WHITE = color(255, 255, 255)

    private var grid = Array(boardHeight) { Array(boardWidth) { 0 } }

    init {
        grid[5][5] = 1
    }

    override fun settings() {
        size(boardWidth * cellSize - 1, boardHeight * cellSize - 1)
    }

    override fun setup() {
        frameRate(fps)
        colorMode(RGB)
    }

    override fun draw() {

        setNextGeneration()

        background(WHITE)

        displayGeneration()
    }

    override fun mouseDragged() {
        grid[r(mouseY / cellSize)][c(mouseX / cellSize)] = 1
    }

    private fun displayGeneration() {

        stroke(BLACK)
        fill(BLACK)

        grid.forEachIndexed { row, cols->
            cols.forEachIndexed { col, _ ->
                if (grid[row][col] == 1) {
                    rect(col * cellSize.toFloat(), row * cellSize.toFloat(), cellSize.toFloat(), cellSize.toFloat())
                }
            }
        }
    }

    private fun setNextGeneration() {

        val newGrid = Array(boardWidth) { Array(boardHeight) { 0 } }

        newGrid.forEachIndexed { row, cols ->
            cols.forEachIndexed { col, _ ->
                val numNeighbors = numberOfNeighbors(row, col)
                when {
                    grid[row][col] == 1 ->
                        when {
                            numNeighbors < 2 -> newGrid[row][col] = 0
                            numNeighbors <= 3 -> newGrid[row][col] = 1
                            else -> newGrid[row][col] = 0
                        }
                    numNeighbors == 3 -> newGrid[row][col] = 1
                    else -> newGrid[row][col] = grid[row][col]
                }
            }
        }

        grid = newGrid
    }

    private fun r(row: Int): Int {
        val x = row % boardHeight
        return if (x >= 0) x else x + boardHeight
    }
    private fun c(col: Int): Int {
        val x = col % boardWidth
        return if (x >= 0) x else x + boardWidth
    }

    private fun numberOfNeighbors(row: Int, col: Int): Int {
        return  grid[r(row - 1)][c(col - 1)] + grid[r(row - 1)][c(col)] + grid[r(row - 1)][c(col + 1)] +
                grid[r(row)][c(col - 1)] +                                          grid[r(row)][c(col + 1)] +
                grid[r(row + 1)][c(col - 1)] + grid[r(row + 1)][c(col)] + grid[r(row + 1)][c(col + 1)]
    }

    fun initSurfaze(): PSurface {
        val ps = initSurface()
        ps.setSize(boardWidth * cellSize, boardHeight * cellSize)
        return ps
    }

}

fun main(args: Array<String>) {

    val frame = JFrame("Game of Life")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;

    val gol = GameOfLife()

    val golPs = gol.initSurfaze()

    val smoothCanvas = golPs.native as PSurfaceAWT.SmoothCanvas

    frame.add(smoothCanvas)

    frame.pack()
    frame.isVisible = true

    //start your sketch
    golPs.startThread()
}




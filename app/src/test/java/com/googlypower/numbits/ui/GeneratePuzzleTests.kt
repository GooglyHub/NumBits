package com.googlypower.numbits.ui

import org.junit.Test
import java.time.LocalDate

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GeneratePuzzleTests {
    @Test
    fun generateEasyPuzzles() {
        var day = LocalDate.of(2025, 12, 1)
        repeat(10000) {
            val puzzle = generatePuzzle(day, Difficulty.Easy)
            println("$day $puzzle")
            day = day.plusDays(1)
        }
    }

    @Test
    fun generateMediumPuzzles() {
        var day = LocalDate.of(2025, 12, 1)
        repeat(10000) {
            val puzzle = generatePuzzle(day, Difficulty.Medium)
            println("$day $puzzle")
            day = day.plusDays(1)
        }
    }

    @Test
    fun generateHardPuzzles() {
        var day = LocalDate.of(2025, 12, 1)
        repeat(10000) {
            val puzzle = generatePuzzle(day, Difficulty.Hard)
            println("$day $puzzle")
            day = day.plusDays(1)
        }
    }
}
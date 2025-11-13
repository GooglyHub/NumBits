package com.googlypower.numbits.ui

const val UNKNOWN = '?'

enum class GameState {
    SOLVING,
    PASSED,
    FAILED
}

enum class Difficulty {
    Easy,
    Medium,
    Hard
}

val ALL_DIFFICULTIES = listOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard)

data class GameUiState(
    val difficulty: Difficulty = Difficulty.Easy,
    val availableDifficulties: List<Difficulty> = ALL_DIFFICULTIES,
    val leftExpression: String = "",
    val rightExpression: String = "",
    val leftSubstitutions: String = "",
    val rightSubstitutions: String = "",
    val reserves: String = "",
    val state: GameState = GameState.SOLVING,
    val timeToSolve: String = ""
)
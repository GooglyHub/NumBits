package com.googlypower.numbits.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.googlypower.numbits.NumBitsApplication
import com.googlypower.numbits.data.Puzzle
import com.googlypower.numbits.data.SavedProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import com.googlypower.numbits.data.SavedProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit


class GameViewModel(
    private val savedProgressRepository: SavedProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val today : LocalDate = LocalDate.now()
    var gameId = 0

    var startTime: Long = 0

    init {
        val base = LocalDate.of(2025, 11, 15)
        val daysBetween = base.until(today, ChronoUnit.DAYS)
        gameId = daysBetween.toInt()

        determineWhichPuzzleToShow()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as NumBitsApplication)
                GameViewModel(application.savedProgressRepository)
            }
        }
    }
    fun isSolving() : Boolean {
        return (_uiState.value.leftSubstitutions.contains('?')
            || _uiState.value.rightSubstitutions.contains('?'))
    }

    fun getState() : GameState {
        if (isSolving()) {
            return GameState.SOLVING
        }
        val lhs = evaluate(substitute(_uiState.value.leftExpression, _uiState.value.leftSubstitutions))
        val rhs = evaluate(substitute(_uiState.value.rightExpression, _uiState.value.rightSubstitutions))
        if (lhs == rhs) {
            return GameState.PASSED
        }
        return GameState.FAILED
    }

    fun substitute(expression: String, substitutions: String) : String {
        val s = StringBuilder()
        var subIdx = 0
        for (ch in expression) {
            if (ch == UNKNOWN) {
                s.append(substitutions[subIdx])
                subIdx++
            } else {
                s.append(ch)
            }
        }
        return s.toString()
    }

    fun onDrop(
        source: Location,
        dest: Location
    ) {
        if (_uiState.value.state == GameState.PASSED) {
            return
        }

        var ch: Char

        when (source.phase) {
            Location.Phase.LEFT -> {
                ch = _uiState.value.leftSubstitutions[source.index]
                _uiState.update { currentState ->
                    currentState.copy(
                        leftSubstitutions = _uiState.value.leftSubstitutions.replaceRange(
                            source.index,
                            source.index + 1,
                            UNKNOWN.toString()
                        )
                    )
                }
            }
            Location.Phase.RIGHT -> {
                ch = _uiState.value.rightSubstitutions[source.index]
                _uiState.update { currentState ->
                    currentState.copy(
                        rightSubstitutions = _uiState.value.rightSubstitutions.replaceRange(
                            source.index,
                            source.index + 1,
                            UNKNOWN.toString()
                        )
                    )
                }
            }
            Location.Phase.RESERVE -> {
                ch = _uiState.value.reserves[source.index]
                _uiState.update { currentState ->
                    currentState.copy(
                        reserves = _uiState.value.reserves.removeRange(
                            source.index,
                            source.index + 1
                        )
                    )
                }
            }
        }

        when (dest.phase) {
            Location.Phase.LEFT -> {
                val replacedCh = _uiState.value.leftSubstitutions[dest.index]
                val newReserves = if (replacedCh == UNKNOWN) _uiState.value.reserves else _uiState.value.reserves + replacedCh
                _uiState.update { currentState ->
                    currentState.copy(
                        leftSubstitutions = _uiState.value.leftSubstitutions.replaceRange(
                            dest.index,
                            dest.index + 1,
                            ch.toString()
                        ),
                        reserves = newReserves
                    )
                }
            }

            Location.Phase.RIGHT -> {
                val replacedCh = _uiState.value.rightSubstitutions[dest.index]
                val newReserves = if (replacedCh == UNKNOWN) _uiState.value.reserves else _uiState.value.reserves + replacedCh
                _uiState.update { currentState ->
                    currentState.copy(
                        rightSubstitutions = _uiState.value.rightSubstitutions.replaceRange(
                            dest.index,
                            dest.index + 1,
                            ch.toString()
                        ),
                        reserves = newReserves
                    )
                }
            }

            Location.Phase.RESERVE -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        reserves = _uiState.value.reserves + ch.toString()
                    )
                }
            }
        }

        val newState = getState()
        if (newState != _uiState.value.state) {
            if (newState == GameState.PASSED) {
                val timeToSolve =
                    ((System.currentTimeMillis() - startTime) / 1000).seconds.toString()

                val availableDifficulties = when(_uiState.value.difficulty) {
                    Difficulty.Easy -> listOf(Difficulty.Easy, Difficulty.Medium)
                    Difficulty.Medium -> listOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard)
                    Difficulty.Hard -> listOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard)
                }
                _uiState.update {
                    currentState -> currentState.copy(
                        state = newState,
                        timeToSolve = timeToSolve,
                        availableDifficulties = availableDifficulties
                    )
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        state = newState
                    )
                }
            }
        }

        viewModelScope.launch {
            saveProgress(
                _uiState.value.difficulty,
                SavedProgress(
                    id = gameId,
                    solved = _uiState.value.state == GameState.PASSED,
                    timeToSolve = _uiState.value.timeToSolve,
                    startTime = startTime,
                    lhs = _uiState.value.leftSubstitutions,
                    rhs = _uiState.value.rightSubstitutions,
                    reserves = _uiState.value.reserves
                )
            )
        }

    }

    fun generateEmptySubstitutions(expression: String) : String {
        return UNKNOWN.toString().repeat(expression.count{ it == UNKNOWN })
    }

    fun onUpdateDifficulty(difficulty: Difficulty) {
        if (difficulty == uiState.value.difficulty) {
            return
        }
        setPuzzle(difficulty)
    }

    fun jumpToNextPuzzle(
        currDifficulty: Difficulty
    ) {
        when (currDifficulty) {
            Difficulty.Easy -> setPuzzle(Difficulty.Medium)
            Difficulty.Medium -> setPuzzle(Difficulty.Hard)
            Difficulty.Hard -> {}
        }
    }
    fun resetGame() {
        val puzzle = generatePuzzle(today, _uiState.value.difficulty)
        val leftSubs = generateEmptySubstitutions(puzzle.lhs)
        val rightSubs = generateEmptySubstitutions(puzzle.rhs)
        _uiState.update { currentState ->
            currentState.copy(
                leftExpression = puzzle.lhs,
                leftSubstitutions = leftSubs,
                rightExpression = puzzle.rhs,
                rightSubstitutions = rightSubs,
                reserves = puzzle.reserves,
                state = GameState.SOLVING
            )
        }
        viewModelScope.launch {
            saveProgress(
                _uiState.value.difficulty,
                SavedProgress(
                    id = gameId,
                    solved = false,
                    timeToSolve = "",
                    startTime = startTime,
                    lhs = leftSubs,
                    rhs = rightSubs,
                    reserves = puzzle.reserves
                )
            )
        }
    }

    // '1' - '9' or '?'
    fun isValidSavedProgressChar(char: Char) : Boolean {
        if (char == UNKNOWN) {
            return true
        }
        if (char.isDigit()) {
            return true
        }
        return false
    }

    fun isValidSavedProgress(
        puzzle: Puzzle,
        savedProgress: SavedProgress
    ) : Boolean {
        if (gameId != savedProgress.id) {
            return false
        }
        if (puzzle.lhs.count { it == UNKNOWN } != savedProgress.lhs.length) {
            return false
        }
        if (puzzle.rhs.count { it == UNKNOWN } != savedProgress.rhs.length) {
            return false
        }
        for (ch in savedProgress.lhs) {
            if (!isValidSavedProgressChar(ch)) {
                return false
            }
        }
        for (ch in savedProgress.rhs) {
            if (!isValidSavedProgressChar(ch)) {
                return false
            }
        }
        for (ch in savedProgress.reserves) {
            if (!isValidSavedProgressChar(ch)) {
                return false
            }
        }
        return true
    }

    fun determineWhichPuzzleToShow() {
        viewModelScope.launch {
            var startingDifficulty = Difficulty.Easy
            val availableDifficulties : MutableList<Difficulty> = mutableListOf(Difficulty.Easy)

            val easyPuzzle = generatePuzzle(today, Difficulty.Easy)
            val savedEasy = savedProgressRepository.easyProgress.first()
            if (isValidSavedProgress(easyPuzzle, savedEasy) && savedEasy.solved) {
                startingDifficulty = Difficulty.Medium
                availableDifficulties.add(Difficulty.Medium)
                val mediumPuzzle = generatePuzzle(today, Difficulty.Medium)
                val savedMedium = savedProgressRepository.mediumProgress.first()
                if (isValidSavedProgress(mediumPuzzle, savedMedium) && savedMedium.solved) {
                    startingDifficulty = Difficulty.Hard
                    availableDifficulties.add(Difficulty.Hard)
                }
            }

            _uiState.update { currentState ->
                currentState.copy(
                    availableDifficulties = availableDifficulties
                ) }

            setPuzzle(startingDifficulty)
        }
    }

    fun setPuzzle(difficulty: Difficulty) {
        viewModelScope.launch {
            val puzzle = generatePuzzle(today, difficulty)

            // Check if there is saved progress
            val savedProgress = when(difficulty) {
                Difficulty.Easy -> savedProgressRepository.easyProgress
                Difficulty.Medium -> savedProgressRepository.mediumProgress
                Difficulty.Hard -> savedProgressRepository.hardProgress
            }.first()

            if (isValidSavedProgress(puzzle, savedProgress)) {
                startTime = savedProgress.startTime
                _uiState.update { currentState ->
                    currentState.copy(
                        difficulty = difficulty,
                        leftExpression = puzzle.lhs,
                        leftSubstitutions = savedProgress.lhs,
                        rightExpression = puzzle.rhs,
                        rightSubstitutions = savedProgress.rhs,
                        reserves = savedProgress.reserves,
                        timeToSolve = savedProgress.timeToSolve,
                        state = if (savedProgress.solved) GameState.PASSED
                        else if (savedProgress.lhs.indexOf(UNKNOWN) < 0 && savedProgress.rhs.indexOf(UNKNOWN) < 0) GameState.FAILED
                        else GameState.SOLVING,
                    )
                }
            } else {
                startTime = System.currentTimeMillis()
                val leftSubs = generateEmptySubstitutions(puzzle.lhs)
                val rightSubs = generateEmptySubstitutions(puzzle.rhs)
                _uiState.update { currentState ->
                    currentState.copy(
                        difficulty = difficulty,
                        leftExpression = puzzle.lhs,
                        leftSubstitutions = leftSubs,
                        rightExpression = puzzle.rhs,
                        rightSubstitutions = rightSubs,
                        reserves = puzzle.reserves,
                        state = GameState.SOLVING
                    )
                }
                saveProgress(
                    difficulty,
                    SavedProgress(
                        id = gameId,
                        solved = false,
                        timeToSolve = "",
                        startTime = startTime,
                        lhs = leftSubs,
                        rhs = rightSubs,
                        reserves = puzzle.reserves
                    )
                )
            }
        }
    }

    fun saveProgress(
        difficulty: Difficulty,
        progress: SavedProgress
    ) {
        viewModelScope.launch {
            when (difficulty) {
                Difficulty.Easy -> savedProgressRepository.saveEasyProgress(progress)
                Difficulty.Medium -> savedProgressRepository.saveMediumProgress(progress)
                Difficulty.Hard -> savedProgressRepository.saveHardProgress(progress)
            }
        }
    }
}

@VisibleForTesting
internal fun generatePuzzle(date: LocalDate, difficulty: Difficulty) : Puzzle {
    val seed = 1000000 * date.year + 100 * date.dayOfYear + toInt(difficulty)
    val seededRandom = Random(seed)
    return when (difficulty) {
        Difficulty.Easy -> generateEasyPuzzle(seededRandom)
        Difficulty.Medium -> generateMediumPuzzle(seededRandom)
        Difficulty.Hard -> generateHardPuzzle(seededRandom)
    }
}

fun generateEasyPuzzle(r: Random) : Puzzle {
    val s1 = generateRandomDigits(r, 4)
    val positions = when (r.nextInt(0, 2)) {
        0 -> listOf(1)
        else -> listOf(2)
    }
    val ops = if (r.nextBoolean()) "+" else "-"
    var s2 = insertOperators(s1, positions, ops)

    var eval = evaluate(s2)
    if (eval <= 0) {
        s2 = s2.replace('-', '+')
    }
    eval = evaluate(s2)

    return Puzzle(
        lhs = Regex("\\d").replace(s2, UNKNOWN.toString()),
        rhs = eval.toString(),
        reserves = generateReserves(r, s2)
    )
}

fun generateMediumPuzzle(r: Random) : Puzzle {
    val s1 = generateRandomDigits(r, 6)
    val positions = when (r.nextInt(0, 3)) {
        0 -> listOf(1, 3)
        1 -> listOf(2, 4)
        else -> listOf(1, 4)
    }
    val ops = generateTwoRandomOperators(r)
    var s2 = insertOperators(s1, positions, ops)

    var eval = evaluate(s2)
    if (eval <= 0) {
        s2 = s2.replace('-', '+')
    }
    eval = evaluate(s2)

    return Puzzle(
        lhs = Regex("\\d").replace(s2, UNKNOWN.toString()),
        rhs = eval.toString(),
        reserves = generateReserves(r, s2)
    )
}

fun generateHardPuzzle(r: Random) : Puzzle {
    val s1 = generateRandomDigits(r, 6)
    val positions = when (r.nextInt(0, 3)) {
        0 -> listOf(1, 3)
        1 -> listOf(2, 4)
        else -> listOf(1, 4)
    }
    val ops = generateTwoRandomOperatorsWithOneMultiply(r)
    var s2 = insertOperators(s1, positions, ops)

    var eval = evaluate(s2)
    if (eval < 0) {
        s2 = s2.replace('-', '+')
    }
    eval = evaluate(s2)

    val evalStr = eval.toString()
    var evalIdx = r.nextInt(0, evalStr.length)
    if (eval > 0) {
        while (evalStr[evalIdx] == '0') {
            evalIdx = (evalIdx + 1) % evalStr.length
        }
    }

    return Puzzle(
        lhs = Regex("\\d").replace(s2, UNKNOWN.toString()),
        rhs = evalStr.substring(0, evalIdx) + UNKNOWN + evalStr.substring(evalIdx + 1),
        reserves = generateReserves(r, s2, initial = evalStr[evalIdx])
    )
}

fun toInt(difficulty: Difficulty) : Int {
    return when (difficulty) {
        Difficulty.Easy -> 0
        Difficulty.Medium -> 1
        Difficulty.Hard -> 2
    }
}

fun generateReserves(r: Random, s: String, initial: Char? = null) : String {
    val sb = StringBuilder()
    for (ch in s) {
        if (ch.isDigit()) {
            sb.append(ch)
        }
    }
    if (initial != null) {
        sb.append(initial)
    }
    sb.append(generateRandomDigits(r, 1))

    val charArray = sb.toString().toCharArray()
    charArray.sort()
    sb.clear()
    sb.append(charArray)
    return sb.toString()
}


fun generateTwoRandomOperators(r: Random) : String {
    return when(r.nextInt(0, 6)) {
        0 -> "+-"
        1 -> "+*"
        2 -> "-+"
        3 -> "-*"
        4 -> "*+"
        else -> "*-"
    }
}

fun generateTwoRandomOperatorsWithOneMultiply(r: Random) : String {
    return when(r.nextInt(0, 4)) {
        0 -> "+*"
        1 -> "-*"
        2 -> "*+"
        else -> "*-"
    }
}

fun insertOperators(s: String, positions: List<Int>, ops: String) : String {
    val sb = StringBuilder()
    var opsIdx = 0
    for ((idx, char) in s.withIndex()) {
        if (idx in positions) {
            sb.append(ops[opsIdx])
            opsIdx++
        } else {
            sb.append(char)
        }
    }
    return sb.toString()
}

fun generateRandomDigits(r: Random, maskSize: Int) : String {
    val s = StringBuilder()
    repeat(maskSize) {
        s.append('0' + r.nextInt(1, 10)) // '1' to '9' inclusive.. Avoid 0 intentionally
    }
    return s.toString()
}

fun evaluate(expression: String) : Int {
    val postfix : MutableList<Any> = mutableListOf()
    val opStack = ArrayDeque<Char>()
    var curr = 0
    var hasDigit = false
    for (ch in expression) {
        if (ch.isDigit()) {
            hasDigit = true
            curr = 10 * curr + (ch - '0')
        } else {
            if (hasDigit) {
                postfix.add(curr)
            }
            curr = 0
            hasDigit = false
            while (opStack.isNotEmpty() && precedence(ch) <= precedence(opStack.first())) {
                postfix.add(opStack.removeFirst())
            }
            opStack.addFirst(ch)
        }
    }
    if (hasDigit) {
        postfix.add(curr)
    }
    while (opStack.isNotEmpty()) {
        postfix.add(opStack.removeFirst())
    }

    val evalStack = ArrayDeque<Int>()
    for (x in postfix) {
        if (x is Int) {
            evalStack.addFirst(x)
        } else {
            val second = evalStack.removeFirst()
            val first = evalStack.removeFirst()
            evalStack.addFirst(compute(first, second, x as Char))
        }
    }
    return evalStack.first()
}

fun precedence(op: Char) : Int {
    if (op == '+' || op == '-') {
        return 0
    }
    if (op == '*') {
        return 1
    }
    return 0
}

fun compute(a: Int, b: Int, op: Char) : Int {
    if (op == '+') {
        return a + b
    }
    if (op == '-') {
        return a - b
    }
    if (op == '*') {
        return a * b
    }
    return 0
}

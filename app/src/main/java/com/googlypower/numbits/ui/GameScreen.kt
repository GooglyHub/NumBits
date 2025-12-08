package com.googlypower.numbits.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.googlypower.numbits.R
import com.googlypower.numbits.ui.theme.NumBitsTheme

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    NumBitsTheme {
        GameScreen()
    }
}

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel()
) {
    val gameUiState by gameViewModel.uiState.collectAsState()
    val smallPadding = dimensionResource(R.dimen.padding_small)
    val mediumSpacing = dimensionResource(R.dimen.spacer_medium)
    val largeSpacing = dimensionResource(R.dimen.spacer_large)
    val intentContext = LocalContext.current

    Scaffold(topBar = { NumBitsTopAppBar() }) { it ->
        Draggable(
            modifier = Modifier
                .fillMaxSize()
                .padding(smallPadding)
        ) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DifficultySelector(
                    difficulty = gameUiState.difficulty,
                    availableDifficulties =  gameUiState.availableDifficulties,
                    onUpdateDifficulty = { diff: Difficulty -> gameViewModel.onUpdateDifficulty(diff) }
                )

                Spacer(modifier = Modifier.height(largeSpacing))

                GameLayout(
                    leftExpression = gameUiState.leftExpression,
                    leftSubstitutions = gameUiState.leftSubstitutions,
                    rightExpression = gameUiState.rightExpression,
                    rightSubstitutions = gameUiState.rightSubstitutions,
                    onDrop = { loc1, loc2 -> gameViewModel.onDrop(loc1, loc2) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )

                Spacer(modifier = Modifier.height(largeSpacing))

                Text(
                    text = stringResource(R.string.instructions),
                    textAlign = TextAlign.Center,
                    style = typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(largeSpacing))

                ReservesRow(
                    reserves = gameUiState.reserves,
                    onDrop = { loc1, loc2 -> gameViewModel.onDrop(loc1, loc2) }
                )

                if (gameUiState.state == GameState.PASSED) {
                    Spacer(modifier = Modifier.height(mediumSpacing))
                    Text(
                        text = stringResource(R.string.congrats),
                        textAlign = TextAlign.Center,
                        style = typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(mediumSpacing))
                    Text(
                        text = stringResource(
                            R.string.finish_time,
                            gameUiState.timeToSolve
                        ),
                        textAlign = TextAlign.Center,
                        style = typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(mediumSpacing))
                    if (gameUiState.difficulty == Difficulty.Hard) {
                        Text(
                            text = stringResource(R.string.come_back_tomorrow),
                            textAlign = TextAlign.Center,
                            style = typography.bodyLarge
                        )
                    } else {
                        Button(
                            onClick = {
                                gameViewModel.jumpToNextPuzzle(gameUiState.difficulty)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.play_next_puzzle))
                        }
                    }
                    Spacer(modifier = Modifier.height(mediumSpacing))
                    OutlinedButton(
                        onClick = {
                            shareSolvedPuzzle(
                                intentContext = intentContext,
                                puzzleNumber = gameViewModel.gameId,
                                puzzleDifficulty = gameUiState.difficulty.toString(),
                                timeToSolve = gameUiState.timeToSolve
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.share_results))
                    }
                } else if (gameUiState.state == GameState.FAILED) {
                    Spacer(modifier = Modifier.height(largeSpacing))
                    Text(
                        text = stringResource(R.string.not_quite),
                        textAlign = TextAlign.Center,
                        style = typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(largeSpacing))
                    OutlinedButton(
                        onClick = {
                            gameViewModel.resetGame()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultySelector(
    modifier: Modifier = Modifier,
    difficulty: Difficulty,
    availableDifficulties: List<Difficulty>,
    onUpdateDifficulty: (Difficulty) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.clickable {
                expanded = !expanded
            },
            text = difficulty.toString()
        )
        IconButton(
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select difficulty")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (d in ALL_DIFFICULTIES) {
                DropdownMenuItem(
                    text = {
                        Text(text = d.toString())
                    },
                    enabled = availableDifficulties.contains(d),
                    onClick = {
                        expanded = false
                        onUpdateDifficulty(d)
                    }
                )
            }
        }
    }
}
@Composable
fun GameLayout(
    leftExpression: String,
    leftSubstitutions: String,
    rightExpression: String,
    rightSubstitutions: String,
    onDrop: (Location, Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val largePadding = dimensionResource(R.dimen.padding_large)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(largePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(mediumPadding)
        ) {

            ExpressionRow(
                phase = Location.Phase.LEFT,
                expression = leftExpression,
                substitutions = leftSubstitutions,
                onDrop = onDrop)

            ExpressionRow(
                phase = Location.Phase.RIGHT,
                expression = rightExpression,
                substitutions = rightSubstitutions,
                onDrop = onDrop)
        }
    }
}

@Composable
fun ReservesRow(
    modifier: Modifier = Modifier,
    reserves: String,
    onDrop: (Location, Location) -> Unit
) {
    DropTarget<Location>(
        modifier = modifier
    ) { isInBound, location ->
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .heightIn(min = 60.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,

                ) {
                for ((index, ch) in reserves.withIndex()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    DragTarget(
                        modifier = Modifier,
                        dataToDrop = Location(phase = Location.Phase.RESERVE, index = index)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                style = typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            if (isInBound && location != null && location.phase != Location.Phase.RESERVE) {
                onDrop(location, Location(Location.Phase.RESERVE, 0))
            }
        }
    }
}

@Composable
fun ExpressionRow(
    modifier: Modifier = Modifier,
    phase: Location.Phase,
    expression: String,
    substitutions: String,
    onDrop: (Location, Location) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (phase == Location.Phase.RIGHT) {
            Text(
                text = "=",
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        var subIdx = 0
        for (ch in expression) {
            if (ch == UNKNOWN) {
                val idx = subIdx
                if (substitutions[idx] == UNKNOWN) {
                    // Empty box: Drop target only
                    DropTarget<Location>(
                        modifier = Modifier
                    ) { isInBound, location ->
                        val borderColor = MaterialTheme.colorScheme.onPrimary
                        val borderWidth = 2.dp

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .border(width = borderWidth, color = borderColor)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                        }
                        if (isInBound && location != null) {
                            onDrop(location, Location(phase, idx))
                        }
                    }
                } else {
                    // Filled box: Both drop target and drag target
                    DragTarget(modifier = Modifier, dataToDrop = Location(phase = phase, index = idx )) {
                        DropTarget<Location>(
                            modifier = Modifier
                        ) { isInBound, location ->
                            val borderColor = MaterialTheme.colorScheme.onPrimary
                            val borderWidth = 2.dp

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(width = borderWidth, color = borderColor)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = substitutions[idx].toString(),
                                    style = typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isInBound && location != null) {
                                onDrop(location, Location(phase, idx))
                            }
                        }
                    }
                }
                subIdx++
            } else {
                // Hard-coded char: No drag and drop
                val isDigit = ch.isDigit()
                val space = 12.dp

                // Put space around non-digits
                if (!isDigit) {
                    Spacer(modifier = Modifier.width(space))
                }
                Text(
                    text = if (ch == '*') "×" else ch.toString(),
                    style = typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!isDigit) {
                    Spacer(modifier = Modifier.width(space))
                }
            }
        }
    }
}

data class Location(val phase: Phase, val index: Int) {
    enum class Phase { LEFT, RIGHT, RESERVE }
}

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }
@Composable
fun <T> DragTarget(
    modifier: Modifier,
    dataToDrop: T,
    content: @Composable (() -> Unit)
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragTargetInfo.current

    Box(modifier = modifier
        .onGloballyPositioned {
            currentPosition = it.localToWindow(Offset.Zero)
        }
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = {
                currentState.dataToDrop = dataToDrop
                currentState.isDragging = true
                currentState.dragPosition = currentPosition + it
                currentState.draggableComposable = content
            }, onDrag = { change, dragAmount ->
                change.consume()
                currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
            }, onDragEnd = {
                currentState.isDragging = false
                currentState.dragOffset = Offset.Zero
            }, onDragCancel = {
                currentState.dragOffset = Offset.Zero
                currentState.isDragging = false
            })
        }) {
        content()
    }
}

@Composable
fun Draggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            content()
            if (state.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        scaleX = 1.5f
                        scaleY = 1.5f
                        alpha = if (targetSize == IntSize.Zero) 0f else .7f
                        translationX = offset.x.minus(targetSize.width / 2)
                        translationY = offset.y.minus(targetSize.height)
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

@Composable
fun <T> DropTarget(
    modifier: Modifier,
    content: @Composable() (BoxScope.(isInBound: Boolean, data: T?) -> Unit)) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
                    && dragOffset != Offset.Zero
        }
    }) {
        val data =
            if (isCurrentDropTarget && !dragInfo.isDragging) dragInfo.dataToDrop as T? else null
        content(isCurrentDropTarget, data)
    }
}

private fun shareSolvedPuzzle(
    intentContext: Context,
    puzzleNumber: Int,
    puzzleDifficulty: String,
    timeToSolve: String
) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            intentContext.getString(R.string.share_text, puzzleNumber, puzzleDifficulty, timeToSolve)
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)

    try {
        intentContext.startActivity(shareIntent, null)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            intentContext,
            intentContext.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumBitsTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.image_size))
                    .padding(dimensionResource(id = R.dimen.padding_small)),
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.app_name),
                style = typography.displaySmall
            )
        }
    },
        modifier = modifier
    )
}

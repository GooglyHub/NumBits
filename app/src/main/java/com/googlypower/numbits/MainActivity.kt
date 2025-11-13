package com.googlypower.numbits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.googlypower.numbits.ui.GameScreen
import com.googlypower.numbits.ui.GameViewModel
import com.googlypower.numbits.ui.theme.NumBitsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumBitsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameScreen(gameViewModel = viewModel(
                        factory = GameViewModel.Factory
                    ))
                }
            }
        }
    }
}

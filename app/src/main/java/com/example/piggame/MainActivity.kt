package com.example.piggame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.piggame.model.Player

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B5E20)) { // Koyu Yeşil Masa
                PigGameScreen()
            }
        }
    }
}

@Composable
fun PigGameScreen(vm: GameViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val isRolling by vm.isRolling.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skor Tablosu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround // Diziyi kaldırıp sadece bunu yazıyoruz
        ) {
            ScoreBoard("Siz", state.player1Score, state.currentPlayer == Player.PLAYER_1)
            ScoreBoard("Ajan (RL)", state.player2Score, state.currentPlayer == Player.PLAYER_2)
        }

        // Orta Alan: Zar ve Geçici Puan
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tur Puanı: ${state.currentTurnScore}", fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(20.dp))
            DiceView(state.lastRolledDice, isRolling)
        }

        // Kontroller
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { vm.onRollClick() },
                enabled = state.currentPlayer == Player.PLAYER_1 && !state.isGameOver(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) { Text("Zar At") }

            Button(
                onClick = { vm.onHoldClick() },
                enabled = state.currentPlayer == Player.PLAYER_1 && !state.isGameOver(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) { Text("Dur (Hold)") }
        }

        if (state.isGameOver()) {
            Text("KAZANAN: ${if (state.player1Score >= 100) "SİZ" else "AJAN"}",
                color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScoreBoard(name: String, score: Int, isCurrent: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFFFFD600) else Color(0xFFFFFFFF).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(140.dp).padding(8.dp)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontWeight = FontWeight.Bold)
            Text("$score", fontSize = 24.sp)
        }
    }
}

@Composable
fun DiceView(value: Int, isRolling: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = infiniteRepeatable(tween(200, easing = LinearEasing))
    )

    Card(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer { rotationZ = if (isRolling) rotation else 0f },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (value == 0) "?" else "$value", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
    }
}
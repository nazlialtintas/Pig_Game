package com.example.piggame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.piggame.model.Player
import kotlinx.coroutines.delay

// Retro Casino Renkleri
val CasinoDarkGreen = Color(0xFF0A3D16)
val CasinoGold = Color(0xFFD4AF37)
val CasinoRed = Color(0xFF8B0000)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RetroPigApp()
        }
    }
}

@Composable
fun RetroPigApp(vm: GameViewModel = viewModel()) {
    val mode by vm.gameMode.collectAsState()
    // ViewModel'daki gerçek yükleme durumunu dinliyoruz
    val isLoading by vm.isLoading.collectAsState()

    if (isLoading) {
        // EĞİTİM EKRANI: Ajan öğrenene kadar burada bekletiyoruz
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CasinoDarkGreen),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Şık bir yükleme animasyonu
                CircularProgressIndicator(
                    color = CasinoGold,
                    strokeWidth = 4.dp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "KASA HAZIRLANIYOR...",
                    color = CasinoGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Ajan strateji geliştiriyor, lütfen bekleyin.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    } else {
        // OYUN AKIŞI: Eğitim bittiği an burası tetiklenir
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color(0xFF1B5E20), CasinoDarkGreen)))
        ) {
            Crossfade(
                targetState = mode,
                animationSpec =tween(500) // Geçişi yumuşatır
            ) { currentMode ->
                when (currentMode) {
                    GameViewModel.GameMode.MENU -> MainMenuScreen(onStart = { vm.startGame() })
                    GameViewModel.GameMode.IN_GAME -> GamePlayScreen(vm)
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PIG GAME", fontSize = 52.sp, color = CasinoGold, fontWeight = FontWeight.ExtraBold)
        Text("REINFORCEMENT LEARNING", fontSize = 14.sp, color = CasinoGold.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(48.dp))
        CasinoButton(text = "OYUNA BAŞLA", onClick = onStart)
    }
}
@Composable
fun GamePlayScreen(vm: GameViewModel) {
    val state by vm.uiState.collectAsState()
    val isRolling by vm.isRolling.collectAsState()

    // Tüm ekranı kaplayan ana kutu (Overlay için Box kullanıyoruz)
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. OYUN ALANI (Arka plandaki ana görünüm)
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Üst Bölüm: Skorlar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ScoreCard("PLAYER", state.player1Score, state.currentPlayer == Player.PLAYER_1, Modifier.weight(1f))
                ScoreCard("AGENT", state.player2Score, state.currentPlayer == Player.PLAYER_2, Modifier.weight(1f))
            }

            // Orta Bölüm: Zar ve Tur Puanı
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TUR PUANI", color = CasinoGold.copy(alpha = 0.8f), fontSize = 16.sp)
                Text("${state.currentTurnScore}", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                CasinoDiceView(state.lastRolledDice, isRolling)
            }

            // Alt Bölüm: Kontroller
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CasinoButton(
                    "ZAR AT",
                    onClick = { vm.onRollClick() },
                    enabled = state.currentPlayer == Player.PLAYER_1 && !state.isGameOver()
                )
                CasinoButton(
                    "DUR",
                    onClick = { vm.onHoldClick() },
                    enabled = state.currentPlayer == Player.PLAYER_1 && !state.isGameOver()
                )
            }
        }

        // 2. OYUN BİTTİ EKRANI (Kazananı gösteren şık Overlay)
        if (state.isGameOver()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)) // Arka planı karart
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, CasinoGold, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = CasinoDarkGreen),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.player1Score >= 100) "TEBRİKLER!" else "KASA KAZANDI!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CasinoGold
                        )
                        Text(
                            text = if (state.player1Score >= 100) "Ajanı dize getirdin!" else "Ajan stratejini çözdü.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FinalScoreItem("SİZ", state.player1Score)
                            FinalScoreItem("AJAN", state.player2Score)
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                        CasinoButton("ANA MENÜ", onClick = { vm.backToMenu() })
                    }
                }
            }
        }
    }
}
@Composable
fun ScoreCard(label: String, score: Int, isCurrent: Boolean, modifier: Modifier) {
    val borderColor = if (isCurrent) CasinoGold else Color.Transparent
    val backgroundColor = if (isCurrent) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$score", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun CasinoButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = CasinoRed,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(2.dp, CasinoGold),
        modifier = Modifier.height(55.dp).padding(horizontal = 12.dp)
    ) {
        Text(text, color = CasinoGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun CasinoDiceView(value: Int, isRolling: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 720f else 0f,
        animationSpec = tween(500, easing = LinearOutSlowInEasing)
    )

    Card(
        modifier = Modifier
            .size(110.dp)
            .graphicsLayer {
                rotationZ = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (value == 0) {
                Text("?", fontSize = 48.sp, color = Color.LightGray)
            } else {
                DiceDotsDisplay(value)
            }
        }
    }
}

@Composable
fun FinalScoreItem(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("$score", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DiceDotsDisplay(value: Int) {
    Box(modifier = Modifier.padding(12.dp).fillMaxSize()) {
        val dotColor = Color.Black
        when (value) {
            1 -> Dot(Alignment.Center)
            2 -> { Dot(Alignment.TopEnd); Dot(Alignment.BottomStart) }
            3 -> { Dot(Alignment.TopEnd); Dot(Alignment.Center); Dot(Alignment.BottomStart) }
            4 -> { Dot(Alignment.TopStart); Dot(Alignment.TopEnd); Dot(Alignment.BottomStart); Dot(Alignment.BottomEnd) }
            5 -> { Dot(Alignment.TopStart); Dot(Alignment.TopEnd); Dot(Alignment.Center); Dot(Alignment.BottomStart); Dot(Alignment.BottomEnd) }
            6 -> { Dot(Alignment.TopStart); Dot(Alignment.TopEnd); Dot(Alignment.CenterStart); Dot(Alignment.CenterEnd); Dot(Alignment.BottomStart); Dot(Alignment.BottomEnd) }
        }
    }
}

@Composable
fun BoxScope.Dot(alignment: Alignment) {
    Surface(
        modifier = Modifier.size(16.dp).align(alignment),
        shape = CircleShape,
        color = Color.Black
    ) {}
}


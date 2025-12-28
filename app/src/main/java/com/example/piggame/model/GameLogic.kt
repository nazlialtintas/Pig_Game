package com.example.piggame.model

import kotlin.random.Random

class GameLogic {
    fun rollDice(): Int = Random.nextInt(1, 7)

    // Hamle sonrası yeni durumu hesaplar
    fun nextState(currentState: GameState, action: AgentAction, diceValue: Int): GameState {
        return when (action) {
            AgentAction.ROLL -> {
                if (diceValue == 1) {
                    // 1 geldi, tüm tur puanı gitti, sıra değişti
                    currentState.copy(
                        currentTurnScore = 0,
                        currentPlayer = if (currentState.currentPlayer == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1,
                        lastRolledDice = 1
                    )
                } else {
                    // Puan eklendi, sıra hala aynı kişide
                    currentState.copy(
                        currentTurnScore = currentState.currentTurnScore + diceValue,
                        lastRolledDice = diceValue
                    )
                }
            }
            AgentAction.HOLD -> {
                // Mevcut tur puanını kasaya ekle ve sırayı devret
                val isP1 = currentState.currentPlayer == Player.PLAYER_1
                currentState.copy(
                    player1Score = if (isP1) currentState.player1Score + currentState.currentTurnScore else currentState.player1Score,
                    player2Score = if (!isP1) currentState.player2Score + currentState.currentTurnScore else currentState.player2Score,
                    currentTurnScore = 0,
                    currentPlayer = if (isP1) Player.PLAYER_2 else Player.PLAYER_1,
                    lastRolledDice = 0
                )
            }
        }
    }
}
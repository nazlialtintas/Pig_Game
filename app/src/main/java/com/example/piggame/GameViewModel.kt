package com.example.piggame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piggame.agent.*
import com.example.piggame.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val qTable = QTable()
    private val logic = GameLogic()
    private val agent = QLearningAgent(qTable)
    private val trainer = TrainingManager(qTable)

    private val _uiState = MutableStateFlow(GameState(0, 0, Player.PLAYER_1, 0))
    val uiState = _uiState.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling = _isRolling.asStateFlow()



    init {
        // Uygulama açıldığında ajanı arka planda eğit
        viewModelScope.launch {
            trainer.train(100000)
        }
    }

    fun onRollClick() {
        if (_uiState.value.currentPlayer == Player.PLAYER_1 && !_uiState.value.isGameOver()) {
            performRoll()
        }
    }

    fun onHoldClick() {
        if (_uiState.value.currentPlayer == Player.PLAYER_1) {
            _uiState.value = logic.nextState(_uiState.value, AgentAction.HOLD, 0)
            checkAgentTurn()
        }
    }

    private fun performRoll() {
        viewModelScope.launch {
            _isRolling.value = true
            delay(500) // Zar animasyonu için bekleme
            val dice = logic.rollDice()
            _uiState.value = logic.nextState(_uiState.value, AgentAction.ROLL, dice)
            _isRolling.value = false

            if (_uiState.value.currentPlayer == Player.PLAYER_2) {
                checkAgentTurn()
            }
        }
    }

    private fun checkAgentTurn() {
        if (_uiState.value.currentPlayer == Player.PLAYER_2 && !_uiState.value.isGameOver()) {
            viewModelScope.launch {
                delay(1000) // Ajan "düşünüyor" hissi
                val action = agent.chooseAction(_uiState.value)
                if (action == AgentAction.ROLL) {
                    performRoll()
                } else {
                    _uiState.value = logic.nextState(_uiState.value, AgentAction.HOLD, 0)
                }
            }
        }
    }
}
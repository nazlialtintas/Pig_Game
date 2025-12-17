package com.example.piggame

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.piggame.agent.*
import com.example.piggame.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AndroidViewModel kullanıyoruz ki Context üzerinden hafızaya erişebilelim
class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val qTable = QTable()
    private val logic = GameLogic()
    private val agent = QLearningAgent(qTable)
    private val trainer = TrainingManager(qTable)

    private val _uiState = MutableStateFlow(GameState(0, 0, Player.PLAYER_1, 0))
    val uiState = _uiState.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling = _isRolling.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.MENU)
    val gameMode = _gameMode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    enum class GameMode { MENU, IN_GAME }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext

            // Önce hafızayı yükle
            qTable.loadFromStorage(context)

            // Eğer hala 0 ise eğitimi başlat
            if (qTable.size() == 0) {
                println("DEBUG: Ajan cahil, eğitim başlıyor...")
                trainer.train(100000) // 200 bin maç daha iyidir
                qTable.saveToStorage(context)
                println("DEBUG: Eğitim bitti. Yeni Durum Sayısı: ${qTable.size()}")
            } else {
                println("DEBUG: Ajan tecrübeli, ${qTable.size()} durum yüklendi.")
            }
            _isLoading.value = false
        }
    }

    fun startGame() {
        _uiState.value = GameState(0, 0, Player.PLAYER_1, 0)
        _gameMode.value = GameMode.IN_GAME
    }

    fun backToMenu() {
        _gameMode.value = GameMode.MENU
    }

    fun onRollClick() {
        if (_uiState.value.currentPlayer == Player.PLAYER_1 && !_uiState.value.isGameOver() && !_isRolling.value) {
            performRoll()
        }
    }

    fun onHoldClick() {
        if (_uiState.value.currentPlayer == Player.PLAYER_1 && !_isRolling.value) {
            _uiState.value = logic.nextState(_uiState.value, AgentAction.HOLD, 0)
            checkAgentTurn()
        }
    }

    private fun performRoll() {
        viewModelScope.launch {
            _isRolling.value = true
            delay(500)
            val dice = logic.rollDice()
            _uiState.value = logic.nextState(_uiState.value, AgentAction.ROLL, dice)
            _isRolling.value = false

            // SIRA KİMDEYSE ONA GÖRE KONTROL YAP
            if (!_uiState.value.isGameOver()) {
                if (_uiState.value.currentPlayer == Player.PLAYER_2) {
                    checkAgentTurn() // Ajanın sırasıysa devam et
                }
            }
        }
    }

    private fun checkAgentTurn() {
        // Ajanın hamle yapması için gereken kontroller
        if (_uiState.value.currentPlayer == Player.PLAYER_2 && !_uiState.value.isGameOver() && !_isRolling.value) {
            viewModelScope.launch {
                delay(1000) // Ajan düşünme süresi
                val action = agent.chooseAction(_uiState.value)
                if (action == AgentAction.ROLL) {
                    performRoll()
                } else {
                    _uiState.value = logic.nextState(_uiState.value, AgentAction.HOLD, 0)
                    // Hold yapınca sıra oyuncuya geçer, döngü burada durur.
                }
            }
        }
    }
}
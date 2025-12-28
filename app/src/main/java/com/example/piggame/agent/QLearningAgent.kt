package com.example.piggame.agent

import com.example.piggame.model.AgentAction
import com.example.piggame.model.GameLogic
import com.example.piggame.model.GameState
import kotlin.random.Random

class QLearningAgent(private val qTable: QTable) {

    // RL Parametreleri
    private val alpha = 0.1    // Öğrenme hızı
    private val gamma = 0.9    // Gelecekteki ödüllerin önemi
    private val epsilon = 0.1  // %10 ihtimalle rastgele hareket et

    // Ajanın karar verme anı
    fun chooseAction(state: GameState): AgentAction {

        if (state.currentTurnScore == 0) {
            return AgentAction.ROLL
        }

        if (state.player2Score + state.currentTurnScore >= 100) {
            return AgentAction.HOLD
        }

        // Epsilon-Greedy stratejisi
        if (Random.nextDouble() < epsilon) {
            return AgentAction.values().random()
        }

        val qRoll = qTable.getQValue(state, AgentAction.ROLL)
        val qHold = qTable.getQValue(state, AgentAction.HOLD)

        return if (qRoll > qHold) AgentAction.ROLL else AgentAction.HOLD
    }

    // Öğrenme fonksiyonu (Bellman Denklemi basitleştirilmiş hali)
    fun learn(state: GameState, action: AgentAction, nextState: GameState, reward: Double) {
        val oldQ = qTable.getQValue(state, action)

        // Bir sonraki durumdaki en iyi olası Q değeri
        val nextMaxQ = AgentAction.values().maxOf { qTable.getQValue(nextState, it) }

        // Q-Learning Formülü
        val newQ = oldQ + alpha * (reward + gamma * nextMaxQ - oldQ)
        qTable.setQValue(state, action, newQ)
    }
}
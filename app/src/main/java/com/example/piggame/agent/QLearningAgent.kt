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
    fun chooseAction(state: GameState, isTraining: Boolean): AgentAction {

        if (state.currentTurnScore == 0) {
            return AgentAction.ROLL
        }
        if (state.player2Score + state.currentTurnScore >= 100) {
            return AgentAction.HOLD
        }
        if(!isTraining){
            val myPotentialTotal =state.player2Score + state.currentTurnScore

            // Eğer rakip kazanmaya çok yaklaştıysa
            if (state.player1Score >= 94){
                if(myPotentialTotal < 100) return AgentAction.ROLL
            }

            //Rakiple 70i geçtiyse
            else if(state.player1Score >= 70 && state.player2Score < state.player1Score){
                if (state.currentTurnScore >= 20) {
                    return AgentAction.HOLD
                }
                // Farkı kapatma
                val gap = state.player1Score - myPotentialTotal
                if (gap <= 10) {
                    return AgentAction.HOLD
                }
                // Eğer baya gerideysen zar at
                return AgentAction.ROLL
            }
            if(state.player2Score < state.player1Score && state.player1Score >= 80){
                return AgentAction.ROLL
            }
        }

        val currentEpsilon = if (isTraining) epsilon else 0.0
        // Epsilon-Greedy stratejisi
        if (Random.nextDouble() < epsilon) {
            return AgentAction.values().random()
        }

        // Q table kararı
        val qRoll = qTable.getQValue(state, AgentAction.ROLL)
        val qHold = qTable.getQValue(state, AgentAction.HOLD)

        if(!isTraining){
        println("DEBUG AGENT: Durum= ${state.toQTableKey()} | ROLL Q=${qRoll} | HOLD Q:${qHold}")
        }
        //println("DEBUG AGENT: Durum= ${state.toQTableKey()} | ROLL Q=${qRoll} | HOLD Q:${qHold}")
        return if (qRoll >= qHold) AgentAction.ROLL else AgentAction.HOLD

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
package com.example.piggame.agent

import com.example.piggame.model.*

class TrainingManager(private val qTable: QTable) {
    private val logic = GameLogic()
    private val agent = QLearningAgent(qTable)

    fun train(episodes: Int) {
        repeat(episodes) {
            var state = GameState(0, 0, Player.PLAYER_2, 0) // Ajan kendiyle oynuyor

            while (!state.isGameOver()) {
                val action = agent.chooseAction(state)
                val dice = if (action == AgentAction.ROLL) logic.rollDice() else 0
                val nextState = logic.nextState(state, action, dice)

                // Ödül hesaplama
                var reward = 0.0
                if (nextState.player2Score >= 100) reward = 100.0 // Kazandı
                if (nextState.player1Score >= 100) reward = -100.0 // Kaybetti

                agent.learn(state, action, nextState, reward)
                state = nextState
            }
        }
    }
}
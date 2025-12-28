package com.example.piggame.agent

import com.example.piggame.model.*

class TrainingManager(private val qTable: QTable) {
    private val logic = GameLogic()
    private val agent = QLearningAgent(qTable)

    fun train(episodes: Int) {
        repeat(episodes) {
            // Her maç başında skorları sıfırla
            var state = GameState(0, 0, Player.PLAYER_1, 0)

            while (!state.isGameOver()) {
                val action = agent.chooseAction(state, isTraining = true)
                val dice = if (action == AgentAction.ROLL) logic.rollDice() else 0
                val nextState = logic.nextState(state, action, dice)

                // Ödül (Reward) Sistemi
                var reward = 0.0
                if (nextState.isGameOver()) {
                    // Ajan (Player 2) kazandıysa büyük ödül
                    if (nextState.player2Score >= 100) reward = 150.0
                    // Ajan kaybettiyse büyük ceza
                    else if (nextState.player1Score >= 100) reward = -150.0
                }else {
                    // Ara ödüller
                    if(action == AgentAction.HOLD){
                        val gainedScore = state.currentTurnScore
                        val scoreDiff = state.player1Score - state.player2Score

                        if (scoreDiff > 15) {
                            if (gainedScore < 15) {
                                reward = -20.0 // BÜYÜK CEZA! "Risk alman lazımdı!"
                            } else {
                                reward = 10.0  // AFERİN! Farkı kapatacak kadar topladın.
                            }
                        } else {
                            reward = gainedScore.toDouble()
                        }
                    }else {

                    // 1 gelme cezası
                    if(action == AgentAction.ROLL && dice == 1){
                        reward = -5.0
                    }else{
                        reward = 2.0
                    }
                    }
                }

                agent.learn(state, action, nextState, reward)
                state = nextState
            }
        }
    }
}
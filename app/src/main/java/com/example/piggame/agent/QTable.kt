package com.example.piggame.agent

import com.example.piggame.model.AgentAction
import com.example.piggame.model.GameState

class QTable {
    // Anahtar: GameState'in benzersiz string temsili (toQTableKey())
    // Değer: O GameState için her bir Action'ın (ROLL/HOLD) Q-değeri (kazanma potansiyeli)
    private val qValues: MutableMap<String, MutableMap<AgentAction, Double>> = mutableMapOf()

    // Bir durum ve eylem için Q-değerini döndürür. Eğer yoksa varsayılan 0.0 döndürür.
    fun getQValue(state: GameState, action: AgentAction): Double {
        val stateKey = state.toQTableKey()
        return qValues[stateKey]?.get(action) ?: 0.0
    }

    // Bir durum ve eylem için Q-değerini günceller.
    fun setQValue(state: GameState, action: AgentAction, value: Double) {
        val stateKey = state.toQTableKey()
        qValues.getOrPut(stateKey) { mutableMapOf() }[action] = value
    }

    // Q-tablosunu temizle
    fun clear() {
        qValues.clear()
    }

    // Öğrenilmiş durum/eylem çiftlerinin sayısını döndürür
    fun size(): Int {
        return qValues.size
    }

    // Q-Table'ı debug etmek için, belirli bir stateKey'e ait tüm action Q değerlerini döndürür.
    fun getActionsQValues(state: GameState): Map<AgentAction, Double> {
        val stateKey = state.toQTableKey()
        return qValues[stateKey] ?: emptyMap()
    }
}
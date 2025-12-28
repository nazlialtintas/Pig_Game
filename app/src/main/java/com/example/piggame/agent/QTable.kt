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

    fun saveToStorage(context: android.content.Context) {
        val prefs = context.getSharedPreferences("pig_game_prefs", android.content.Context.MODE_PRIVATE)
        val data = qValues.map { (state, actions) ->
            "$state|${actions[AgentAction.ROLL] ?: 0.0}|${actions[AgentAction.HOLD] ?: 0.0}"
        }.joinToString(";")
        prefs.edit().putString("agent_memory", data).apply()
    }

    fun loadFromStorage(context: android.content.Context) {
        val prefs = context.getSharedPreferences("pig_game_prefs", android.content.Context.MODE_PRIVATE)
        val data = prefs.getString("agent_memory", null) ?: return
        qValues.clear()
        data.split(";").forEach { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                val state = parts[0]
                val rollQ = parts[1].toDouble()
                val holdQ = parts[2].toDouble()
                qValues.getOrPut(state) { mutableMapOf() }[AgentAction.ROLL] = rollQ
                qValues.getOrPut(state) { mutableMapOf() }[AgentAction.HOLD] = holdQ
            }
        }
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
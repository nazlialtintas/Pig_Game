// GameState.kt
package com.example.piggame.model

data class GameState(
    val player1Score: Int,      // Kullanıcının (insan) toplam puanı
    val player2Score: Int,      // Ajanın toplam puanı
    val currentPlayer: Player,  // Sıra kimde
    val currentTurnScore: Int,  // Mevcut turda toplanan geçici puan
    val lastRolledDice: Int = 0 // En son atılan zar (sadece görsel için, Q-Learning'de doğrudan kullanılmaz)
) {
    // Oyunun bitip bitmediğini kontrol eder
    fun isGameOver(): Boolean {
        return player1Score >= 100 || player2Score >= 100
    }

    // Ajanın Q-Table'ı için bu durumu benzersiz bir şekilde temsil eden bir anahtar üretir.
    // Deep Learning olmadan tüm durumları bir tabloya sığdırmak için önemli.
    fun toQTableKey(): String {
        // currentStateKey, ajanın karar vermek için hangi bilgilere ihtiyacı olduğunu gösterir.
        // Burada Player2 (ajan) kendi puanına ve senin puanına göre karar verir.
        // Kendi (ajanın) o anki geçici puanı da önemlidir.
        return "${player2Score}-${player1Score}-${currentTurnScore}"
    }

    // Oyun bittiğinde kimin kazandığını döndürür
    fun getWinner(): Player? {
        if (player1Score >= 100) return Player.PLAYER_1
        if (player2Score >= 100) return Player.PLAYER_2
        return null
    }
}

enum class Player {
    PLAYER_1, // İnsan oyuncu
    PLAYER_2  // Ajan
}

// Ajanın verebileceği kararlar (eylemler)
enum class AgentAction {
    ROLL, // Zar at
    HOLD  // Dur, puanı kasana ekle
}
package com.example.insanecrossmobilepingpongapp.model

/**
 * Represents different screens in the app navigation flow.
 */
sealed class Screen {
    /**
     * Start menu where user selects player role.
     */
    data object Menu : Screen()
    
    /**
     * Game screen with racket view and motion control.
     */
    data object Game : Screen()
}

/**
 * Player role selection.
 */
enum class PlayerRole(val displayName: String, val token: String) {
    PLAYER1("Spieler 1 (Unten)", "player1"),
    PLAYER2("Spieler 2 (Oben)", "player2")
}

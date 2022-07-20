package dev.taah.crewmate.api.inner.enums

enum class GameState(val id: Int) {
    NotStarted(0),
    WaitingForHost(1),
    InGame(2);

    companion object {
        fun getById(id: Int): GameState? {
            return values().firstOrNull { it.id == id }
        }
    }

}
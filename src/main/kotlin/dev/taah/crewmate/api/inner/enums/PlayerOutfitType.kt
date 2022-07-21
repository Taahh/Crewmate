package dev.taah.crewmate.api.inner.enums

enum class PlayerOutfitType(val id: Int) {
    Default(0),
    Shapeshifted(1);

    companion object {
        fun getById(id: Int): PlayerOutfitType? {
            return values().firstOrNull { it.id == id }
        }
    }
}
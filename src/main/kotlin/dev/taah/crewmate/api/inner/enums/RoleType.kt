package dev.taah.crewmate.api.inner.enums

enum class RoleType(val id: Int) {
    Crewmate(0),
    Imposter(1),
    Scientist(2),
    Engineer(3),
    GuardianAngel(4),
    Shapeshifter(5);

    companion object {
        fun getById(id: Int): RoleType? {
            return values().firstOrNull { it.id == id }
        }
    }

}
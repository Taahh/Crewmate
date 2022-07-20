package dev.taah.crewmate.api.inner.enums

enum class TaskBarMode(val id: Int) {
    Normal(0),
    MeetingOnly(1),
    Invisible(2);

    companion object {
        fun getById(id: Int): TaskBarMode? {
            return values().firstOrNull { it.id == id }
        }
    }
}
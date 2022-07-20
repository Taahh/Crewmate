package dev.taah.crewmate.api.inner.enums

enum class QuickChatMode(val id: Int) {
    FreeChatOrQuickChat(1),
    QuickChatOnly(2);

    companion object {
        fun getById(id: Int): QuickChatMode? {
            return values().firstOrNull { it.id == id }
        }
    }

}
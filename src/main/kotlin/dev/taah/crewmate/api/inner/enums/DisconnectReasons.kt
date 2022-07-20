package dev.taah.crewmate.api.inner.enums

enum class DisconnectReasons(val id: Int) {
    ExitGame(0),
    GameFull(1),
    GameStarted(2),
    GameNotFound(3),
    IncorrectVersion(5),
    Banned(6),
    Kicked(7),
    Custom(8),
    InvalidName(9),
    Hacking(10),
    NotAuthorized(11),
    ConnectionLimit(12),
    Destroy(16),
    Error(17),
    IncorrectGame(18),
    ServerRequest(19),
    ServerFull(20),
    InternalPlayerMissing(100),
    InternalNonceFailure(101),
    InternalConnectionToken(102),
    PlatformLock(103),
    LobbyInactivity(104),
    MatchmakerInactivity(105),
    InvalidGameOptions(106),
    NoServersAvailable(107),
    QuickmatchDisabled(108),
    TooManyGames(109),
    QuickchatLock(110),
    MatchmakerFull(111),
    Sanctions(112),
    ServerError(113),
    SelfPlatformLock(114),
    DuplicateConnectionDetected(115),
    TooManyRequests(116),
    FocusLostBackground(207),
    IntentionalLeaving(208),
    FocusLost(209),
    NewConnection(210),
    PlatformParentalControlsBlock(211),
    PlatformUserBlock(212),
    PlatformFailedToGetUserBlock(213);

    companion object {
        fun getById(id: Int): DisconnectReasons? {
            return values().firstOrNull { it.id == id }
        }
    }

}
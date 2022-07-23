package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import kotlin.reflect.KClass

enum class RpcFlags(val id: Int, val clazz: KClass<out AbstractMessage>? = null) {
    PlayAnimation(0),
    CompleteTask(1),
    SyncSettings(2, SyncSettingsRpc::class),
    SetInfected(3),
    Exiled(4),
    CheckName(5, CheckNameRpc::class),
    SetName(6, SetNameRpc::class),
    CheckColor(7),
    SetColor(8, SetColorRpc::class),
    SetHat(9),
    SetSkin(10),
    ReportDeadBody(11),
    MurderPlayer(12),
    SendChat(13, SendChatRpc::class),
    StartMeeting(14),
    SetScanner(15),
    SendChatNote(16),
    SetPet(17),
    SetStartCounter(18, SetStartCounterRpc::class),
    EnterVent(19, EnterVentRpc::class),
    ExitVent(20),
    SnapTo(21),
    CloseMeeting(22),
    VotingComplete(23),
    CastVote(24),
    ClearVote(25),
    AddVote(26),
    CloseDoorsOfType(27),
    RepairSystem(28),
    SetTasks(29),
    ClimbLadder(31),
    UsePlatform(32),
    SendQuickChat(33),
    BootFromVent(34),
    UpdateSystem(35),
    SetVisor(36),
    SetNamePlate(37),
    SetLevel(38, SetLevelRpc::class),
    SetHatStr(39, SetHatRpc::class),
    SetSkinStr(40, SetSkinRpc::class),
    SetPetStr(41, SetPetRpc::class),
    SetVisorStr(42, SetVisorRpc::class),
    SetNamePlateStr(43, SetNamePlateRpc::class),
    SetRole(44),
    ProtectPlayer(45),
    Shapeshift(46),
    CheckMurder(47),
    CheckProtect(48),
    AdjustEscapeTimer(49);

    companion object {
        fun getById(id: Int): RpcFlags? {
            return values().firstOrNull { it.id == id }
        }
    }

}
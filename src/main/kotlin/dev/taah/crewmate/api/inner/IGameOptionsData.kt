package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.inner.enums.TaskBarMode
import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable

/**
 * @author Taah
 * @project crewmate
 * @since 7:03 PM [20-05-2022]
 */
interface IGameOptionsData<R : IRoleOptionsData<*>> : ISerializable, IDeserializable<IGameOptionsData<R>> {
    var version: Byte
    var maxPlayers: Byte
    var keywords: Int
    var map: Byte
    var speed: Float
    var crewLight: Float
    var imposterLight: Float
    var killCooldown: Float
    var commonTasks: Byte
    var longTasks: Byte
    var shortTasks: Byte
    var emergencyMeetings: Int
    var imposters: Byte
    var killDistance: Byte
    var discussionTime: Int
    var votingTime: Int
    var default: Boolean
    var emergencyCooldowns: Byte
    var confirmEjects: Boolean
    var visualTasks: Boolean
    var anonymousVoting: Boolean
    var taskbarMode: TaskBarMode
    var roleOptionsData: R
}
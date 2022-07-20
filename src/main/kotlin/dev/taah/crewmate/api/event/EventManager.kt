package dev.taah.crewmate.api.event

import com.google.common.collect.Lists

class EventManager {
    companion object {
        val EVENTS: ArrayList<Any> = Lists.newArrayList()
        var INSTANCE: EventManager? = null
    }

     fun registerEvent(listener: Any) {
        EVENTS.add(listener)
    }

     fun unregisterEvent(listenerClass: Class<Any>) {
        EVENTS.removeIf { it.javaClass.simpleName.equals(listenerClass.javaClass.simpleName) }
    }

     fun callEvent(event: IEvent) {
        EVENTS.forEach {e ->
            e.javaClass.declaredMethods.forEach {
                if (it.isAnnotationPresent(Subscribe::class.java) && it.parameterCount > 0 && it.parameters[0].type.simpleName == event.javaClass.simpleName) {
                    it.invoke(e, event)
                }
            }
        }
    }
}
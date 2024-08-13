package com.breadmoirai.awoobot.util

import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.EventTimeout
import dev.minn.jda.ktx.events.toTimeout
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass
import kotlin.time.Duration


inline fun <T : GenericEvent> JDA.listener(
    type: KClass<T>,
    timeout: Duration? = null,
    crossinline consumer: suspend CoroutineEventListener.(T) -> Unit
): CoroutineEventListener {
    return (eventManager as CoroutineEventManager).listener(type, timeout, consumer)
}

inline fun <T : GenericEvent> CoroutineEventManager.listener(
    type: KClass<T>,
    timeout: Duration? = null,
    crossinline consumer: suspend CoroutineEventListener.(T) -> Unit
): CoroutineEventListener {
    return object : CoroutineEventListener {
        override val timeout: EventTimeout
            get() = timeout.toTimeout()

        override fun cancel() {
            return unregister(this)
        }

        override suspend fun onEvent(event: GenericEvent) {
            @Suppress("UNCHECKED_CAST")
            if (type.isInstance(event))
                consumer(event as T)
        }
    }.also { register(it) }
}
package com.breadmoirai.awoobot.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import kotlin.reflect.KClass

class EventAwaiter<T : GenericEvent, R>(
    private val type: KClass<T>,
    private val jda: JDA,
    private val transform: suspend (T) -> R,
    private var filters: MutableList<suspend (T) -> Boolean> = mutableListOf(),
    private var stops: MutableList<suspend (T) -> Boolean> = mutableListOf(),
    private var cancels: MutableList<suspend () -> Unit> = mutableListOf()
) {
    fun filter(predicate: suspend (T) -> Boolean): EventAwaiter<T, R> {
        filters.add(predicate)
        return this
    }

    private var count: Int = 0
    fun until(limit: Int): EventAwaiter<T, R> {
        return until {
            ++count >= limit
        }
    }

    fun until(condition: suspend (T) -> Boolean): EventAwaiter<T, R> {
        stops.add(condition)
        return this
    }

    fun <U> map(transform: suspend (R) -> U): EventAwaiter<T, U> {
        return EventAwaiter(type, jda, { t -> transform(this.transform(t)) }, filters, stops)
    }

    suspend fun awaitNext(): R {
        val deferred = CompletableDeferred<R>()
        jda.listener(type) { event: T ->
            if (filters.all { predicate -> predicate(event) }) {
                cancel()
                deferred.complete(transform(event))
            }
        }
        return deferred.await()
    }

    fun subscribe(action: suspend (R) -> Unit): Deferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        jda.listener(type) { event: T ->
            if (filters.all { predicate -> predicate(event) }) {
                action(transform(event))
                if (stops.all { stop -> stop(event) }) {
                    cancel()
                    cancels.forEach { it() }
                    deferred.complete(Unit)
                }
            }
        }
        return deferred
    }

    fun onCancel(function: () -> Unit): EventAwaiter<T, R> {
        cancels.add(function)
        return this
    }
}

@JvmName("matchingSlash")
fun EventAwaiter<SlashCommandInteractionEvent, SlashCommandInteractionEvent>.matching(name: String): EventAwaiter<SlashCommandInteractionEvent, SlashCommandInteractionEvent> {
    return filter { it.name == name }
}

@JvmName("matchingButton")
fun EventAwaiter<ButtonInteractionEvent, ButtonInteractionEvent>.matching(id: String): EventAwaiter<ButtonInteractionEvent, ButtonInteractionEvent> {
    return filter { it.button.id == id }
}

fun EventAwaiter<ButtonInteractionEvent, ButtonInteractionEvent>.matchingPrefix(id: String): EventAwaiter<ButtonInteractionEvent, ButtonInteractionEvent> {
    return filter { it.button.id?.startsWith(id) == true }
}

fun EventAwaiter<ButtonInteractionEvent, ButtonInteractionEvent>.parseIntSuffix(): EventAwaiter<ButtonInteractionEvent, Pair<ButtonInteractionEvent, Int>> {
    return map { event ->
        val id = event.button.id!!
        val lastNonDigit = id.indexOfLast { !it.isDigit() }
        event to id.substring(lastNonDigit + 1).toInt()
    }
}

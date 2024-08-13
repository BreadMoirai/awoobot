package com.breadmoirai.awoobot

import com.breadmoirai.awoobot.util.EventAwaiter
import com.breadmoirai.awoobot.util.matching
import com.breadmoirai.awoobot.util.matchingPrefix
import com.breadmoirai.awoobot.util.parseIntSuffix
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object AwooBot {
    lateinit var jda: JDA

    suspend inline fun <reified T : GenericEvent> awaitEvent(noinline predicate: (T) -> Boolean): T {
        return listenFor<T>().filter(predicate).awaitNext()
    }

    suspend inline fun awaitButton(vararg ids: String): ButtonInteractionEvent {
        return awaitEvent { event -> ids.any { id -> event.button.id?.startsWith(id) ?: false } }
    }

    inline fun <reified T : GenericEvent> listenFor(): EventAwaiter<T, T> {
        return EventAwaiter(T::class, jda, {s -> s})
    }

    suspend inline fun awaitIndexedButton(prefix: String): Pair<ButtonInteractionEvent, Int> {
        return listenFor<ButtonInteractionEvent>()
            .matchingPrefix(prefix)
            .parseIntSuffix()
            .awaitNext()
    }

    fun createCommands() {
        println("Creating Commands")
        jda.updateCommands {
            slash("awoo", "Start a werewolf game")
        }.queue()
        jda.getGuildById("1016620556252090408")!!.upsertCommand("ping", "pongs").queue()
        jda.getGuildById("1016620556252090408")!!.upsertCommand("awoo", "Start a werewolf game").queue()
        jda.getGuildById("1016620556252090408")!!.upsertCommand("awoot", "Start a werewolf game").queue()
    }

    fun initializaListeners() {
        println("Initializing Listeners")
        listenFor<SlashCommandInteractionEvent>().matching("ping").subscribe { event -> event.reply("pong").queue() }
        listenFor<SlashCommandInteractionEvent>().matching("awoo").subscribe { event ->
            WerewolfLobby("Werewolf Game #0", event).openLobby()
        }
        listenFor<SlashCommandInteractionEvent>().matching("awoot").subscribe { event ->
            WerewolfLobby("Werewolf Game #0", event).openFakeLobby()
        }
        listenFor<ButtonInteractionEvent>().subscribe { event -> println("${event.button.id} was clicked by ${event.member!!.effectiveName}") }
    }

}
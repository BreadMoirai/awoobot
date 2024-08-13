package com.breadmoirai.awoobot

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent

fun main(args: Array<String>) {
    val jda =
        light(args[0], enableCoroutines = true) {
            intents += GatewayIntent.fromEvents()
        }
    println(
        jda.getInviteUrl(
            Permission.MESSAGE_SEND,
            Permission.CREATE_PUBLIC_THREADS,
            Permission.MESSAGE_SEND_IN_THREADS,
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_MANAGE,
            Permission.MANAGE_THREADS
        )
    )
    AwooBot.jda = jda
    AwooBot.jda.listener<ReadyEvent> {
        AwooBot.createCommands()
        AwooBot.initializaListeners()
    }
}

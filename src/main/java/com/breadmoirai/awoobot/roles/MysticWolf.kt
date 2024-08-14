package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.WerewolfGame

class MysticWolf : Werewolf() {
    override val description: String = "Sees a another player's card"
    override val altOrder: Int = 24

    override suspend fun altNightAction(game: WerewolfGame, player: MemberPlayer) {
        val (event, target) = game.targetPlayer(
            player,
            "Select a player to view their card",
            "$id-player",
            false
        )
        event.reply("$target's card is a ${target.card}").setEphemeral(true).queue()
        game.nightHistory.add("${player.role} $player saw that $target is ${target.card}")
    }
}
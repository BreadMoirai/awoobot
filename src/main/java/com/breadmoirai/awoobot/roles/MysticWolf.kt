package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.WerewolfGame

class MysticWolf : Werewolf() {
    override val description: String = "Sees a another player's card"
    override val nightOrder: Int = 24

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        super.nightAction(game, player)
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
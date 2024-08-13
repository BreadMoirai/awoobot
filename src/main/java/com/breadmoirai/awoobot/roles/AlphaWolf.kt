package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.WerewolfGame

class AlphaWolf : Werewolf() {
    override val description: String = "Gives another player the center werewolf card"
    override val nightOrder: Int = 22

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        super.nightAction(game, player)
        val (event, target) = game.targetPlayer(
            player,
            "Select a player to give the center werewolf card",
            "$id-player",
            false
        )
        event.reply("You have given $target the center werewolf card").setEphemeral(true).queue()
        game.nightHistory.add("${player.role} $player swapped ${game.center[3]}'s ${game.center[3].card} with ${target}'s ${target.card}")
        game.swapCards(target, game.center[3])
    }
}
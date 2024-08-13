package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class Witch : Role() {
    override val description: String = ""
    override val team: Team = Team.Villager
    override val nightOrder: Int = 80

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val (peekEvent, center) = game.targetCenter(
            player,
            "## ${player.role}\nView a card from the center?",
            "$id-peek"
        )
        peekEvent.reply("You seen ${center.card}").setEphemeral(true).queue()

        val (swapEvent, target) = game.targetPlayer(
            player,
            "Select a player to give ${center.card}",
            "$id-swap",
            true
        )
        swapEvent.reply("You gave ${center.card} to $target").setEphemeral(true).queue()

        game.nightHistory.add("${player.role} $player witched $target into $center ${center.card}")

        game.swapCards(player, center)
    }
}


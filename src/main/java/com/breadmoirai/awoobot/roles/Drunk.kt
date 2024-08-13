package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class Drunk: Role() {
    override val description: String = "When the Drunk wakes up, they exchange their card with a card in the center. They DO NOT look at their new card."
    override val team: Team = Team.Villager
    override val nightOrder: Int = 80

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val (pressEvent, target) = game.targetCenter(
            player,
            "## ${player.role}\nWhat card would you like to swap?",
            id
        )
        pressEvent.reply("You have stolen $target").setEphemeral(true).queue()

        game.swapCards(player, target)
        game.nightHistory.add("${player.role} $player drunked into $target's ${target.card}")
    }
}


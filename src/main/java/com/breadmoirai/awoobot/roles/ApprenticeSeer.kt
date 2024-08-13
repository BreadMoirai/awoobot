package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class ApprenticeSeer() : Role() {
    override val description: String = "Sees a center card"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 52

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val (pressEvent, center) = game.targetCenter(player, "## ${player.role}\nWhat card would you like to see?", id)

        pressEvent.reply("OK!, you saw $center ${center.card}").setEphemeral(true).queue()
        game.nightHistory.add("${player.role} $player saw $center ${center.card}")
    }
}
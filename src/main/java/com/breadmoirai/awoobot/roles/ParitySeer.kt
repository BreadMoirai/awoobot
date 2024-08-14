package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import com.breadmoirai.awoobot.util.joinToOxford

class ParitySeer() : Role() {
    override val description: String = "Sees if any 2 players are on the same team"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 54

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val targetPlayerFlow =
            game.targetPlayers(
                player,
                "## ${player.role}\nSelect 2 players to compare teams",
                id,
                false,
                2
            )
        val targets = mutableListOf<MemberPlayer>()
        targetPlayerFlow.collect { (event, target) ->
            targets += target
            if (targets.size == 1) {
                event.reply("You have selected $target").setEphemeral(true).queue()
            } else {
                if (targets.map { it.team }.toSet().size == 1) {
                    event.reply("${targets.joinToOxford()} are on the same team").setEphemeral(true).queue()
                } else {
                    event.reply("${targets.joinToOxford()} are NOT on the same team").setEphemeral(true).queue()
                }
            }
        }
    }

}
package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class Troublemaker() : Role() {
    override val description: String =
        "When the Troublemaker wakes up, they can switch 2 other player's cards, without looking at them. They cannot swap their own card"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 70

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val targetPlayerFlow =
            game.targetPlayers(
                player,
                "## ${player.role}\nSelect 2 players to swap cards",
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
                val (targetA, targetB) = targets
                event.reply("You have swapped $targetA and $targetB").setEphemeral(true).queue()
                game.swapCards(targetA, targetB)
                game.nightHistory.add("${player.role} $player swapped $targetA and $targetB")
            }
        }


    }
}
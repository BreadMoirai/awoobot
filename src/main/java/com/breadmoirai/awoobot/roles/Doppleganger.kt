package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class Doppleganger : Role() {
    override val description: String = "Look at someone's card and then copy it. Do her role immediately"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 10

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val (pressEvent, target) = game.targetPlayer(
            player,
            "## ${player.role}\nWho would you like to steal?",
            id,
            false
        )
        pressEvent.reply("You have copied ${target}'s ${target.card}").setEphemeral(true).queue()
        player.refCard = target.card
        player.team = target.team

        game.nightHistory.add("${player.role} $player doppleganger'd $target ${target.card}")
        when (target.card) {
            is AlphaWolf, is ApprenticeSeer, is DreamWolf, is Drunk, is Mason, is MysticWolf, is ParitySeer, is Rascal, is Robber, is Seer, is Troublemaker, is Witch, is Werewolf -> target.role.nightAction(game, player)
            is Insomniac -> {
                player.nightOrder = target.nightOrder!! + 1
                game.wakeupQueue.add(player)
            }
        }
    }
}


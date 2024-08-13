package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

class Robber: Role() {
    override val description: String = "Swap cards with another player and then see what it is"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 60

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val (pressEvent, target) = game.targetPlayer(
            player,
            "## ${player.role}\nWhose card would you like to steal?",
            id,
            false
        )
        pressEvent.reply("You have stolen ${target}'s ${target.card}").setEphemeral(true).queue()
        game.swapCards(player, target)
        game.nightHistory.add("${player.role} $player robbed ${player.card} from $target")
    }
}

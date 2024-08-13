package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import com.breadmoirai.awoobot.util.joinToOxford

open class Werewolf(val _id: String? = null) : Role() {

    override val id: String
        get() = _id ?: super.id

    override val description: String =
        "Wakes alongside other werewolves. *If alone, they can look at one card in the center."
    override val team: Team = Team.Werewolf
    override val nightOrder: Int? = 20

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val werewolves = game.players.filter { it.team == Team.Werewolf }
        val fellows = werewolves.filter { otherWerewolf -> otherWerewolf != player }
        if (fellows.isEmpty()) {

        }
        val fellowsDisplay = fellows.joinToOxford()
        player.hook.sendMessage(
            "## ${player.role}\nYour fellow werewolves are $fellowsDisplay"
        ).setEphemeral(true).queue()
        game.nightHistory.add("${player.role} $player woke up and saw fellow werewolves $fellowsDisplay")
    }
}



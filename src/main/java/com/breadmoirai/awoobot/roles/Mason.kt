package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame

open class Mason(val _id: String? = null) : Role() {

    override val id: String
        get() = _id ?: super.id

    override val description: String =
        "Wakes alongside other masons"
    override val team: Team = Team.Villager
    override val nightOrder: Int? = 20

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val masons = game.players.filter { it.role is Mason }
        val fellow = masons.singleOrNull { otherMason -> otherMason != player }
        if (fellow == null) {
            player.hook.sendMessage(
                "## ${player.role}\nYou are a solo mason"
            ).setEphemeral(true).queue()
            game.nightHistory.add("${player.role} $player woke up alone")
        } else {
            player.hook.sendMessage(
                "## ${player.role}\nYour fellow mason is $fellow"
            ).setEphemeral(true).queue()
            game.nightHistory.add("${player.role} $player woke up with their fellow mason $fellow ${fellow.card}")
        }
    }
}



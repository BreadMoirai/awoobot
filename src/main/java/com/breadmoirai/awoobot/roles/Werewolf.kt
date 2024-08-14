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
    open val altOrder: Int? = null
    var woke: Boolean = false
    override val nightOrder: Int?
        get() = (if (!woke) 20 else altOrder)

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        if (woke) {
            altNightAction(game, player)
            return
        }
        val werewolves = game.players.filter { it.team == Team.Werewolf }
        val fellows = werewolves.filter { otherWerewolf -> otherWerewolf != player }
        if (fellows.isEmpty()) {
            val (pressEvent, center) = game.targetCenter(
                player, "## Lone ${player.role}\nWhich center card would you like to see?", id
            )
            pressEvent.reply("OK!, you saw $center ${center.card}").setEphemeral(true).queue()
            game.nightHistory.add("${player.role} $player saw $center ${center.card}")
        }
        val fellowsDisplay = fellows.joinToOxford()
        player.hook.sendMessage(
            "## ${player.role}\nYour fellow werewolves are $fellowsDisplay"
        ).setEphemeral(true).queue()
        game.nightHistory.add("${player.role} $player woke up and saw fellow werewolves $fellowsDisplay")
        woke = true
        if (altOrder != null) {
            game.wakeupQueue.add(player)
        }
    }

    open suspend fun altNightAction(game: WerewolfGame, player: MemberPlayer) {

    }
}



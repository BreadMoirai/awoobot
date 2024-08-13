package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate

class Insomniac : Role() {
    override val description: String = "When the Insomniac wakes up, they look at their card to see if it has changed."
    override val team: Team = Team.Villager
    override val nightOrder: Int = 90

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        player.hook.sendMessage(MessageCreate {
            content = "## ${player.role}\nYou have woken up as ${player.card}"
        }).setEphemeral(true).await()
        game.nightHistory.add("${player.role} $player woke up as ${player.card}")
    }
}
package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.AwooBot
import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

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
            }
        }

    }

}
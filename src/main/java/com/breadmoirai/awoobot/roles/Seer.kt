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

class Seer() : Role() {
    override val description: String = "Sees 2 center cards or sees a player's card"
    override val team: Team = Team.Villager
    override val nightOrder: Int = 50

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        val centerId: String = "$id-do-center"
        val playersId: String = "$id-do-player"
        val message1 = player.hook.sendMessage(MessageCreate {
            content = "## ${player.role}\nWhat would you like to do?"
            val buttons: MutableList<Button> = mutableListOf()
            buttons += button(centerId, "View 2 center cards")
            buttons += button(playersId, "View another players card")
            actionRow(buttons)
        }).setEphemeral(true).await()

        val pressEvent: ButtonInteractionEvent = AwooBot.awaitButton(centerId, playersId)
        val pressedButton: String = pressEvent.button.id!!
        message1.delete().queue()
        // Peek at 2 cards from the center
        if (pressedButton == centerId) {
            game.targetCenters(
                player,
                "## ${player.role}\nWhat two cards from the center would you like to see?",
                "$id-center",
                2
            ).collect { (event, center) ->
                event.reply("You saw $center ${center.card}").setEphemeral(true).queue()
                game.nightHistory.add("${player.role} $player saw $center ${center.card}")
            }
        }
        // Peek at another player's card
        if (pressedButton == playersId) {
            val (pressEvent2, target) = game.targetPlayer(
                player,
                "Select a player to view their card",
                "$id-player",
                false
            )
            pressEvent2.reply("$target's card is a ${target.card}").setEphemeral(true).queue()
            game.nightHistory.add("${player.role} $player saw that $target is ${target.card}")
        }
    }

}
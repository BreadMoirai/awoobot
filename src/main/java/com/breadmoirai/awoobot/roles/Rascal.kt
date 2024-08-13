package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import kotlin.random.Random
import kotlin.random.nextInt

class Rascal : Role() {
    override val description: String = """
        | Randomly does one of the following options
        | - View another player's card and gives it to another player
        | - Takes another player's card without looking at it
        | - Exchanges two player's cards
        | - Takes another player's card and looks at their new card""".trimMargin()
    override val team: Team = Team.Villager
    override val nightOrder: Int = 60

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        when (Random.nextInt(0..3)) {
            0 -> {
                val (peekEvent, targetA) = game.targetPlayer(
                    player,
                    "## ${player.role}\nView another player's card and give it to another player",
                    "$id-witch-peek",
                    false
                )
                peekEvent.reply("You saw $targetA's ${targetA.card}").setEphemeral(true).queue()

                val (swapEvent, targetB) = game.targetPlayer(
                    player,
                    "Select a player to give ${targetA.card}",
                    "$id-witch-give",
                    false
                )
                swapEvent.reply("You gave ${targetA.card} to $targetB").setEphemeral(true).queue()
                game.nightHistory.add("${player.role} $player viewed $targetA's ${targetA.card} and swapped it with $targetB ${targetB.card}")
                game.swapCards(targetA, targetB)
            }

            1 -> {
                val (peekEvent, target) = game.targetPlayer(
                    player,
                    "## ${player.role}\nSelect another player to exchange cards with",
                    "$id-drunk",
                    false
                )
                peekEvent.reply("You swapped your card with $target").setEphemeral(true).queue()
                game.nightHistory.add("${player.role} $player swapped cards with $target ${target.card}")
                game.swapCards(player, target)
            }

            2 -> {
                val targetPlayerFlow = game.targetPlayers(
                    player,
                    "## ${player.role}\nSelect 2 players to swap cards",
                    "troublmaker-swap",
                    true,
                    2
                )
                val targets = mutableListOf<MemberPlayer>()
                targetPlayerFlow.collect { (event, target) ->
                    event.reply("You have selected $target").setEphemeral(true).queue()
                    targets += target
                }

                val (targetA, targetB) = targets
                game.swapCards(targetA, targetB)
                game.nightHistory.add("${player.role} $player swapped $targetA and $targetB")
            }

            3 -> {
                val (pressEvent, target) = game.targetPlayer(
                    player,
                    "## ${player.role}\nWhose card would you like to steal?",
                    "$id-steal",
                    false
                )
                pressEvent.reply("You have stolen ${target}'s ${target.card}").setEphemeral(true).queue()
                game.swapCards(player, target)
                game.nightHistory.add("${player.role} $player robbed ${player.card} from $target")
            }
        }

    }
}

package com.breadmoirai.awoobot

import com.breadmoirai.awoobot.roles.Hunter
import com.breadmoirai.awoobot.roles.Role
import com.breadmoirai.awoobot.util.joinToOxford
import com.breadmoirai.awoobot.util.matching
import com.breadmoirai.awoobot.util.matchingPrefix
import com.breadmoirai.awoobot.util.parseIntSuffix
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class WerewolfGame(
    val id: String,
    val thread: ThreadChannel,
    playerSet: Set<Member>,
    roleSet: Set<Role>,
    centerWerewolf: Role? = null
) {
    val players: List<MemberPlayer> = run {
        val roleStack = roleSet.toMutableList().shuffled()
        playerSet.toList().shuffled().mapIndexed { num, member -> MemberPlayer(member, num + 1, roleStack[num]) }
    }
    val center: List<CenterPlayer> = run {
        val rolesInPlay = players.map(MemberPlayer::role).toSet()
        val centerRoles = roleSet.minus(rolesInPlay).toMutableList().shuffled().toMutableList()
        if (centerWerewolf != null) {
            centerRoles.add(centerWerewolf)
        }
        centerRoles.mapIndexed { pos, r -> CenterPlayer(pos + 1, r) }
    }
    val wakeupQueue: PriorityQueue<MemberPlayer> = PriorityQueue(Comparator.comparingInt { p -> p.nightOrder!! })
    val votes: MutableMap<MemberPlayer, MemberPlayer> = mutableMapOf()
    val nightHistory: MutableList<String> = mutableListOf()

    suspend fun runGame() {
        readyPhase()
        startPhase()
        nightPhase()
        dayPhase()
        votePhase()
        endPhase()
    }

    fun swapCards(first: Player, second: Player) {
        first.card = second.card.also { second.card = first.card } //swap cards
        first.team = second.team.also { second.team = first.team } //swap teams
    }

    /**
     *
     * Refresh interaction hook for all players
     * TODO: Reset readyPhase if 5 minutes go by without all players pressing ready
     *
     */
    suspend fun readyPhase() {
        val readyMessage = thread.sendMessage(MessageCreate {
            content = "Please ready up! Waiting for \n${players.joinToOxford { it.member.asMention }}"
            actionRow(button("$id-ready-phase", "Ready", style = ButtonStyle.SUCCESS))
        }).await()
        AwooBot.listenFor<ButtonInteractionEvent>()
            .matching("$id-ready-phase")
            .until { players.all(MemberPlayer::hasHook) }
            .onCancel {
                readyMessage.delete().queue()
            }
            .subscribe { event ->
                for (player in players) {
                    if (player.member.id == event.member!!.id) {
                        player.hook = event.hook
                    }
                }
                event.reply("You are ready!").setEphemeral(true).queue()
            }.await()
    }

    private fun startPhase() {
        for (player in players) {
            player.hook.sendMessage(MessageCreate {
                content = "You have gotten the card ${player.card}"
            }).setEphemeral(true).queue()
        }
    }


    suspend fun nightPhase() {
        mutePlayers(true)
        for (p in players) {
            if (p.nightOrder != null)
                wakeupQueue.add(p)
        }

        while (wakeupQueue.isNotEmpty()) {
            val player = wakeupQueue.remove()
            println("Taking nightAction for $player ${player.role}")
            player.role.nightAction(this, player)
        }
    }

    private suspend fun dayPhase() {
        mutePlayers(false)
        val voteTime = Instant.now().plus(6.minutes.toJavaDuration())
        thread.sendMessage("@here Voting ends ${TimeFormat.RELATIVE.format(voteTime)}").queue()
        delay(6.minutes)
    }

    private suspend fun votePhase() {
        mutePlayers(true)
        val message = thread.sendMessage(MessageCreate {
            content = "# Time to vote!"
            for (playerChunk in players.chunked(5)) {
                val buttons: MutableList<ItemComponent> = mutableListOf()
                for (p in playerChunk) {
                    buttons += button(id = "$id-vote-${p.number}", label = p.name)
                }
                actionRow(buttons)
            }
        }).await()
        AwooBot.listenFor<ButtonInteractionEvent>()
            .matchingPrefix("$id-vote-")
            .parseIntSuffix()
            .map { (event, id) -> event to (players.find { it.member.id == event.user.id } to players[id - 1]) }
            .until {
                votes.size == players.size
            }
            .onCancel {
                message.delete().queue()
            }.subscribe { (event, players) ->
                val (voter, votee) = players
                if (voter == null) {
                    event.reply("YOU AREN'T PLAYING").setEphemeral(true).queue()
                    return@subscribe
                }
                if (voter == votee) {
                    event.reply("You cannot vote for yourself...").setEphemeral(true).queue()
                    return@subscribe
                }
                event.reply("You have voted for $votee").setEphemeral(true).queue()
                votes[voter] = votee
            }.await()

        val voteCount = mutableMapOf<MemberPlayer, Int>()
        for (voted in votes.values) {
            voteCount[voted] = voteCount.getOrDefault(voted, 0) + 1
        }
        val maxVotes = voteCount.values.maxOrNull() ?: 0
        val winners = voteCount.filterValues { it == maxVotes }.keys.toMutableList()
        val hunter = winners.find { p -> p.role is Hunter }
        if (hunter != null) {
            //whoever hunter voted for dies
            winners.add(votes[hunter]!!)
        }

        thread.sendMessage(buildString {
            append("The $winners are dead with $maxVotes votes!")
            append("Vote Tally \n")
            for (player in players) {
                append("$player : ${voteCount[player]}\n")
            }
        }).queue()

        thread.sendMessage(buildString {
            if (winners.any { p -> p.team == Team.Tanner }) {
                append("Tanners win")
            } else if (winners.any { p -> p.team == Team.Werewolf }) {
                append("Villagers won!")
            } else if (winners.any { p -> p.team == Team.Villager }) {
                append("Werewolfs win")
            }
        }).queue()
    }

    private fun endPhase() {
        mutePlayers(false)
        thread.sendMessage(buildString {
            append("# Game History\n")
            append("## Start\n")
            for (player in players) {
                append("${player}\t ${player.role}\n")
            }
            for (c in center) {
                append("$c\t ${c.role}\n")
            }
            append("## Night Phase\n")
            for (action in nightHistory) {
                append(action)
                append("\n")
            }
            append("## End\n")
            for (player in players) {
                append("${player}\t ${player.card}\n")
            }
            for (c in center) {
                append("$c\t ${c.card}\n")
            }
        }).queue()
        thread.manager.setArchived(true).queue()
    }

    suspend fun targetCenter(
        source: MemberPlayer,
        prompt: String,
        interactionId: String
    ): Pair<ButtonInteractionEvent, CenterPlayer> {
        source.hook.sendMessage(MessageCreate {
            content = prompt
            val buttons: MutableList<ItemComponent> = mutableListOf()
            for (i in center.indices) {
                buttons += button("$id-$interactionId-$i", "${i + 1}")
            }
            actionRow(buttons)
        }).setEphemeral(true).queue()

        val (pressEvent, centerIdx) = AwooBot.awaitIndexedButton("$id-$interactionId-")
        return pressEvent to center[centerIdx]
    }


    suspend fun targetCenters(
        source: MemberPlayer,
        prompt: String,
        interactionId: String,
        count: Int
    ): Flow<Pair<ButtonInteractionEvent, CenterPlayer>> {
        val message = source.hook.sendMessage(MessageCreate {
            content = prompt
            val buttons: MutableList<ItemComponent> = mutableListOf()
            for (i in center.indices) {
                buttons += button("$id-$interactionId-$i", "${i + 1}")
            }
            actionRow(buttons)
        }).setEphemeral(true).await()
        return flow {
            val listener = AwooBot.listenFor<ButtonInteractionEvent>()
                .matchingPrefix("$id-$interactionId-")
                .until(count)
                .parseIntSuffix()
                .map { (event, i) -> event to center[i] }
                .onCancel {
                    message.delete().queue()
                }
            repeat(count) {
                emit(listener.awaitNext())
            }
        }
    }

    suspend fun targetPlayer(
        source: MemberPlayer,
        prompt: String,
        interactionId: String,
        includeSelf: Boolean
    ): Pair<ButtonInteractionEvent, MemberPlayer> {
        val message = source.hook.sendMessage(MessageCreate {
            content = prompt
            val otherPlayers = if (includeSelf) players else players.filter { p -> p != source }
            for (playerChunk in otherPlayers.chunked(5)) {
                val buttons: MutableList<ItemComponent> = mutableListOf()
                for (p in playerChunk) {
                    buttons += button(id = "$id-$interactionId-${p.number}", label = p.name)
                }
                actionRow(buttons)
            }
        }).setEphemeral(true).await()
        val (pressEvent, num) = AwooBot.awaitIndexedButton("$id-$interactionId-")
        val target = players[num - 1]
        message.delete().queue()
        return pressEvent to target
    }

    suspend fun targetPlayers(
        source: MemberPlayer,
        prompt: String,
        interactionId: String,
        includeSelf: Boolean,
        count: Int
    ): Flow<Pair<ButtonInteractionEvent, MemberPlayer>> {
        val message = source.hook.sendMessage(MessageCreate {
            content = prompt
            val otherPlayers = if (includeSelf) players else players.filter { p -> p != source }
            for (playerChunk in otherPlayers.chunked(5)) {
                val buttons: MutableList<ItemComponent> = mutableListOf()
                for (p in playerChunk) {
                    buttons += button(id = "$id-$interactionId-${p.number}", label = p.name)
                }
                actionRow(buttons)
            }
        }).setEphemeral(true).await()
        return flow {
            val listener = AwooBot.listenFor<ButtonInteractionEvent>()
                .matchingPrefix("$id-$interactionId-")
                .until(count)
                .parseIntSuffix()
                .map { (event, id) -> event to players[id - 1] }
                .onCancel {
                    message.delete().queue()
                }
            repeat(count) {
                emit(listener.awaitNext())
            }
        }
    }

    private fun mutePlayers(mute: Boolean) {
        for (p in players) {
            p.member.guild.mute(p.member, mute).queue({}, { err ->
                if (err is ErrorResponseException) {
                    if (err.errorCode == 40032) {
                        return@queue
                    }
                }
                err.printStackTrace()
            })
        }

    }
}




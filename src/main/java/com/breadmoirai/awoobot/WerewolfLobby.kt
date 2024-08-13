package com.breadmoirai.awoobot

import com.breadmoirai.awoobot.roles.Role
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.CompletableFuture
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class WerewolfLobby(val name: String, val event: SlashCommandInteractionEvent) {

    val id: String = event.id
    val creator: Member = event.member!!
    val channel: TextChannel = event.messageChannel as TextChannel
    val players: MutableSet<Member> = mutableSetOf()
    val isOpen: Boolean = true
    val roleSet: MutableSet<Role> = mutableSetOf()
    private val roleEditMsgs: MutableList<Message> = mutableListOf()
    private lateinit var thread: ThreadChannel
    private lateinit var hook: InteractionHook
    private var editAction: CompletableFuture<Message>? = null
    private var listeners: MutableList<CoroutineEventListener> = mutableListOf()

    suspend fun openLobby() {
        hook = event.reply(displayLobby()).await()
        val response = hook.retrieveOriginal().await()
        thread = response.createThreadChannel(name).await()
        thread.sendMessage(MessageCreate {
            components += row(button("$id-start-game", "Start Game", style = ButtonStyle.SUCCESS))
        }).queue()
        for (roleChunk in Role.roles.chunked(20)) {
            val roleMsg = thread.sendMessage(MessageCreate {
                content = "Set roles"
                for (rowChunk in roleChunk.chunked(5)) {
                    val buttons: MutableList<ItemComponent> = mutableListOf()
                    for (role in rowChunk) {
                        buttons += button(
                            id = "$id-roleset-${role.id}-add",
                            label = role.name,
                            style = ButtonStyle.SECONDARY
                        )
                    }
                    actionRow(buttons)
                }
            }).await()
            roleEditMsgs.add(roleMsg)
        }
        listeners += addRoleSetChangeListener()
        listeners += addPlayerChangeListener()
        listeners += addGameStartListener()
    }

    private fun addPlayerChangeListener(): CoroutineEventListener {
        return AwooBot.jda.listener<ButtonInteractionEvent> { event ->
            if (event.button.id == "$id-join") {
                if (players.add(event.member!!)) {
                    event.reply("You have joined the game!").setEphemeral(true).queue()
                    updateLobby()
                } else {
                    event.reply("You cannot join a game you are already in.").setEphemeral(true).queue()
                }
            }
            if (event.button.id == "$id-leave") {
                if (players.remove(event.member!!)) {
                    event.reply("You have left the game :(").setEphemeral(true).queue()
                    updateLobby()
                } else {
                    event.reply("You cannot leave a game you are not in.").setEphemeral(true).queue()
                }
            }
        }
    }

    private fun addRoleSetChangeListener(): CoroutineEventListener {
        return AwooBot.jda.listener<ButtonInteractionEvent> { event ->
            val btnId = event.button.id
            if (btnId?.startsWith("$id-roleset") != true) return@listener
            val isAdd = btnId.endsWith("add")
            val btnRoleId = btnId.removePrefix("$id-roleset-")
            val role = Role.roles.find { r -> btnRoleId.startsWith(r.id) }!!
            val changed = if (isAdd) {
                println("${event.member!!.effectiveName} add ${role.id}")
                roleSet.add(role)
            } else {
                println("${event.member!!.effectiveName} remove ${role.id}")
                roleSet.remove(role)
            }
            if (!changed) {
                val nah = event.reply("nah").setEphemeral(true).await()
                delay(3.seconds)
                nah.deleteOriginal().queue()
                return@listener
            }
            val msg = event.deferEdit().queue()
            for ((idx, roleChunk) in Role.roles.chunked(20).withIndex()) {
                if (role in roleChunk) {
                    val rows: MutableList<LayoutComponent> = mutableListOf()
                    for (rowChunk in roleChunk.chunked(5)) {
                        val buttons: MutableList<ItemComponent> = mutableListOf()
                        for (r in rowChunk) {
                            val inSet = r in roleSet
                            buttons += button(
                                id = "$id-roleset-${r.id}-${if (inSet) "remove" else "add"}",
                                label = r.name,
                                style = if (inSet) ButtonStyle.SUCCESS else ButtonStyle.SECONDARY
                            )
                        }
                        rows.add(ActionRow.of(buttons))
                    }
                    roleEditMsgs[idx].editMessageComponents(rows).queue()
                }
            }
            updateLobby()
        }
    }

    private fun addGameStartListener(): CoroutineEventListener {
        return AwooBot.jda.listener<ButtonInteractionEvent> { event ->
            if (event.button.id != "$id-start-game") return@listener
            AwooBot.jda.removeEventListener(*listeners.toTypedArray())
            hook.editOriginalComponents().queue()
            event.deferEdit().queue()
            event.message.delete().queue()
            for (roleEditMsg in roleEditMsgs) {
                roleEditMsg.delete().queue()
            }
            WerewolfGame(id, thread, players, roleSet).runGame()
        }
    }

    private fun updateLobby() {
        editAction?.cancel(false)
        editAction = hook.editOriginal(editLobby()).submit()
    }

    private fun displayLobby() = MessageCreate {
        embed {
            title = "Awooo"
            author(
                name = creator.effectiveName,
                iconUrl = creator.effectiveAvatarUrl
            )
            image = "https://images-na.ssl-images-amazon.com/images/I/81u-IzSuNnL.jpg"
            description = "Join now!"
            field("Players - 0")
            field("Roles - 0")
        }
        if (isOpen) {
            components += row(
                button(id = "$id-join", label = "Join", style = ButtonStyle.SUCCESS),
                button(id = "$id-leave", label = "Leave", style = ButtonStyle.DANGER)
            )
        }
    }

    private fun editLobby() = MessageEdit {
        embed {
            title = "Awooo"
            author(
                name = creator.effectiveName,
                iconUrl = creator.effectiveAvatarUrl
            )
            image = "https://images-na.ssl-images-amazon.com/images/I/81u-IzSuNnL.jpg"
            description = "Join now!"
            field("Players - ${players.size}", players.joinToString("\n") { it.effectiveName })
            field("Roles - ${roleSet.size}", roleSet.joinToString("\n") { it.name })
        }
        if (isOpen) {
            components += row(
                button(id = "$id-join", label = "Join", style = ButtonStyle.SUCCESS),
                button(id = "$id-leave", label = "Leave", style = ButtonStyle.DANGER)
            )
        }
    }

    suspend fun openFakeLobby() {
        val member = event.member!!
        repeat(5) { n ->
            val fakeName = "${member.effectiveName}#$n"
            players += fakeMember(member, fakeName)
        }
        openLobby()
    }

    private fun fakeMember(member: Member, fakeName: String): Member {
        val fake = spyk<Member>(member)
        every { fake.effectiveName } returns fakeName
        every { fake.hashCode() } returns Random.nextInt()
        return fake
    }

}

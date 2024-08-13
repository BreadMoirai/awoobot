package com.breadmoirai.awoobot

import com.breadmoirai.awoobot.roles.Role
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.InteractionHook

interface Player {
    var number: Int
    val role: Role
    var nightOrder: Int?
    var card: Role
    var team: Team
    var refCard: Role?
    override fun toString(): String
}
// GET BACK TO WORK
class MemberPlayer(val member: Member, override var number: Int, override val role: Role) : Player {
    override var nightOrder: Int? = role.nightOrder
    override var card: Role = role
    override var team: Team = role.team
    override var refCard: Role? = null
    lateinit var hook: InteractionHook

    override fun toString(): String {
        return "**${name}**"
    }

    fun hasHook(): Boolean {
        return this::hook.isInitialized
    }

    val name: String
        get() {
            return member.effectiveName
        }
}


class CenterPlayer(override var number: Int, override val role: Role,) : Player {
    override var nightOrder: Int? = null
    override var card: Role = role
    override var team: Team = role.team
    override var refCard: Role? = null

    override fun toString(): String {
        return "**Center #$number**"
    }

}

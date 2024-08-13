package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.Team
import com.breadmoirai.awoobot.WerewolfGame
import com.breadmoirai.awoobot.util.pascalCaseToKebabCase
import com.breadmoirai.awoobot.util.pascalCaseToTitleCase

/**
 * https://boardgamegeek.com/thread/2032064/pbf-master-lists-roles-marks-teams-etc
 *
 */
abstract class Role {
    /**
     * Id
     */
    open val id: String = this::class.simpleName!!.pascalCaseToKebabCase()

    /**
     * Name
     */
    open val name: String = this::class.simpleName!!.pascalCaseToTitleCase()

    /**
     * Description
     */
    abstract val description: String

    abstract val team: Team

    open val nightOrder: Int? = null

    open suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {}

    override fun toString(): String {
        return "__${name}__"
    }

    companion object {
        val roles: List<Role> = listOf(
            Villager("villager-a"),
            Villager("villager-b"),
            Villager("villager-c"),
            Mason("mason-a"),
            Mason("mason-b"),
            Werewolf("werewolf-a"),
            Werewolf("werewolf-b"),
            ApprenticeSeer(),
            Seer(),
            Robber(),
            Troublemaker(),
            Insomniac(),
            Drunk(),
            Doppleganger(),
            MysticWolf(),
            AlphaWolf(),
            Rascal(),
            Witch(),
            Hunter(),
        )
    }


}


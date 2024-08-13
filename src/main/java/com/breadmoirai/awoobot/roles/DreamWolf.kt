package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.MemberPlayer
import com.breadmoirai.awoobot.WerewolfGame

class DreamWolf : Werewolf() {
    override val description: String = "Sleeps at night"
    override val nightOrder: Int? = null

    override suspend fun nightAction(game: WerewolfGame, player: MemberPlayer) {
        // eep
    }
}
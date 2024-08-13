package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.Team

class Villager(override val id: String) : Role() {
    override val description: String = "Useless"
    override val team: Team = Team.Villager
}


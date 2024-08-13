package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.Team

class Hunter() : Role() {
    override val description: String = "If hunter dies, whoever they vote for also dies."
    override val team: Team = Team.Villager
}


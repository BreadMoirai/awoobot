package com.breadmoirai.awoobot.roles

import com.breadmoirai.awoobot.Team

class Tanner : Role() {
    override val description: String = "Wins by themself if they die"
    override val team: Team = Team.Tanner
}
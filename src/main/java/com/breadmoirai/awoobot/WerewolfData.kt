package com.breadmoirai.awoobot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement


object WerewolfData {

    @Serializable
    data class PlayerStats(var name: String, var wins: Int, var losses: Int, var roles: MutableMap<String, Int>, var nightTargetCount: Int)

    @Serializable
    data class GameStats(var gameCount: Int, var winsByTeam: MutableMap<Team, Int>)

    val playerStats: MutableMap<String, PlayerStats> = mutableMapOf()
    val gameStats: GameStats = GameStats(0, mutableMapOf())

    fun save(): JsonObject {
        return buildJsonObject {
            put("player_stats", Json.encodeToJsonElement(playerStats))
            put("game_stats", Json.encodeToJsonElement(gameStats))
        }
    }

    fun load(json: JsonObject) {
        playerStats.clear()
        json.getValue("player_stats")
    }
}

package com.breadmoirai.awoobot

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.*

@Serializable
object DataStore {

    private const val PATH = "awoobot_data.json"

    private var data: WerewolfData = WerewolfData

    init {
        load()
    }

    fun modify(player: String, label: String, dice: String) {
//        val newValue = data.getOrPut(player, ::mutableMapOf).set(label, dice)
        save()
    }

    fun get(player: String, label: String): String? {
//        return _players[player]?.get(label)
        return null
    }

    private fun save() {
        Path(PATH).writeText(Json.encodeToString(data))
    }

    private fun load() {
        val path = Path(PATH)
        if (path.exists()) {
            val localStash = Json.decodeFromString<String>(path.readText())
//            println("Loaded data from ${Path(DICE_VAULT_PATH).absolute()}")
        }
    }

}

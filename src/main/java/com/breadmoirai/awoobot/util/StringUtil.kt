package com.breadmoirai.awoobot.util

/**
 * Turns "AlphaWerewolf" into "Alpha Werewolf"
 *
 */
fun String.pascalCaseToTitleCase(): String {
    var firstChar = true
    return buildString {
        for (c in this@pascalCaseToTitleCase) {
            if (c.isUpperCase()) {
                if (!firstChar) {
                    append(' ')
                }
            }
            append(c)
            firstChar = false
        }
    }
}

/**
 * Turns "AlphaWerewolf" into "alpha-werewolf
 *
 */
fun String.pascalCaseToKebabCase(): String {
    var firstChar = true
    return buildString {
        for (c in this@pascalCaseToKebabCase) {
            if (c.isUpperCase()) {
                if (!firstChar) {
                    append('-')
                }
                append(c.lowercase())
            } else {
                append(c)
            }
            firstChar = false
        }
    }
}

fun <T> List<T>.joinToOxford(transform: (T) -> String = {"$it"}): String {
    return when (size) {
        1 -> {
            transform(first())
        }
        2 -> {
            "${transform(get(0))} and ${transform(get(1))}"
        }
        else -> {
            buildString {
                append(subList(0, size-1).joinToString(", ", transform = transform))
                append(", and ")
                append(transform(this@joinToOxford.last()))
            }
        }
    }
}
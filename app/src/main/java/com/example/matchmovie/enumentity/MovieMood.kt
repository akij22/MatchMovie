package com.example.matchmovie.enumentity

enum class MovieMood {
    HAPPY,
    SAD,
    ROMANTIC,
    DARK,
    COZY,
    MIND_BLOWING,
    FUNNY,
    SCARY,
    ACTION,
    ADVENTUROUS,
    SUSPENSEFUL,
    DRAMATIC,
    INFORMATIVE,
    PLAYFUL,
    NOSTALGIC,
    REALITY,
    RELAXED,
    NOT_SPECIFIED
}

fun MovieMood.displayName(): String {
    return name
        .lowercase()
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}

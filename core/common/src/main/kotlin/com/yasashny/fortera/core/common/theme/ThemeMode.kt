package com.yasashny.fortera.core.common.theme

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        val Default: ThemeMode = SYSTEM

        fun fromNameOrDefault(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: Default
    }
}

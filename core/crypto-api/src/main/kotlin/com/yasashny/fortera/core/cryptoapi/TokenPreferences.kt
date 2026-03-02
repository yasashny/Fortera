package com.yasashny.fortera.core.cryptoapi

import androidx.datastore.preferences.core.stringSetPreferencesKey

object TokenPreferences {
    val ENABLED_TOKENS_KEY = stringSetPreferencesKey("enabled_tokens")
}

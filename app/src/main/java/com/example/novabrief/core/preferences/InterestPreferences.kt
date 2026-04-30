package com.example.novabrief.core.preferences

import android.content.Context
import android.content.SharedPreferences

class InterestPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "novabrief_interests",
        Context.MODE_PRIVATE
    )

    fun saveInterests(interests: Set<String>) {
        prefs.edit().putStringSet("selected_interests", interests).apply()
    }

    fun getInterests(): Set<String> {
        return prefs.getStringSet("selected_interests", emptySet()) ?: emptySet()
    }

    fun hasSelectedInterests(): Boolean {
        return getInterests().isNotEmpty()
    }

    fun clearInterests() {
        prefs.edit().remove("selected_interests").apply()
    }
}

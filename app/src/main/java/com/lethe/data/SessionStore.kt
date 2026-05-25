package com.lethe.data

import android.content.Context
import android.net.Uri

class SessionStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("lethe-session", Context.MODE_PRIVATE)

    fun loadProcessed(): Set<Long> =
        prefs.getStringSet(KEY_PROCESSED, emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet()
            ?: emptySet()

    fun addProcessed(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        val current = prefs.getStringSet(KEY_PROCESSED, emptySet()) ?: emptySet()
        prefs.edit()
            .putStringSet(KEY_PROCESSED, current + ids.map { it.toString() })
            .apply()
    }

    fun loadPendingTrash(): List<Uri> =
        prefs.getStringSet(KEY_PENDING, emptySet())
            ?.map(Uri::parse)
            ?: emptyList()

    fun savePendingTrash(uris: List<Uri>) {
        prefs.edit()
            .putStringSet(KEY_PENDING, uris.map { it.toString() }.toSet())
            .apply()
    }

    fun clearPendingTrash() {
        prefs.edit().remove(KEY_PENDING).apply()
    }

    private companion object {
        const val KEY_PROCESSED = "processed_ids"
        const val KEY_PENDING = "pending_trash"
    }
}

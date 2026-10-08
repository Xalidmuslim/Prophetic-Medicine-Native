package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import org.json.JSONArray

class AppStore(context: Context) {
    private val preferences = context.getSharedPreferences("medicina_reader_state", Context.MODE_PRIVATE)
    var fontSize: Float
        get() = preferences.getFloat("fontSize", 19f)
        set(value) { preferences.edit().putFloat("fontSize", value.coerceIn(14f, 32f)).apply() }
    var darkMode: Boolean
        get() = preferences.getBoolean("darkMode", false)
        set(value) { preferences.edit().putBoolean("darkMode", value).apply() }
    var lastChapterId: String?
        get() = preferences.getString("lastChapterId", null)
        set(value) { preferences.edit().putString("lastChapterId", value).apply() }

    fun bookmarks(): Set<String> = loadSet("bookmarks")
    fun history(): List<String> = loadList("history")
    fun readChapters(): Set<String> = loadSet("readChapters")
    fun toggleBookmark(id: String): Set<String> {
        val next = bookmarks().toMutableSet()
        if (!next.add(id)) next.remove(id)
        preferences.edit().putString("bookmarks", JSONArray(next.toList()).toString()).apply()
        return next
    }
    fun rememberChapter(id: String) {
        lastChapterId = id
        val visited = readChapters().toMutableSet().apply { add(id) }
        preferences.edit().putString("readChapters", JSONArray(visited.toList()).toString()).apply()
        val next = history().toMutableList().apply { remove(id); add(0, id) }.take(100)
        preferences.edit().putString("history", JSONArray(next).toString()).apply()
    }
    private fun loadSet(key: String) = loadList(key).toSet()
    private fun loadList(key: String): List<String> {
        val array = try { JSONArray(preferences.getString(key, "[]")) } catch (_: Exception) { JSONArray() }
        return (0 until array.length()).mapNotNull { array.optString(it).takeIf { s -> s.isNotBlank() } }
    }
}

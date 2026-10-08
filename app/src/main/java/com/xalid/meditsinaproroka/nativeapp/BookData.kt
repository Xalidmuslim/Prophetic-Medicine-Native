package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BookBlock(val id: String, val type: String, val text: String, val anchor: String)
data class Chapter(
    val id: String, val title: String, val section: String,
    val blocks: List<BookBlock>, val topics: List<String>, val remedies: List<String>,
    val previousId: String?, val nextId: String?
)
data class Topic(val id: String, val title: String, val chapterIds: List<String>)
data class Remedy(val id: String, val title: String, val chapterIds: List<String>, val aliases: List<String>)
data class Treatment(val id: String, val condition: String, val chapterId: String, val category: String)
data class BookData(
    val title: String, val author: String, val chapters: List<Chapter>,
    val topics: List<Topic>, val remedies: List<Remedy>, val treatments: List<Treatment>,
    val hasFullText: Boolean, val expectedChapters: Int, val expectedTopics: Int,
    val expectedRemedies: Int
) {
    fun chapter(id: String) = chapters.firstOrNull { it.id == id }
    fun search(query: String): List<Chapter> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        return chapters.filter { c ->
            c.title.contains(q, ignoreCase = true) ||
                c.section.contains(q, ignoreCase = true) ||
                c.blocks.any { it.text.contains(q, ignoreCase = true) }
        }.take(150)
    }

    companion object {
        fun load(context: Context): BookData {
            val hasFullText = context.assets.list("")?.contains("book.json") == true
            val file = if (hasFullText) "book.json" else "book_index.json"
            return parse(context.assets.open(file).bufferedReader().use { it.readText() }, hasFullText)
        }

        fun parse(raw: String, hasFullText: Boolean): BookData {
            val obj = JSONObject(raw)
            val chapters = obj.array("chapters").objects().map { c ->
                Chapter(c.str("id"), c.str("title"), c.str("section"),
                    c.array("blocks").objects().map { b ->
                        BookBlock(b.str("id"), b.str("type"), b.str("text"), b.str("anchor"))
                    }, c.array("topics").strings(), c.array("remedies").strings(),
                    c.optString("previousId").takeIf { it.isNotBlank() && it != "null" },
                    c.optString("nextId").takeIf { it.isNotBlank() && it != "null" })
            }
            val topics = obj.array("topics").objects().map { t ->
                Topic(t.str("id"), t.str("title"), t.array("chapterIds").strings())
            }
            val remedies = obj.array("remedies").objects().map { r ->
                Remedy(r.str("id"), r.str("title"), r.array("chapterIds").strings(), r.array("aliases").strings())
            }
            val treatments = obj.array("treatments").objects().map { t ->
                Treatment(t.str("id"), t.str("condition"), t.str("chapterId"), t.str("category"))
            }
            val stats = obj.optJSONObject("stats") ?: JSONObject()
            return BookData(obj.str("title"), obj.str("author"), chapters, topics, remedies, treatments,
                hasFullText = hasFullText,
                expectedChapters = stats.optInt("chapters", chapters.size),
                expectedTopics = stats.optInt("topics", topics.size),
                expectedRemedies = stats.optInt("remedies", remedies.size))
        }
    }
}

private fun JSONObject.str(key: String) = optString(key, "")
private fun JSONObject.array(key: String) = optJSONArray(key) ?: JSONArray()
private fun JSONArray.objects(): List<JSONObject> =
    (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONArray.strings(): List<String> =
    (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }

package com.xalid.meditsinaproroka.nativeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterSpeechTextTest {
    private fun chapter(vararg content: String) = Chapter(
        id = "chapter-1", slug = "chapter-1", title = "Название главы",
        order = 1, section = "Раздел",
        blocks = content.mapIndexed { index, value ->
            BookBlock(id = "b$index", anchor = "p$index", type = "text", text = value)
        },
        topics = emptyList(), remedies = emptyList(), previousId = null, nextId = null,
    )

    @Test fun readsTitleAndEveryNonEmptyBlockInOrder() {
        val chunks = chapter("Первый абзац.", " ", "Второй абзац!").speechSegments(800)
        assertEquals(listOf("Название главы", "Первый абзац.", "Второй абзац!"), chunks)
    }

    @Test fun longTextIsSplitWithoutLosingAnyWords() {
        val text = (1..250).joinToString(" ") { "слово$it" }
        val chunks = chapter(text).speechSegments(100)
        assertTrue(chunks.size > 2)
        assertTrue(chunks.all { it.length <= 100 })
        assertEquals(text, chunks.drop(1).joinToString(" "))
    }

    @Test fun arabicTextIsNeverSilentlyDiscarded() {
        val source = "قال رسول الله ﷺ — Мир ему."
        val chunks = chapter(source).speechSegments(100)
        assertTrue(chunks.contains(source))
    }

    @Test fun allSegmentsAreBoundedAndNonBlank() {
        val chunks = chapter("A".repeat(252), "", "Текст").speechSegments(70)
        assertTrue(chunks.isNotEmpty())
        assertTrue(chunks.all { it.isNotBlank() && it.length <= 70 })
        assertFalse(chunks.any { it == "" })
    }
}

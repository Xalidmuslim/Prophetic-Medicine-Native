package com.xalid.meditsinaproroka.nativeapp

import org.junit.Assert.*
import org.junit.Test

class BookDataTest {
    private val fixture = """
      {"title":"Fixture","author":"Author","chapters":[
        {"id":"ch-001","title":"О мёде","section":"Средства","blocks":[{"id":"p1","type":"text","text":"Описание.","anchor":"p1"}],"topics":[],"remedies":[],"previousId":null,"nextId":"ch-002"}
      ],"topics":[],"remedies":[],"treatments":[],"stats":{"chapters":111,"topics":15,"remedies":98}}
    """.trimIndent()

    @Test fun parsesOriginalApkSchema() {
        val book = BookData.parse(fixture, true)
        assertEquals(1, book.chapters.size)
        assertEquals("О мёде", book.chapters.first().title)
        assertEquals("Описание.", book.chapters.first().blocks.first().text)
        assertEquals("ch-002", book.chapters.first().nextId)
        assertNull(book.chapters.first().previousId)
        assertTrue(book.hasFullText)
    }

    @Test fun previewIsNeverMisrepresentedAsComplete() {
        val preview = BookData.parse(fixture, false)
        assertFalse(preview.hasFullText)
        assertEquals(111, preview.expectedChapters)
        assertEquals(15, preview.expectedTopics)
        assertEquals(98, preview.expectedRemedies)
    }

    @Test fun fullTextSearchFindsBlockAndTitle() {
        val book = BookData.parse(fixture, true)
        assertEquals(1, book.search("описание").size)
        assertEquals(1, book.search("мёде").size)
        assertTrue(book.search("не найдено").isEmpty())
    }
}

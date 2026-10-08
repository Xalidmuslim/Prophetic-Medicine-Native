package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Imports the owner's book into PRIVATE Android app storage. No book text is committed to
 * the public GitHub repository, fetched from a server, or embedded in public CI artifacts.
 */
object BookRepository {
    private const val FILE_NAME = "book.json"
    private const val MAX_BOOK_BYTES = 8 * 1024 * 1024
    private const val EXPECTED_CHAPTERS = 111
    private const val EXPECTED_TOPICS = 15
    private const val EXPECTED_REMEDIES = 98
    private const val EXPECTED_TREATMENTS = 39

    fun load(context: Context): BookData {
        val imported = File(context.filesDir, FILE_NAME)
        if (imported.isFile) {
            try {
                return parseVerified(imported.readText(Charsets.UTF_8))
            } catch (_: Exception) {
                // Retain a corrupt imported copy for diagnosis; never silently delete user data.
                // Fallback keeps the app usable and allows a new import.
            }
        }
        val packaged = context.assets.list("")?.contains(FILE_NAME) == true
        val asset = if (packaged) FILE_NAME else "book_index.json"
        val raw = context.assets.open(asset).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return if (packaged) parseVerified(raw) else BookData.parse(raw, hasFullText = false)
    }

    fun importBook(context: Context, uri: Uri): BookData {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть выбранный файл")
        val buffer = ByteArrayOutputStream()
        input.use { source ->
            val chunk = ByteArray(8192)
            while (true) {
                val read = source.read(chunk)
                if (read < 0) break
                if (buffer.size() + read > MAX_BOOK_BYTES) {
                    throw IllegalArgumentException("Файл слишком большой")
                }
                buffer.write(chunk, 0, read)
            }
        }
        val bytes = buffer.toByteArray()
        val parsed = parseVerified(bytes.toString(Charsets.UTF_8))
        val target = File(context.filesDir, FILE_NAME)
        val temporary = File(context.filesDir, "$FILE_NAME.tmp")
        try {
            FileOutputStream(temporary).use { out ->
                out.write(bytes)
                out.fd.sync()
            }
            Files.move(
                temporary.toPath(), target.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
            )
        } finally {
            temporary.delete()
        }
        return parsed
    }

    private fun parseVerified(raw: String): BookData {
        val book = BookData.parse(raw, hasFullText = true)
        require(
            book.chapters.size == EXPECTED_CHAPTERS &&
                book.topics.size == EXPECTED_TOPICS &&
                book.remedies.size == EXPECTED_REMEDIES &&
                book.treatments.size == EXPECTED_TREATMENTS
        ) { "Неверная структура книги. Ожидается 111 глав, 15 тем, 98 средств и 39 ситуаций." }
        require(book.chapters.sumOf { it.blocks.size } >= 2500) {
            "Книга неполная: отсутствуют текстовые блоки."
        }
        require(book.chapters.all { it.id.isNotBlank() } &&
            book.chapters.map { it.id }.distinct().size == EXPECTED_CHAPTERS) {
            "Ошибка идентификаторов глав."
        }
        return book
    }
}

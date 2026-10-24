package com.xalid.meditsinaproroka.nativeapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Break a chapter into short, independent utterances. The source book, block IDs,
 * and actual reading paragraphs remain untouched. Each source block is spoken.
 */
internal fun Chapter.speechSegments(maxChars: Int = 700): List<String> {
    require(maxChars >= 20)
    val result = mutableListOf<String>()
    val parts = sequenceOf(title) + blocks.asSequence().map { it.text }
    for (raw in parts) {
        var remaining = raw.trim().replace(Regex("\\s+"), " ")
        while (remaining.isNotEmpty()) {
            if (remaining.length <= maxChars) {
                result.add(remaining)
                break
            }
            val space = remaining.lastIndexOf(' ', startIndex = maxChars)
            val take = if (space > maxChars / 3) space else maxChars
            result.add(remaining.take(take).trim())
            remaining = remaining.drop(take).trimStart()
        }
    }
    return result
}

internal enum class ChapterVoiceStatus { Preparing, Ready, Playing, Paused, Error }

/**
 * Uses the Android speech engine selected by the reader. The engine can be the
 * free offline neural RuVoice app; no paid API, recordings or permissions.
 * Android TTS has no genuine mid-utterance pause; resuming repeats at most
 * one short segment, not the whole chapter.
 */
internal class ChapterSpeechController(context: Context) {
    private val main = Handler(Looper.getMainLooper())
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var closed = false
    private var generation = 0
    private var segments: List<String> = emptyList()

    var status by mutableStateOf(ChapterVoiceStatus.Preparing)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var segmentIndex by mutableIntStateOf(0)
        private set
    var segmentCount by mutableIntStateOf(0)
        private set
    var rate by mutableFloatStateOf(0.95f)
        private set

    init {
        tts = TextToSpeech(appContext) { init ->
            main.post {
                if (closed) return@post
                val engine = tts
                if (init != TextToSpeech.SUCCESS || engine == null) {
                    fail("Голосовой движок Android недоступен. Установите русский голос.")
                    return@post
                }
                val language = engine.setLanguage(Locale.forLanguageTag("ru-RU"))
                if (language == TextToSpeech.LANG_MISSING_DATA ||
                    language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    fail("Русский голос не установлен. Откройте настройки голоса.")
                    return@post
                }
                // Prefer installed, offline male voices when their identity is
                // known. The selected engine still controls the actual sound.
                val voices = runCatching { engine.voices.orEmpty() }.getOrDefault(emptySet())
                    .filter { it.locale.language == "ru" && !it.isNetworkConnectionRequired }
                val maleNames = listOf(
                    "aidar", "eugene", "alexandr", "dmitriy",
                    "marat", "roman", "igor", "eduard",
                )
                val preferred = voices.firstOrNull { voice ->
                    maleNames.any { voice.name.contains(it, ignoreCase = true) }
                } ?: voices.maxByOrNull { it.quality }
                if (preferred != null) {
                    runCatching { engine.voice = preferred }
                }
                engine.setSpeechRate(rate)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) {
                        main.post {
                            if (closed || status != ChapterVoiceStatus.Playing ||
                                utteranceId != utteranceIdForCurrent()) return@post
                            segmentIndex++
                            if (segmentIndex >= segmentCount) {
                                status = ChapterVoiceStatus.Ready
                                segmentIndex = 0
                            } else {
                                speakCurrent()
                            }
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        main.post {
                            if (!closed && status == ChapterVoiceStatus.Playing &&
                                utteranceId == utteranceIdForCurrent()) {
                                fail("Ошибка синтеза речи. Проверьте установленный голос.")
                            }
                        }
                    }
                })
                status = ChapterVoiceStatus.Ready
            }
        }
    }

    fun play(chapter: Chapter) {
        if (status == ChapterVoiceStatus.Preparing || status == ChapterVoiceStatus.Error) return
        if (status == ChapterVoiceStatus.Paused) {
            status = ChapterVoiceStatus.Playing
            speakCurrent()
            return
        }
        stop()
        segments = chapter.speechSegments()
        segmentCount = segments.size
        segmentIndex = 0
        if (segmentCount == 0) {
            fail("В этой главе нет текста для чтения.")
            return
        }
        errorMessage = null
        status = ChapterVoiceStatus.Playing
        speakCurrent()
    }

    fun pause() {
        if (status != ChapterVoiceStatus.Playing) return
        // Prevent late callbacks from a canceled utterance advancing the book.
        generation++
        status = ChapterVoiceStatus.Paused
        tts?.stop()
    }

    fun stop() {
        generation++
        tts?.stop()
        segmentIndex = 0
        if (status != ChapterVoiceStatus.Preparing &&
            status != ChapterVoiceStatus.Error) status = ChapterVoiceStatus.Ready
    }

    fun cycleRate() {
        rate = when {
            rate < 0.99f -> 1.0f
            rate < 1.10f -> 1.15f
            else -> 0.85f
        }
        tts?.setSpeechRate(rate)
    }

    private fun utteranceIdForCurrent() = "chapter:$generation:$segmentIndex"

    private fun speakCurrent() {
        if (closed || status != ChapterVoiceStatus.Playing) return
        val engine = tts ?: run {
            fail("Голосовой движок недоступен.")
            return
        }
        val result = engine.speak(
            segments[segmentIndex], TextToSpeech.QUEUE_FLUSH, null, utteranceIdForCurrent()
        )
        if (result == TextToSpeech.ERROR) fail("Не удалось начать чтение. Выберите другой голос.")
    }

    private fun fail(message: String) {
        tts?.stop()
        errorMessage = message
        status = ChapterVoiceStatus.Error
    }

    fun close() {
        closed = true
        generation++
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

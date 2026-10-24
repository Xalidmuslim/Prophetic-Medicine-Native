package com.xalid.meditsinaproroka.nativeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterSpeechEngineTest {
    @Test fun useRuVoiceAndNeverSilentlyReplaceItWithTheAndroidDefault() {
        assertEquals("ru.kost.ruvoice", RUVOICE_ENGINE_PACKAGE)
        assertTrue(isRuVoiceEngine("ru.kost.ruvoice"))
        assertFalse(isRuVoiceEngine("com.google.android.tts"))
        assertFalse(isRuVoiceEngine(null))
    }

    @Test fun useNaturalUnmodifiedSpeechRate() {
        assertEquals(1.0f, CHAPTER_SPEECH_DEFAULT_RATE, 0.0001f)
    }
}

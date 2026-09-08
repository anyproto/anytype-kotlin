package com.anytypeio.anytype.domain.ai

import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FlagGatedTypeSuggestionEngineTest {

    private val settings: UserSettingsRepository = mock()

    private val delegate = object : TypeSuggestionEngine {
        var prewarmed = false
        override suspend fun isAvailable(): Boolean = true
        override suspend fun prewarm() { prewarmed = true }
        override suspend fun suggest(text: String, typeNames: List<String>): String? =
            typeNames.firstOrNull()
    }

    @Test
    fun `flag off - everything degrades silently`() = runTest {
        whenever(settings.getQuickCaptureAiEnabled()).thenReturn(false)
        val engine = FlagGatedTypeSuggestionEngine(settings, delegate)

        assertFalse(engine.isAvailable())
        engine.prewarm()
        assertFalse(delegate.prewarmed)
        assertNull(engine.suggest("Buy milk", listOf("Task", "Note")))
    }

    @Test
    fun `flag on - delegates`() = runTest {
        whenever(settings.getQuickCaptureAiEnabled()).thenReturn(true)
        val engine = FlagGatedTypeSuggestionEngine(settings, delegate)

        assertTrue(engine.isAvailable())
        engine.prewarm()
        assertTrue(delegate.prewarmed)
        assertEquals("Task", engine.suggest("Buy milk", listOf("Task", "Note")))
    }

    @Test
    fun `settings failure counts as flag off`() = runTest {
        whenever(settings.getQuickCaptureAiEnabled()).thenThrow(RuntimeException("boom"))
        val engine = FlagGatedTypeSuggestionEngine(settings, delegate)

        assertFalse(engine.isAvailable())
        assertNull(engine.suggest("Buy milk", listOf("Task")))
    }

    @Test
    fun `noop engine never suggests`() = runTest {
        assertFalse(TypeSuggestionEngine.NoOp.isAvailable())
        assertNull(TypeSuggestionEngine.NoOp.suggest("Buy milk", listOf("Task")))
        verify(settings, never()).getQuickCaptureAiEnabled()
    }
}

package com.anytypeio.anytype.domain.ai

import com.anytypeio.anytype.domain.config.UserSettingsRepository

/**
 * On-device AI type suggestion for quick capture (spec: docs/quick-capture-android-spec.md §9).
 *
 * Contract: given a short note text and the type names currently shown in the type bar,
 * return exactly one name **from the list** — or null. Null is the only failure mode:
 * the caller degrades silently to the non-AI ordering and never surfaces an error.
 */
interface TypeSuggestionEngine {

    suspend fun isAvailable(): Boolean

    /** Loads the model into memory ahead of the first suggestion; no-op when unavailable. */
    suspend fun prewarm()

    /** Returns a name from [typeNames], or null → caller degrades silently. */
    suspend fun suggest(text: String, typeNames: List<String>): String?

    object NoOp : TypeSuggestionEngine {
        override suspend fun isAvailable(): Boolean = false
        override suspend fun prewarm() {}
        override suspend fun suggest(text: String, typeNames: List<String>): String? = null
    }
}

/**
 * Gates a real engine behind the quickCaptureTypeSuggestions feature flag, so the
 * presentation layer never has to know about settings.
 */
class FlagGatedTypeSuggestionEngine(
    private val settings: UserSettingsRepository,
    private val delegate: TypeSuggestionEngine
) : TypeSuggestionEngine {

    private suspend fun isFlagEnabled(): Boolean =
        runCatching { settings.getQuickCaptureAiEnabled() }.getOrDefault(false)

    override suspend fun isAvailable(): Boolean =
        isFlagEnabled() && delegate.isAvailable()

    override suspend fun prewarm() {
        if (isFlagEnabled()) delegate.prewarm()
    }

    override suspend fun suggest(text: String, typeNames: List<String>): String? =
        if (isFlagEnabled()) delegate.suggest(text, typeNames) else null
}

package com.anytypeio.anytype.core_ai

import com.anytypeio.anytype.domain.ai.TypeSuggestionEngine
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * On-device type suggestion via the ML Kit GenAI Prompt API (Gemini Nano in the AICore
 * system service). Spec: docs/quick-capture-android-spec.md §9.
 *
 * The Structured Output API cannot express a runtime enum (compile-time schemas only),
 * so the "exactly one type name from the list" contract is enforced by prompt-side
 * enumeration + strict app-side validation of the returned string — the Android analog
 * of iOS's constrained decoding.
 *
 * Every failure degrades to null (= no suggestion): unsupported device, model still
 * downloading, safety-filter refusal on benign text, quota, timeout. The caller must
 * never surface an AI error, and capture must never block on AI. Each silent fallback
 * is logged at debug level — otherwise "why no suggestion?" is undebuggable on device.
 */
class MlKitTypeSuggestionEngine : TypeSuggestionEngine {

    // Lazy on purpose: this is the flag-off cold-path guarantee. The engine singleton is
    // constructed on every editor open, but AICore must only be contacted once the
    // quickCaptureTypeSuggestions flag lets a call through (see FlagGatedTypeSuggestionEngine).
    private val model by lazy { Generation.getClient() }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        runCatching { model.checkStatus() == FeatureStatus.AVAILABLE }
            .onFailure { Timber.d(it, "QC suggestions: availability check failed") }
            .getOrDefault(false)
    }

    override suspend fun prewarm() = withContext(Dispatchers.IO) {
        runCatching {
            if (model.checkStatus() == FeatureStatus.AVAILABLE) {
                model.warmup()
            }
        }.onFailure { Timber.d(it, "QC suggestions: warmup failed") }
        Unit
    }

    override suspend fun suggest(text: String, typeNames: List<String>): String? {
        if (typeNames.isEmpty()) return null
        val input = text.trim().take(MAX_INPUT_LENGTH)
        if (input.length < MIN_INPUT_LENGTH) return null
        // Off the main thread: the caller runs on viewModelScope (Main.immediate) while the
        // user is typing, and withTimeoutOrNull only bounds a cooperatively suspending body —
        // it cannot interrupt a blocking call inside a third-party beta SDK. Confining here
        // makes "capture never blocks on AI" structural rather than an assumption about
        // ML Kit's threading.
        return withContext(Dispatchers.IO) {
            withTimeoutOrNull(SUGGESTION_TIMEOUT_MS) {
                runCatching {
                    if (model.checkStatus() != FeatureStatus.AVAILABLE) {
                        Timber.d("QC suggestions: model not available")
                        return@runCatching null
                    }
                    val response = model.generateContent(
                        generateContentRequest(
                            TextPart(buildPrompt(input, typeNames))
                        ) {
                            temperature = 0.0f
                            topK = 1
                            candidateCount = 1
                            maxOutputTokens = MAX_OUTPUT_TOKENS
                        }
                    )
                    val raw = response.candidates.firstOrNull()?.text?.trim()
                    // Strict validation replaces constrained decoding: out-of-list → discarded.
                    val match = typeNames.firstOrNull { it.equals(raw, ignoreCase = true) }
                    if (match == null) {
                        Timber.d("QC suggestions: no valid match (raw = %s)", raw)
                    }
                    match
                }.onFailure {
                    // Cancellation reaches here too (runCatching catches Throwable), so a
                    // superseded classification returns null rather than propagating — the
                    // caller must therefore not treat null as "this text was classified".
                    Timber.d(it, "QC suggestions: generation failed")
                }.getOrNull()
            }.also {
                if (it == null) Timber.d("QC suggestions: silent fallback (no suggestion)")
            }
        }
    }

    private fun buildPrompt(input: String, typeNames: List<String>): String = buildString {
        appendLine("Classify the note into exactly one category.")
        appendLine("Categories: ${typeNames.joinToString(" | ")}")
        appendLine("Note: \"$input\"")
        append("Reply with only the category name, nothing else.")
    }

    companion object {
        const val MIN_INPUT_LENGTH = 3
        const val MAX_INPUT_LENGTH = 500
        const val MAX_OUTPUT_TOKENS = 16
        const val SUGGESTION_TIMEOUT_MS = 2_500L
    }
}

package com.anytypeio.anytype.core_ui.widgets.dv.scroll

import kotlin.math.roundToInt

/** The origin is explicit: Compose SideEffect also includes ordinary native fling frames. */
enum class MotionOrigin {
    User, Inertia, Accessibility, ProgrammaticReveal, Restoration, CardDrag, DragAutoScroll, Editing
}

/**
 * Main-thread UI state shared by the View parent and direct Compose column connections.
 * Positive physical-pixel travel collapses the header. Content offsets never enter this state.
 * Animation and native scroll cancellation belong to the adapters, not this reducer.
 */
class DataviewScrollCoordinator {
    data class Session internal constructor(
        val owner: String,
        val generation: Long,
        val origin: MotionOrigin,
        internal val integerPixels: Boolean
    )

    var range: Float = 0f
        private set
    var offset: Float = 0f
        private set
    val progress: Float get() = if (range > 0f) offset / range else 0f
    val savedProgress: Float get() = pendingProgress ?: progress
    var generation: Long = 0
        private set
    /** Restorations survive geometry invalidation, but never supersede newer user intent. */
    var inputGeneration: Long = 0
        private set
    var touchGeneration: Long? = null
        private set
    val isBlocked: Boolean get() = blocked.isNotEmpty()
    val isIdle: Boolean get() = session == null

    private val blocked = mutableSetOf<MotionOrigin>()
    private val listeners = mutableSetOf<() -> Unit>()
    private var session: Session? = null
    private var cancelOwner: (() -> Unit)? = null
    private var pendingProgress: Float? = null

    /** One epoch for all pointers in a board gesture, even after a column claims ownership. */
    fun beginTouch() {
        notifyUserInput()
        touchGeneration = generation
    }

    fun notifyUserInput() {
        acceptInput()
        cancel()
    }

    fun endTouch() { touchGeneration = null }

    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun changed() { listeners.toList().forEach { it() } }

    fun begin(
        owner: String,
        origin: MotionOrigin = MotionOrigin.User,
        integerPixels: Boolean = false,
        cancel: () -> Unit = {}
    ): Session {
        this.cancel()
        if (origin == MotionOrigin.User || origin == MotionOrigin.Accessibility) acceptInput()
        // Normalize once at the native session boundary. Never round each Compose delta.
        if (integerPixels) normalizePixels()
        return Session(owner, generation, origin, integerPixels).also {
            session = it
            cancelOwner = cancel
        }
    }

    fun isCurrent(candidate: Session): Boolean = session == candidate && generation == candidate.generation

    fun cancel() {
        val previous = cancelOwner
        session = null
        cancelOwner = null
        generation++
        previous?.invoke()
    }

    fun cancel(candidate: Session) { if (isCurrent(candidate)) cancel() }

    fun end(candidate: Session) {
        if (isCurrent(candidate)) {
            session = null
            cancelOwner = null
        }
    }

    fun setBlocked(origin: MotionOrigin, value: Boolean) {
        val didChange = if (value) blocked.add(origin) else blocked.remove(origin)
        if (didChange) {
            if (value) cancel()
            changed()
        }
    }

    /** Geometry changes are not consumed input. Preserve endpoints and idle partial progress. */
    fun setRange(value: Float, preserveProgress: Boolean = isIdle) {
        val newRange = value.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
        if (newRange == range) return
        val oldRange = range
        val oldOffset = offset
        val requested = pendingProgress
        if (newRange == 0f && oldRange > 0f) pendingProgress = oldOffset / oldRange
        range = newRange
        offset = when {
            requested != null && newRange > 0f -> {
                pendingProgress = null
                requested * newRange
            }
            oldRange > 0f && oldOffset == oldRange -> newRange
            oldOffset == 0f -> 0f
            !preserveProgress -> oldOffset.coerceIn(0f, newRange)
            oldRange > 0f -> oldOffset / oldRange * newRange
            else -> 0f
        }
        if (session?.integerPixels == true) offset = pixelOffset()
        changed()
    }

    fun restoreProgress(value: Float) {
        cancel()
        val bounded = value.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
        pendingProgress = if (range == 0f) bounded else null
        offset = bounded * range
        changed()
    }

    /** Explicit accessibility/edit commands are immediate, including with animations disabled. */
    fun setExpanded(expanded: Boolean) {
        acceptInput()
        restoreProgress(if (expanded) 0f else 1f)
    }

    private fun acceptInput() {
        inputGeneration++
        pendingProgress = null
    }

    fun canConsume(delta: Float, candidate: Session): Boolean =
        eligible(candidate) && delta.isFinite() &&
            ((delta > 0f && offset < range) || (delta < 0f && offset > 0f))

    fun consumePreScroll(delta: Float, candidate: Session): Float =
        if (delta > 0f) consume(delta, candidate) else 0f

    fun consumePostScroll(delta: Float, candidate: Session): Float =
        if (delta < 0f) consume(delta, candidate) else 0f

    private fun eligible(candidate: Session): Boolean = !isBlocked && isCurrent(candidate) &&
        (candidate.origin == MotionOrigin.User || candidate.origin == MotionOrigin.Inertia ||
            candidate.origin == MotionOrigin.Accessibility)

    private fun consume(delta: Float, candidate: Session): Float {
        if (!eligible(candidate) || !delta.isFinite()) return 0f
        val previous = offset
        offset = (offset + delta).coerceIn(0f, range)
        val consumed = offset - previous
        if (consumed != 0f) changed()
        return consumed
    }

    private fun pixelOffset(): Float = offset.roundToInt().toFloat().coerceIn(0f, range)
    private fun normalizePixels() {
        val rounded = pixelOffset()
        if (offset != rounded) {
            offset = rounded
            changed()
        }
    }
}

package com.anytypeio.anytype.features.sets.dv

import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.InputDevice
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.atomic.AtomicReference

/** Held stretch deliberately never becomes idle. Drive its pointer stream off the UI thread. */
internal fun performDataviewNativeAction(view: View, action: ViewAction) {
    checkDataviewMain { check(action.constraints.matches(view)) }
    action.perform(DataviewNativeInput, view)
}

internal fun <T> checkDataviewMain(block: () -> T): T {
    val result = AtomicReference<Result<T>>()
    InstrumentationRegistry.getInstrumentation().runOnMainSync { result.set(runCatching(block)) }
    return result.get().getOrThrow()
}

internal object DataviewNativeInput : UiController {
    override fun injectMotionEvent(event: MotionEvent): Boolean {
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        InstrumentationRegistry.getInstrumentation().sendPointerSync(event)
        return true
    }
    override fun injectKeyEvent(event: KeyEvent): Boolean {
        InstrumentationRegistry.getInstrumentation().sendKeySync(event)
        return true
    }
    override fun injectString(str: String): Boolean = error("This fixture only injects native pointers")
    override fun loopMainThreadForAtLeast(millisDelay: Long) { SystemClock.sleep(millisDelay) }
    override fun loopMainThreadUntilIdle() { InstrumentationRegistry.getInstrumentation().waitForIdleSync() }
}

/** Wait for the actual system IME window, whose input surface can outlive hidden root insets. */
internal fun awaitDataviewImeWindow(visible: Boolean) {
    val automation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
    automation.serviceInfo = automation.serviceInfo.apply {
        flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    }
    val deadline = android.os.SystemClock.uptimeMillis() + 5_000
    var previous: List<android.graphics.Rect>? = null
    var stableSince = android.os.SystemClock.uptimeMillis()
    var current = emptyList<android.graphics.Rect>()
    while (android.os.SystemClock.uptimeMillis() < deadline) {
        current = automation.windows.filter {
            it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }.map { window -> android.graphics.Rect().also(window::getBoundsInScreen) }
        val matches = current.isNotEmpty() == visible
        if (!matches || current != previous) stableSince = android.os.SystemClock.uptimeMillis()
        if (matches && android.os.SystemClock.uptimeMillis() - stableSince >= 250) return
        previous = current
        android.os.SystemClock.sleep(32)
    }
    throw AssertionError("Expected settled native IME window visible=$visible; actual=$current all=" + automation.windows.map { "type=${it.type} title=${it.title} bounds=${android.graphics.Rect().also(it::getBoundsInScreen)}" })
}

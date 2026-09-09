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

package com.anytypeio.anytype.core_utils.ui

import android.os.SystemClock
import android.view.View

/**
 * @property [interval] click window interval in millis
 */
abstract class OnThrottleClickListener @JvmOverloads constructor(
    private val interval: Long = DEFAULT_INTERVAL
) : View.OnClickListener {

    private var lastClickTime: Long = 0

    abstract fun onThrottleClick(v: View?)

    override fun onClick(v: View?) {
        val currentClickTime: Long = SystemClock.elapsedRealtime()
        val elapsedTime = currentClickTime - lastClickTime
        if (elapsedTime <= interval) return
        lastClickTime = currentClickTime
        onThrottleClick(v)
    }

    companion object {
        const val DEFAULT_INTERVAL = 700L
    }
}

fun View.setOnThrottleClickListener(
    millis: Long = OnThrottleClickListener.DEFAULT_INTERVAL,
    action: (v: View?) -> Unit
) {
    setOnClickListener(
        object : OnThrottleClickListener(millis) {
            override fun onThrottleClick(v: View?) {
                action(v)
            }
        }
    )
}
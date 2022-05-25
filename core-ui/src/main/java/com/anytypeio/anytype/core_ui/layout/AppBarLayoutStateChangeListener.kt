package com.anytypeio.anytype.core_ui.layout

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

enum class State { EXPANDED, COLLAPSED, IDLE }
abstract class AppBarLayoutStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    private var state = State.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        state = when (verticalOffset == 0) {
            true -> {
                if (state != State.EXPANDED) {
                    onStateChanged(State.EXPANDED)
                }
                State.EXPANDED
            }
            false -> {
                when (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    true -> {
                        if (state != State.COLLAPSED) {
                            onStateChanged(State.COLLAPSED)
                        }
                        State.COLLAPSED
                    }
                    false -> {
                        if (state != State.IDLE) {
                            onStateChanged(State.IDLE)
                        }
                        State.IDLE
                    }
                }
            }
        }
    }

    abstract fun onStateChanged(state: State)
}
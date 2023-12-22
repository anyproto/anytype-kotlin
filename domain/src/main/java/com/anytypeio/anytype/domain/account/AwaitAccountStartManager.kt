package com.anytypeio.anytype.domain.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AwaitAccountStartManager {
    fun isStarted(): Flow<Boolean>
    fun setIsStarted(isStarted: Boolean)
    object Default: AwaitAccountStartManager {
        private val isStarted = MutableStateFlow(false)
        override fun isStarted(): Flow<Boolean> = isStarted
        override fun setIsStarted(isStarted: Boolean) {
            this.isStarted.value = isStarted
        }
    }
}
package com.anytypeio.anytype.presentation.common

sealed class ScreenState {
    object Idle: ScreenState()
    object Loading: ScreenState()
    object Success: ScreenState()
    data class Failure(val msg: String): ScreenState()
}
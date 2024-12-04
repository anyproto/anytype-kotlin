package com.anytypeio.anytype.presentation.editor.model

sealed class EditorDatePickerState {
    data object Hidden : EditorDatePickerState()
    data object Visible : EditorDatePickerState()
}

sealed class OnEditorDatePickerEvent {
    object OnDatePickerDismiss : OnEditorDatePickerEvent()
    data class OnDateSelected(val timeInMillis: Long?) : OnEditorDatePickerEvent()
    object OnTodayClick : OnEditorDatePickerEvent()
    object OnTomorrowClick : OnEditorDatePickerEvent()
}
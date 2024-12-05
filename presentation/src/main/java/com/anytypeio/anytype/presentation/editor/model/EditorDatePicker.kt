package com.anytypeio.anytype.presentation.editor.model

import com.anytypeio.anytype.core_models.Id

sealed class EditorDatePickerState {
    data object Hidden : EditorDatePickerState()
    sealed class Visible(val targetId: Id) : EditorDatePickerState() {
        class Mention(targetId: Id) : Visible(targetId)
        class Link(targetId: Id) : Visible(targetId)
    }
}

sealed class OnEditorDatePickerEvent {
    sealed class OnDateSelected : OnEditorDatePickerEvent() {
        data class Mention(val timeInMillis: Long?, val targetId: Id) : OnDateSelected()
        data class Link(val timeInMillis: Long?, val targetId: Id) : OnDateSelected()
    }

    object OnDatePickerDismiss : OnEditorDatePickerEvent()

    sealed class OnTodayClick : OnEditorDatePickerEvent() {
        data class Mention(val targetId: Id) : OnTodayClick()
        data class Link(val targetId: Id) : OnTodayClick()
    }

    sealed class OnTomorrowClick : OnEditorDatePickerEvent() {
        data class Mention(val targetId: Id) : OnTomorrowClick()
        data class Link(val targetId: Id) : OnTomorrowClick()
    }
}
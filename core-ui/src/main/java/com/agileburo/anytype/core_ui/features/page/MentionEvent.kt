package com.agileburo.anytype.core_ui.features.page

sealed class MentionEvent {
    data class MentionSuggestText(val text: CharSequence) : MentionEvent()
    object MentionSuggestStart : MentionEvent()
    object MentionSuggestStop : MentionEvent()
}
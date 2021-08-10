package com.anytypeio.anytype.presentation.editor.editor.mention

sealed class MentionEvent {
    data class MentionSuggestText(val text: CharSequence) : MentionEvent()
    data class MentionSuggestStart(val cursorCoordinate : Int, val mentionStart: Int) : MentionEvent()
    object MentionSuggestStop : MentionEvent()
}
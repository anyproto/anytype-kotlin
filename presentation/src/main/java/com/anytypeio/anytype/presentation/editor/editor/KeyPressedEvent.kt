package com.anytypeio.anytype.presentation.editor.editor

import android.text.Editable
import com.anytypeio.anytype.core_models.Id

sealed class KeyPressedEvent {

    data class OnTitleBlockEnterKeyEvent(
        val target: Id,
        val text: String,
        val range: IntRange
    ) : KeyPressedEvent()

    data class OnDescriptionBlockEnterKeyEvent(
        val target: Id,
        val text: String,
        val range: IntRange
    ) : KeyPressedEvent()

}
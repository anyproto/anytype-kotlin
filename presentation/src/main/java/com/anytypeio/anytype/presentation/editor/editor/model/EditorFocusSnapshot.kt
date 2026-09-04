package com.anytypeio.anytype.presentation.editor.editor.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.Id
import kotlinx.parcelize.Parcelize

/**
 * Where the caret was when the editor lost its views.
 *
 * A rotation recreates the activity, and process death rebuilds the ViewModel as well. The
 * fragment saves this in its instance state and hands it back before the document opens again,
 * so the focused block and the caret come back where the user left them.
 *
 * @property blockId the block that had focus
 * @property selectionStart start of the selection, or null when no selection was reported
 * @property selectionEnd end of the selection, or null when no selection was reported
 */
@Parcelize
data class EditorFocusSnapshot(
    val blockId: Id,
    val selectionStart: Int?,
    val selectionEnd: Int?
) : Parcelable

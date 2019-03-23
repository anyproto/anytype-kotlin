package com.agileburo.anytype.feature_editor.ui

import com.agileburo.anytype.feature_editor.domain.Block

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 21.03.2019.
 */
sealed class EditorAction {
    object PressButton : EditorAction()
    data class PressBlock(val id: String) : EditorAction()
}

sealed class EditorState {
    data class Result(val blocks: List<Block>): EditorState()
    object Loading : EditorState()
}
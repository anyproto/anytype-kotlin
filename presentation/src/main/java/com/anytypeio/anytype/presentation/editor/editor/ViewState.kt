package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

sealed class ViewState {
    object Loading : ViewState()
    object NotExist : ViewState()
    data class Success(val blocks: List<BlockView>) : ViewState()
}
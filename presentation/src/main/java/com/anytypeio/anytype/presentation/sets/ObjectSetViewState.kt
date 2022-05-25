package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.sets.model.Viewer

data class ObjectSetViewState(
    val title: BlockView.Title.Basic?,
    val viewer: Viewer
)
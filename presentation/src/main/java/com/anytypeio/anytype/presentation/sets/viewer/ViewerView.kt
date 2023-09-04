package com.anytypeio.anytype.presentation.sets.viewer

import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id

data class ViewerView(
    val id: Id,
    val name: String,
    val type: DVViewerType,
    val isActive: Boolean,
    val showActionMenu: Boolean = false,
    val isUnsupported: Boolean = false,
    val defaultObjectType: Id?,
    val relations: List<String>,
    val sorts: List<String>,
    val filters: List<String>,
    val isDefaultObjectTypeEnabled: Boolean
)



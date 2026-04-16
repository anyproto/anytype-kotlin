package com.anytypeio.anytype.feature_create_object.presentation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

sealed class CreateObjectNavigation {
    data class OpenEditor(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenSet(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenChat(val id: Id, val space: SpaceId) : CreateObjectNavigation()
}

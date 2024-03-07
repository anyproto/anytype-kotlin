package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType

typealias SpaceType = Int

const val UNKNOWN_SPACE_TYPE = -1
const val PERSONAL_SPACE_TYPE = 0
const val PRIVATE_SPACE_TYPE = 1
const val SHARED_SPACE_TYPE = 2

fun SpaceAccessType.asSpaceType(): SpaceType = when (this) {
    SpaceAccessType.PRIVATE -> PRIVATE_SPACE_TYPE
    SpaceAccessType.PERSONAL -> PERSONAL_SPACE_TYPE
    SpaceAccessType.SHARED -> SHARED_SPACE_TYPE
}
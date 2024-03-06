package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id

interface SpaceInviteResolver {
    fun parseContentId(link: String) : Id?
    fun parseFileKey(link: String) : Id?
}
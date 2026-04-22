package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

/**
 * Derives the ID of the per-user personal-widgets virtual object for [space].
 *
 * Middleware (anytype-heart GO-6962, shipped in v0.50.0-rc02) exposes a per-user,
 * per-space block-tree document stored in the user's Tech Space. Its ID is
 * `_personalWidgets_<encodedSpaceId>`, where the first `.` in the SpaceId is
 * replaced with `_` (the same encoding used for participant IDs).
 */
fun personalWidgetsId(space: SpaceId): Id =
    PREFIX + space.id.replaceFirst('.', '_')

private const val PREFIX = "_personalWidgets_"

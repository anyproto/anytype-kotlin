package com.anytypeio.anytype.core_utils.diff

/**
 * Provides identifier (id, key, etc.) for [DefaultDiffUtil.areItemsTheSame] method.
 * Based on [identifier], two objects are considered to be the same or different.
 * @see [DefaultDiffUtil]
 */
interface DefaultObjectDiffIdentifier {
    val identifier: String
}
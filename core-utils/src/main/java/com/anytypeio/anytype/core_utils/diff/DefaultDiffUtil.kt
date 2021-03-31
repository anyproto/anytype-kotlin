package com.anytypeio.anytype.core_utils.diff

import androidx.recyclerview.widget.DiffUtil

/**
 * Type [T] should be a data class type. Otherwise, [areContentsTheSame] won't work as expected.
 * [T] should also implement [DefaultObjectDiffIdentifier]. Otherwise, [areItemsTheSame] won't work.
 * @see [DefaultObjectDiffIdentifier]
 */
class DefaultDiffUtil<in T : DefaultObjectDiffIdentifier>(
    private val old: List<T>,
    private val new: List<T>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition].identifier == old[oldItemPosition].identifier

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition] == old[oldItemPosition]
}
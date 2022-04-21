package com.anytypeio.anytype.ui.dashboard

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import timber.log.Timber

class DesktopDiffUtil(
    private val old: List<DashboardView>,
    private val new: List<DashboardView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        new[newItemPosition].id == old[oldItemPosition].id

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        new[newItemPosition] == old[oldItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

        val oldDoc = old[oldItemPosition]
        val newDoc = new[newItemPosition]

        if (newDoc::class != oldDoc::class)
            return super.getChangePayload(oldItemPosition, newItemPosition)

        val changes = mutableListOf<Int>()

        if (oldDoc is DashboardView.Document && newDoc is DashboardView.Document) {
            if (oldDoc.target != newDoc.target) {
                changes.add(TARGET_CHANGED)
            }
            if (oldDoc.title != newDoc.title) {
                changes.add(TITLE_CHANGED)
            }
            if (oldDoc.emoji != newDoc.emoji) {
                changes.add(EMOJI_CHANGED)
            }
            if (oldDoc.image != newDoc.image) {
                changes.add(IMAGE_CHANGED)
            }
            if (!newDoc.hasIcon && oldDoc.title != newDoc.title) {
                changes.add(AVATAR_LETTER_CHANGED)
            }
            if (oldDoc.isLoading != newDoc.isLoading) {
                changes.add(LOADING_STATE_CHANGED)
            }
            if (oldDoc.done != newDoc.done) {
                changes.add(DONE_CHANGED)
            }
        }

        if (oldDoc is DashboardView.Archive && newDoc is DashboardView.Archive) {
            if (oldDoc.target != newDoc.target) {
                changes.add(TARGET_CHANGED)
            }
            if (oldDoc.title != newDoc.title) {
                changes.add(TITLE_CHANGED)
            }
        }

        if (oldDoc.isSelected != newDoc.isSelected) {
            changes.add(SELECTION_CHANGED)
        }

        return if (changes.isNotEmpty()) {
            Payload(changes).also { Timber.d("Returning payload: $it") }
        } else {
            super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }

    data class Payload(
        val changes: List<Int>
    ) {

        val isLoadingChanged: Boolean = changes.contains(LOADING_STATE_CHANGED)
        val isSelectionChanged: Boolean = changes.contains(SELECTION_CHANGED)
        val isDoneChanged: Boolean = changes.contains(DONE_CHANGED)

        fun targetChanged() = changes.contains(TARGET_CHANGED)
        fun titleChanged() = changes.contains(TITLE_CHANGED)
        fun emojiChanged() = changes.contains(EMOJI_CHANGED)
        fun imageChanged() = changes.contains(IMAGE_CHANGED)
        fun avatarLetterChanged() = changes.contains(AVATAR_LETTER_CHANGED)
    }

    companion object {
        const val TARGET_CHANGED = 1
        const val TITLE_CHANGED = 3
        const val EMOJI_CHANGED = 4
        const val IMAGE_CHANGED = 5
        const val LOADING_STATE_CHANGED = 6
        const val SELECTION_CHANGED = 7
        const val DONE_CHANGED = 8
        const val AVATAR_LETTER_CHANGED = 9
    }
}
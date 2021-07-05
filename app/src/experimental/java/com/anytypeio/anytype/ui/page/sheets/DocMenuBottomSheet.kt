package com.anytypeio.anytype.ui.page.sheets

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.extensions.*
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.domain.status.SyncStatus
import kotlinx.android.synthetic.experimental.fragment_doc_menu_bottom_sheet.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DocMenuBottomSheet : BaseBottomSheetFragment() {

    private val title get() = arg<String?>(TITLE_KEY)
    private val status get() = SyncStatus.valueOf(arg(STATUS_KEY))
    private val image get() = arg<String?>(IMAGE_KEY)
    private val emoji get() = arg<String?>(EMOJI_KEY)
    private val isProfile get() = arg<Boolean>(IS_PROFILE_KEY)
    private val isArchived get() = arg<Boolean>(IS_ARCHIVED)
    private val isDeleteAllowed get() = arg<Boolean>(IS_DELETE_ALLOWED)
    private val isLayoutAllowed get() = arg<Boolean>(IS_LAYOUT_ALLOWED)
    private val isAddCoverAllowed get() = arg<Boolean>(IS_COVER_ALLOWED)
    private val isRelationsAllowed get() = arg<Boolean>(IS_RELATIONS_ALLOWED)
    private val isDownloadAllowed get() = arg<Boolean>(IS_DOWNLOAD_ALLOWED)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_doc_menu_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindTitle()
        bindSyncStatus(status)
        closeButton.clicks().onEach { dismiss() }.launchIn(lifecycleScope)

        searchOnPageContainer
            .clicks()
            .onEach {
                withParent<DocumentMenuActionReceiver> { onSearchOnPageClicked() }.also { dismiss() }
            }
            .launchIn(lifecycleScope)

        if (isDeleteAllowed) {
            tvArchive.alpha = 1.0F
            ivArchive.alpha = 1.0F
            archiveContainer
                .clicks()
                .onEach {
                    withParent<DocumentMenuActionReceiver> {
                        if (isArchived) onRestoreFromArchiveClicked() else onArchiveClicked()
                    }.also { dismiss() }
                }
                .launchIn(lifecycleScope)
        } else {
            tvArchive.alpha = 0.4F
            ivArchive.alpha = 0.4F
        }

        if (isArchived)
            tvArchive.setText(R.string.restore_from_archive)
        else
            tvArchive.setText(R.string.archive)

        if (isAddCoverAllowed) {
            tvSetCover.alpha = 1.0F
            ivSetCover.alpha = 1.0F
            addCoverContainer
                .clicks()
                .onEach {
                    withParent<DocumentMenuActionReceiver> { onAddCoverClicked() }.also { dismiss() }
                }
                .launchIn(lifecycleScope)
        } else {
            tvSetCover.alpha = 0.4F
            ivSetCover.alpha = 0.4F
        }

        if (isLayoutAllowed) {
            tvSetLayout.alpha = 1.0F
            ivSetLayout.alpha = 1.0F
            setLayoutContainer
                .clicks()
                .onEach {
                    withParent<DocumentMenuActionReceiver> { onLayoutClicked() }.also { dismiss() }
                }
                .launchIn(lifecycleScope)
        } else {
            tvSetLayout.alpha = 0.4F
            ivSetLayout.alpha = 0.4F
        }

        if (isRelationsAllowed) {
            tvRelations.alpha = 1.0F
            relationContainer
                .clicks()
                .onEach {
                    withParent<DocumentMenuActionReceiver> { onDocRelationsClicked() }.also { dismiss() }
                }
                .launchIn(lifecycleScope)
        } else {
            tvRelations.alpha = 0.4F
        }

        if (isDownloadAllowed) {
            downloadContainer.visible()
            downloadContainer
                .clicks()
                .onEach {
                    withParent<DocumentMenuActionReceiver> { onDownloadClicked() }.also { dismiss() }
                }
                .launchIn(lifecycleScope)
        }

        if (image != null && !isProfile) icon.setImageOrNull(image)
        if (emoji != null && !isProfile) icon.setEmojiOrNull(emoji)

        if (isProfile) {
            avatar.visible()
            image?.let { avatar.icon(it) } ?: avatar.bind(
                name = title.orEmpty(),
                color = title.orEmpty().firstDigitByHash().let {
                    requireContext().avatarColor(it)
                }
            )
            addCoverContainer.setBackgroundResource(R.drawable.rectangle_doc_menu_bottom)
            searchOnPageContainer.setBackgroundResource(R.drawable.rectangle_doc_menu_top)
        }
    }

    private fun bindTitle() {
        tvTitle.text = title ?: getString(R.string.untitled)
    }

    private fun bindSyncStatus(status: SyncStatus) {
        when (status) {
            SyncStatus.UNKNOWN -> {
                badge.tint(
                    color = requireContext().color(R.color.sync_status_red)
                )
                tvSubtitle.setText(R.string.sync_status_unknown)
            }
            SyncStatus.FAILED -> {
                badge.tint(
                    color = requireContext().color(R.color.sync_status_red)
                )
                tvSubtitle.setText(R.string.sync_status_failed)
            }
            SyncStatus.OFFLINE -> {
                badge.tint(
                    color = requireContext().color(R.color.sync_status_red)
                )
                tvSubtitle.setText(R.string.sync_status_offline)
            }
            SyncStatus.SYNCING -> {
                badge.tint(
                    color = requireContext().color(R.color.sync_status_orange)
                )
                tvSubtitle.setText(R.string.sync_status_syncing)
            }
            SyncStatus.SYNCED -> {
                badge.tint(
                    color = requireContext().color(R.color.sync_status_green)
                )
                tvSubtitle.setText(R.string.sync_status_synced)
            }
            else -> badge.tint(Color.WHITE)
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(
            title: String?,
            status: SyncStatus,
            image: Url?,
            emoji: String?,
            isProfile: Boolean = false,
            isArchived: Boolean,
            isDeleteAllowed: Boolean,
            isLayoutAllowed: Boolean,
            isAddCoverAllowed: Boolean,
            isRelationsAllowed: Boolean,
            isDownloadAllowed: Boolean
        ) = DocMenuBottomSheet().apply {
            arguments = bundleOf(
                TITLE_KEY to title,
                STATUS_KEY to status.name,
                IMAGE_KEY to image,
                EMOJI_KEY to emoji,
                IS_ARCHIVED to isArchived,
                IS_PROFILE_KEY to isProfile,
                IS_DELETE_ALLOWED to isDeleteAllowed,
                IS_LAYOUT_ALLOWED to isLayoutAllowed,
                IS_COVER_ALLOWED to isAddCoverAllowed,
                IS_RELATIONS_ALLOWED to isRelationsAllowed,
                IS_DOWNLOAD_ALLOWED to isDownloadAllowed
            )
        }

        private const val TITLE_KEY = "arg.doc-menu-bottom-sheet.title"
        private const val IMAGE_KEY = "arg.doc-menu-bottom-sheet.image"
        private const val EMOJI_KEY = "arg.doc-menu-bottom-sheet.emoji"
        private const val STATUS_KEY = "arg.doc-menu-bottom-sheet.status"
        private const val IS_ARCHIVED = "arg.doc-menu-bottom-sheet.is-archived"
        private const val IS_PROFILE_KEY = "arg.doc-menu-bottom-sheet.is-profile"
        private const val IS_DELETE_ALLOWED = "arg.doc-menu-bottom-sheet.is-delete-allowed"
        private const val IS_LAYOUT_ALLOWED = "arg.doc-menu-bottom-sheet.is-layout-allowed"
        private const val IS_COVER_ALLOWED = "arg.doc-menu-bottom-sheet.is-cover-allowed"
        private const val IS_RELATIONS_ALLOWED = "arg.doc-menu-bottom-sheet.is-relations-allowed"
        private const val IS_DOWNLOAD_ALLOWED = "arg.doc-menu-bottom-sheet.is-download-allowed"
    }

    interface DocumentMenuActionReceiver {
        fun onArchiveClicked()
        fun onRestoreFromArchiveClicked()
        fun onSearchOnPageClicked()
        fun onDocRelationsClicked()
        fun onAddCoverClicked()
        fun onLayoutClicked()
        fun onDownloadClicked()
    }
}
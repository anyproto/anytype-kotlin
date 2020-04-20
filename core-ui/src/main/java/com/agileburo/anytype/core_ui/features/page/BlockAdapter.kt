package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BULLET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CONTACT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_DIVIDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_UPLOAD
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FOOTER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_ONE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_THREE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_TWO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HIGHLIGHT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_NUMBERED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PAGE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PARAGRAPH
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_UPLOAD
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_UPLOAD
import com.agileburo.anytype.core_utils.ext.typeOf
import timber.log.Timber

/**
 * Adapter for rendering list of blocks.
 * @property blocks mutable list of blocks
 * @see BlockView
 * @see BlockViewHolder
 * @see BlockViewDiffUtil
 */
class BlockAdapter(
    private var blocks: List<BlockView>,
    private val onParagraphTextChanged: (String, Editable) -> Unit,
    private val onTextChanged: (String, Editable) -> Unit,
    private val onTitleTextChanged: (Editable) -> Unit,
    private val onSelectionChanged: (String, IntRange) -> Unit,
    private val onCheckboxClicked: (String) -> Unit,
    private val onFocusChanged: (String, Boolean) -> Unit,
    private val onEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onNonEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onSplitLineEnterClicked: (String, Int) -> Unit,
    private val onEndLineEnterClicked: (String, Editable) -> Unit,
    private val onEndLineEnterTitleClicked: (Editable) -> Unit,
    private val onFooterClicked: () -> Unit,
    private val onPageClicked: (String) -> Unit,
    private val onTextInputClicked: () -> Unit,
    private val onAddUrlClick: (String, String) -> Unit,
    private val onAddLocalVideoClick: (String) -> Unit,
    private val onAddLocalPictureClick: (String) -> Unit,
    private val onAddLocalFileClick: (String) -> Unit,
    private val onPageIconClicked: () -> Unit,
    private val onDownloadFileClicked: (String) -> Unit,
    private val onBookmarkPlaceholderClicked: (String) -> Unit,
    private val onTogglePlaceholderClicked: (String) -> Unit,
    private val onToggleClicked: (String) -> Unit,
    private val onMediaBlockMenuClick: (String) -> Unit,
    private val onBookmarkMenuClicked: (String) -> Unit,
    private val onMarkupActionClicked: (Markup.Type) -> Unit,
    private val onLongClickListener: (BlockView) -> Unit
) : RecyclerView.Adapter<BlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_PARAGRAPH -> {
                BlockViewHolder.Paragraph(
                    view = inflater.inflate(
                        R.layout.item_block_text,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_TITLE -> {
                BlockViewHolder.Title(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_ONE -> {
                BlockViewHolder.HeaderOne(
                    view = inflater.inflate(
                        R.layout.item_block_header_one,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_TWO -> {
                BlockViewHolder.HeaderTwo(
                    view = inflater.inflate(
                        R.layout.item_block_header_two,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_THREE -> {
                BlockViewHolder.HeaderThree(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CODE_SNIPPET -> {
                BlockViewHolder.Code(
                    view = inflater.inflate(
                        R.layout.item_block_code_snippet,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CHECKBOX -> {
                BlockViewHolder.Checkbox(
                    view = inflater.inflate(
                        R.layout.item_block_checkbox,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_TASK -> {
                BlockViewHolder.Task(
                    view = inflater.inflate(
                        R.layout.item_block_task,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BULLET -> {
                BlockViewHolder.Bulleted(
                    view = inflater.inflate(
                        R.layout.item_block_bulleted,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_NUMBERED -> {
                BlockViewHolder.Numbered(
                    view = inflater.inflate(
                        R.layout.item_block_numbered,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_TOGGLE -> {
                BlockViewHolder.Toggle(
                    view = inflater.inflate(
                        R.layout.item_block_toggle,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_CONTACT -> {
                BlockViewHolder.Contact(
                    view = inflater.inflate(
                        R.layout.item_block_contact,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE -> {
                BlockViewHolder.File(
                    view = inflater.inflate(
                        R.layout.item_block_file,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE_PLACEHOLDER -> {
                BlockViewHolder.File.Placeholder(
                    view = inflater.inflate(
                        R.layout.item_block_file_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE_UPLOAD -> {
                BlockViewHolder.File.Upload(
                    view = inflater.inflate(
                        R.layout.item_block_file_uploading,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE_ERROR -> {
                BlockViewHolder.File.Error(
                    view = inflater.inflate(
                        R.layout.item_block_file_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO -> {
                BlockViewHolder.Video(
                    view = inflater.inflate(
                        R.layout.item_block_video,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO_PLACEHOLDER -> {
                BlockViewHolder.Video.Placeholder(
                    view = inflater.inflate(
                        R.layout.item_block_video_empty,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO_UPLOAD -> {
                BlockViewHolder.Video.Upload(
                    view = inflater.inflate(
                        R.layout.item_block_video_uploading,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO_ERROR -> {
                BlockViewHolder.Video.Error(
                    view = inflater.inflate(
                        R.layout.item_block_video_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PAGE -> {
                BlockViewHolder.Page(
                    view = inflater.inflate(
                        R.layout.item_block_page,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK -> {
                BlockViewHolder.Bookmark(
                    view = inflater.inflate(
                        R.layout.item_block_bookmark,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK_PLACEHOLDER -> {
                BlockViewHolder.Bookmark.Placeholder(
                    view = inflater.inflate(
                        R.layout.item_block_bookmark_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK_ERROR -> {
                BlockViewHolder.Bookmark.Error(
                    view = inflater.inflate(
                        R.layout.item_block_bookmark_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE -> {
                BlockViewHolder.Picture(
                    view = inflater.inflate(
                        R.layout.item_block_picture,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE_PLACEHOLDER -> {
                BlockViewHolder.Picture.Placeholder(
                    view = inflater.inflate(
                        R.layout.item_block_picture_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE_UPLOAD -> {
                BlockViewHolder.Picture.Upload(
                    view = inflater.inflate(
                        R.layout.item_block_picture_uploading,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE_ERROR -> {
                BlockViewHolder.Picture.Error(
                    view = inflater.inflate(
                        R.layout.item_block_picture_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_DIVIDER -> {
                BlockViewHolder.Divider(
                    view = inflater.inflate(
                        R.layout.item_block_divider,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HIGHLIGHT -> {
                BlockViewHolder.Highlight(
                    view = inflater.inflate(
                        R.layout.item_block_highlight,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FOOTER -> {
                BlockViewHolder.Footer(
                    view = inflater.inflate(
                        R.layout.item_block_footer,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int) = blocks[position].getViewType()

    override fun getItemCount(): Int = blocks.size

    override fun onBindViewHolder(
        holder: BlockViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position)
        else
            when (holder) {
                is BlockViewHolder.Paragraph -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Bulleted -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Checkbox -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Title -> {
                    holder.processPayloads(
                        payloads = payloads.typeOf(),
                        item = blocks[position] as BlockView.Title
                    )
                }
                is BlockViewHolder.Numbered -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.HeaderOne -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.HeaderTwo -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.HeaderThree -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Toggle -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                else -> throw IllegalStateException("Unexpected view holder: $holder")
            }
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is BlockViewHolder.Paragraph -> {
                holder.bind(
                    item = blocks[position] as BlockView.Paragraph,
                    onTextChanged = onParagraphTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Title -> {
                holder.bind(
                    item = blocks[position] as BlockView.Title,
                    onTitleTextChanged = onTitleTextChanged,
                    onFocusChanged = onFocusChanged,
                    onPageIconClicked = onPageIconClicked
                )
            }
            is BlockViewHolder.HeaderOne -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderOne,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.HeaderTwo -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderTwo,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.HeaderThree -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderThree,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code
                )
            }
            is BlockViewHolder.Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Checkbox,
                    onTextChanged = onTextChanged,
                    onCheckboxClicked = onCheckboxClicked,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Task -> {
                holder.bind(
                    item = blocks[position] as BlockView.Task
                )
            }
            is BlockViewHolder.Bulleted -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bulleted,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Numbered -> {
                holder.bind(
                    item = blocks[position] as BlockView.Numbered,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Toggle -> {
                holder.bind(
                    item = blocks[position] as BlockView.Toggle,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onTogglePlaceholderClicked = onTogglePlaceholderClicked,
                    onToggleClicked = onToggleClicked
                )
            }
            is BlockViewHolder.Contact -> {
                holder.bind(
                    item = blocks[position] as BlockView.Contact
                )
            }
            is BlockViewHolder.File -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.View,
                    onDownloadFileClicked = onDownloadFileClicked,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.File.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Error,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.File.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Upload
                )
            }
            is BlockViewHolder.File.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Placeholder,
                    onAddLocalFileClick = onAddLocalFileClick,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Video -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.View,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Video.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Upload
                )
            }
            is BlockViewHolder.Video.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Placeholder,
                    onAddLocalVideoClick = onAddLocalVideoClick,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Video.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Error,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page,
                    onPageClicked = onPageClicked
                )
            }
            is BlockViewHolder.Bookmark -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.View,
                    onBookmarkMenuClicked = onBookmarkMenuClicked
                )
            }
            is BlockViewHolder.Bookmark.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.Placeholder,
                    onBookmarkPlaceholderClicked = onBookmarkPlaceholderClicked
                )
            }
            is BlockViewHolder.Bookmark.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.Error,
                    onErrorBookmarkMenuClicked = onBookmarkMenuClicked
                )
            }
            is BlockViewHolder.Picture -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.View,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Picture.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Placeholder,
                    onAddLocalPictureClick = onAddLocalPictureClick,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Picture.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Error,
                    menuClick = onMediaBlockMenuClick
                )
            }
            is BlockViewHolder.Picture.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Upload
                )
            }
            is BlockViewHolder.Highlight -> {
                holder.bind(
                    item = blocks[position] as BlockView.Highlight,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Footer -> {
                holder.bind(onFooterClicked)
            }
        }

        if (holder is TextHolder) {

            val block = blocks[position]

            if (block is BlockView.Text) {
                holder.setBackgroundColor(
                    color = block.backgroundColor
                )
            }

            if (holder is BlockViewHolder.Title) {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterTitleClicked(editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        onSplitLineEnterClicked(blocks[holder.adapterPosition].id, index)
                    }
                )
            } else {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterClicked(blocks[holder.adapterPosition].id, editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        onSplitLineEnterClicked(blocks[holder.adapterPosition].id, index)
                    }
                )
            }

            holder.enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = { onEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id) },
                onNonEmptyBlockBackspaceClicked = { onNonEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id) }
            )

            holder.setOnClickListener(onTextInputClicked)
        }
    }

    // Bug workaround for losing text selection ability, see:
    // https://code.google.com/p/android/issues/detail?id=208169
    override fun onViewAttachedToWindow(holder: BlockViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is BlockViewHolder.Paragraph) {
            holder.content.isEnabled = false
            holder.content.isEnabled = true
        }
    }

    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "Consider RecyclerView's AsyncListDiffer instead. Or implement it with Kotlin coroutines."
    )
    fun updateWithDiffUtil(items: List<BlockView>) {
        logDataSetUpdateEvent(items)
        val result = DiffUtil.calculateDiff(BlockViewDiffUtil(old = blocks, new = items))
        blocks = items
        result.dispatchUpdatesTo(this)
    }

    private fun logDataSetUpdateEvent(items: List<BlockView>) {
        Timber.d("----------Updating------------")
        items.forEach { Timber.d(it.toString()) }
        Timber.d("----------Finished------------")
    }
}

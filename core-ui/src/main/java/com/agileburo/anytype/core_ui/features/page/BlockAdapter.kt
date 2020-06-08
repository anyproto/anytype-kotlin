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
import com.agileburo.anytype.core_ui.menu.AnytypeContextMenuEvent
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
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
    private val onTitleTextInputClicked: () -> Unit,
    private val onSelectionChanged: (String, IntRange) -> Unit,
    private val onCheckboxClicked: (String) -> Unit,
    private val onFocusChanged: (String, Boolean) -> Unit,
    private val onEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
    private val onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
    private val onEndLineEnterClicked: (String, Editable) -> Unit,
    private val onEndLineEnterTitleClicked: (Editable) -> Unit,
    private val onFooterClicked: () -> Unit,
    private val onPageClicked: (String) -> Unit,
    private val onTextInputClicked: (String) -> Unit,
    private val onClickListener: (ListenerType) -> Unit,
    private val onAddUrlClick: (String, String) -> Unit,
    private val onPageIconClicked: () -> Unit,
    private val onTogglePlaceholderClicked: (String) -> Unit,
    private val onToggleClicked: (String) -> Unit,
    private val onMarkupActionClicked: (Markup.Type) -> Unit,
    private val onLongClickListener: (String) -> Unit,
    private val clipboardInterceptor: ClipboardInterceptor,
    private val anytypeContextMenuListener: ((AnytypeContextMenuEvent) -> Unit)? = null
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
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_HEADER_TWO -> {
                BlockViewHolder.HeaderTwo(
                    view = inflater.inflate(
                        R.layout.item_block_header_two,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_HEADER_THREE -> {
                BlockViewHolder.HeaderThree(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
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
                        R.layout.item_block_video_placeholder,
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
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
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
        when {
            payloads.isEmpty() -> onBindViewHolder(holder, position)
            else -> when (holder) {
                is BlockViewHolder.Paragraph -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.Bulleted -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.Checkbox -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
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
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.HeaderOne -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.HeaderTwo -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.HeaderThree -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.Toggle -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.Highlight -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                is BlockViewHolder.File -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.File.Placeholder -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.File.Error -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.File.Upload -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Picture -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Picture.Placeholder -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Picture.Error -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Picture.Upload -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Video -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Video.Placeholder -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Video.Error -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Video.Upload -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Page -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Bookmark -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Bookmark.Placeholder -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Bookmark.Error -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Code -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position],
                        onTextChanged = onParagraphTextChanged,
                        onSelectionChanged = onSelectionChanged
                    )
                }
                else -> throw IllegalStateException("Unexpected view holder: $holder")
            }
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
                    onLongClickListener = onLongClickListener,
                    anytypeContextMenuListener = anytypeContextMenuListener
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
                    block = blocks[position] as BlockView.HeaderOne,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.HeaderTwo -> {
                holder.bind(
                    block = blocks[position] as BlockView.HeaderTwo,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.HeaderThree -> {
                holder.bind(
                    block = blocks[position] as BlockView.HeaderThree,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Checkbox,
                    onTextChanged = onTextChanged,
                    onCheckboxClicked = onCheckboxClicked,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener
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
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Numbered -> {
                holder.bind(
                    item = blocks[position] as BlockView.Numbered,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Toggle -> {
                holder.bind(
                    item = blocks[position] as BlockView.Toggle,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onTogglePlaceholderClicked = onTogglePlaceholderClicked,
                    onToggleClicked = onToggleClicked,
                    onLongClickListener = onLongClickListener
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
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.File.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Error,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.File.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Upload,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.File.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.File.Placeholder,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.View,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Upload,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Placeholder,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Error,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page,
                    onPageClicked = onPageClicked,
                    onLongClickListener = onLongClickListener
                )
            }
            is BlockViewHolder.Bookmark -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.View,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Bookmark.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.Placeholder,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Bookmark.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.Error,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Picture -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.View,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Picture.Placeholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Placeholder,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Picture.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Error,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Picture.Upload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Upload,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Highlight -> {
                holder.bind(
                    item = blocks[position] as BlockView.Highlight,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onLongClickListener = onLongClickListener,
                    onSelectionChanged = onSelectionChanged
                )
            }
            is BlockViewHolder.Footer -> {
                holder.bind(onFooterClicked)
            }
            is BlockViewHolder.Divider -> {
                holder.bind(
                    item = blocks[position] as BlockView.Divider,
                    onLongClickListener = onLongClickListener
                )
            }
        }

        if (holder is TextHolder) {

            val block = blocks[position]

            if (block is BlockView.Text) {
                holder.setBackgroundColor(
                    color = block.backgroundColor
                )
            }

            if (block is BlockView.Alignable) {
                block.alignment?.let {
                    holder.setAlignment(alignment = it)
                }
            }

            if (holder is BlockViewHolder.Title) {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterTitleClicked(editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        onSplitLineEnterClicked(blocks[holder.adapterPosition].id, index, holder.content.text!!)
                    }
                )
            } else {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterClicked(blocks[holder.adapterPosition].id, editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        onSplitLineEnterClicked(blocks[holder.adapterPosition].id, index, holder.content.text!!)
                    }
                )
            }

            holder.enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                        onEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id)
                        Timber.d("Proceed onEmptyBlockBackspaceClicked for adapter position:${holder.adapterPosition}")
                    } else {
                        Timber.e("Can't proceed with onEmptyBlockBackspaceClicked, because holder.adapter position is NO_POSITION")
                    }
                },
                onNonEmptyBlockBackspaceClicked = {
                    if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                        onNonEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id, holder.content.text!!)
                        Timber.d("Proceed onNonEmptyBlockBackspaceClicked for adapter position:${holder.adapterPosition}")
                    } else {
                        Timber.e("Can't proceed with onNonEmptyBlockBackspaceClicked, because holder.adapter position is NO_POSITION")
                    }
                }
            )

            if (holder is BlockViewHolder.Title)
                holder.setOnClickListener { onTitleTextInputClicked() }
            else
                holder.setOnClickListener { onTextInputClicked(blocks[holder.adapterPosition].id) }

            holder.content.clipboardInterceptor = clipboardInterceptor
        }
    }

    // Bug workaround for losing text selection ability, see:
    // https://code.google.com/p/android/issues/detail?id=208169
    override fun onViewAttachedToWindow(holder: BlockViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is TextHolder) {
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

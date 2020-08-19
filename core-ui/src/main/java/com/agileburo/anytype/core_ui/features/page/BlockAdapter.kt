package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.editor.holders.*
import com.agileburo.anytype.core_ui.features.editor.holders.placeholders.BookmarkPlaceholder
import com.agileburo.anytype.core_ui.features.editor.holders.placeholders.FilePlaceholder
import com.agileburo.anytype.core_ui.features.editor.holders.placeholders.PicturePlaceholder
import com.agileburo.anytype.core_ui.features.editor.holders.placeholders.VideoPlaceholder
import com.agileburo.anytype.core_ui.features.editor.holders.upload.FileUpload
import com.agileburo.anytype.core_ui.features.editor.holders.upload.PictureUpload
import com.agileburo.anytype.core_ui.features.editor.holders.upload.VideoUpload
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BULLET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_DIVIDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_UPLOAD
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
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PROFILE_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_UPLOAD
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
    private val onParagraphTextChanged: (BlockView.Paragraph) -> Unit,
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
    private val onTextInputClicked: (String) -> Unit,
    private val onClickListener: (ListenerType) -> Unit,
    private val onPageIconClicked: () -> Unit,
    private val onProfileIconClicked: () -> Unit,
    private val onTogglePlaceholderClicked: (String) -> Unit,
    private val onToggleClicked: (String) -> Unit,
    private val onMarkupActionClicked: (Markup.Type, IntRange) -> Unit,
    private val clipboardInterceptor: ClipboardInterceptor,
    private val onMentionEvent: (MentionEvent) -> Unit
) : RecyclerView.Adapter<BlockViewHolder>() {

    val views: List<BlockView> get() = blocks

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
                Title.Document(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PROFILE_TITLE -> {
                Title.Profile(
                    view = inflater.inflate(
                        R.layout.item_block_title_profile,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_ONE -> {
                HeaderOne(
                    view = inflater.inflate(
                        R.layout.item_block_header_one,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_HEADER_TWO -> {
                HeaderTwo(
                    view = inflater.inflate(
                        R.layout.item_block_header_two,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_HEADER_THREE -> {
                HeaderThree(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_CODE_SNIPPET -> {
                Code(
                    view = inflater.inflate(
                        R.layout.item_block_code_snippet,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CHECKBOX -> {
                Checkbox(
                    view = inflater.inflate(
                        R.layout.item_block_checkbox,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_BULLET -> {
                Bulleted(
                    view = inflater.inflate(
                        R.layout.item_block_bulleted,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_NUMBERED -> {
                Numbered(
                    view = inflater.inflate(
                        R.layout.item_block_numbered,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
                )
            }
            HOLDER_TOGGLE -> {
                Toggle(
                    view = inflater.inflate(
                        R.layout.item_block_toggle,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
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
                FilePlaceholder(
                    view = inflater.inflate(
                        R.layout.item_block_file_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE_UPLOAD -> {
                FileUpload(
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
                VideoPlaceholder(
                    view = inflater.inflate(
                        R.layout.item_block_video_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO_UPLOAD -> {
                VideoUpload(
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
                Page(
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
                BookmarkPlaceholder(
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
                PicturePlaceholder(
                    view = inflater.inflate(
                        R.layout.item_block_picture_placeholder,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE_UPLOAD -> {
                PictureUpload(
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
                Divider(
                    view = inflater.inflate(
                        R.layout.item_block_divider,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HIGHLIGHT -> {
                Highlight(
                    view = inflater.inflate(
                        R.layout.item_block_highlight,
                        parent,
                        false
                    ),
                    onMarkupActionClicked = onMarkupActionClicked
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
            else -> {
                if (holder is BlockViewHolder.IndentableHolder) {
                    holder.processIndentChange(blocks[position], payloads.typeOf())
                }
                when (holder) {
                    is BlockViewHolder.Paragraph -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = { _, editable ->
                                val p = (blocks[position] as BlockView.Paragraph)
                                p.apply {
                                    text = editable.toString()
                                    marks = editable.marks()
                                }
                                onParagraphTextChanged(p)
                            },
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is Bulleted -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is Checkbox -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is Title.Document -> {
                        holder.processPayloads(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Title
                        )
                    }
                    is Title.Profile -> {
                        holder.processPayloads(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Title.Profile
                        )
                    }
                    is Numbered -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is HeaderOne -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is HeaderTwo -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is HeaderThree -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is Toggle -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is Highlight -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    is BlockViewHolder.File -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is FilePlaceholder -> {
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
                    is FileUpload -> {
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
                    is PicturePlaceholder -> {
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
                    is PictureUpload -> {
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
                    is VideoPlaceholder -> {
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
                    is VideoUpload -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is Page -> {
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
                    is BookmarkPlaceholder -> {
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
                    is Code -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener
                        )
                    }
                    else -> throw IllegalStateException("Unexpected view holder: $holder")
                }
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
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent
                )
            }
            is Title.Document -> {
                holder.bind(
                    item = blocks[position] as BlockView.Title.Document,
                    onTitleTextChanged = onTitleTextChanged,
                    onFocusChanged = onFocusChanged,
                    onPageIconClicked = onPageIconClicked
                )
            }
            is Title.Profile -> {
                holder.bind(
                    item = blocks[position] as BlockView.Title.Profile,
                    onTitleTextChanged = onTitleTextChanged,
                    onFocusChanged = onFocusChanged,
                    onProfileIconClicked = onProfileIconClicked
                )
            }
            is HeaderOne -> {
                holder.bind(
                    block = blocks[position] as BlockView.Header.One,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    clicked = onClickListener
                )
            }
            is HeaderTwo -> {
                holder.bind(
                    block = blocks[position] as BlockView.Header.Two,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    clicked = onClickListener
                )
            }
            is HeaderThree -> {
                holder.bind(
                    block = blocks[position] as BlockView.Header.Three,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    clicked = onClickListener
                )
            }
            is Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener
                )
            }
            is Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Checkbox,
                    onTextChanged = onTextChanged,
                    onCheckboxClicked = onCheckboxClicked,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener
                )
            }
            is Bulleted -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bulleted,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener
                )
            }
            is Numbered -> {
                holder.bind(
                    item = blocks[position] as BlockView.Numbered,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener
                )
            }
            is Toggle -> {
                holder.bind(
                    item = blocks[position] as BlockView.Toggle,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    onSelectionChanged = onSelectionChanged,
                    onTogglePlaceholderClicked = onTogglePlaceholderClicked,
                    onToggleClicked = onToggleClicked,
                    clicked = onClickListener
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
            is FileUpload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Upload.File,
                    clicked = onClickListener
                )
            }
            is FilePlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.File,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.View,
                    clicked = onClickListener
                )
            }
            is VideoUpload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Upload.Video,
                    clicked = onClickListener
                )
            }
            is VideoPlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.Video,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Video.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Video.Error,
                    clicked = onClickListener
                )
            }
            is Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Bookmark -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark.View,
                    clicked = onClickListener
                )
            }
            is BookmarkPlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.Bookmark,
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
            is PicturePlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.Picture,
                    clicked = onClickListener
                )
            }
            is BlockViewHolder.Picture.Error -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture.Error,
                    clicked = onClickListener
                )
            }
            is PictureUpload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Upload.Picture,
                    clicked = onClickListener
                )
            }
            is Highlight -> {
                holder.bind(
                    item = blocks[position] as BlockView.Highlight,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener,
                    onSelectionChanged = onSelectionChanged
                )
            }
            is Divider -> {
                holder.bind(
                    item = blocks[position] as BlockView.Divider,
                    clicked = onClickListener
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

            if (holder is Title.Document || holder is Title.Profile) {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterTitleClicked(editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        holder.content.text?.let { editable ->
                            onSplitLineEnterClicked(
                                blocks[holder.adapterPosition].id,
                                index,
                                editable
                            )
                        }
                    }
                )
            } else {
                holder.enableEnterKeyDetector(
                    onEndLineEnterClicked = { editable ->
                        onEndLineEnterClicked(blocks[holder.adapterPosition].id, editable)
                    },
                    onSplitLineEnterClicked = { index ->
                        holder.content.text?.let { editable ->
                            onSplitLineEnterClicked(
                                blocks[holder.adapterPosition].id,
                                index,
                                editable
                            )
                        }
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
                        holder.content.text?.let { editable ->
                            onNonEmptyBlockBackspaceClicked(
                                blocks[holder.adapterPosition].id,
                                editable
                            )
                        }
                        Timber.d("Proceed onNonEmptyBlockBackspaceClicked for adapter position:${holder.adapterPosition}")
                    } else {
                        Timber.e("Can't proceed with onNonEmptyBlockBackspaceClicked, because holder.adapter position is NO_POSITION")
                    }
                }
            )

            if (holder is Title.Document || holder is Title.Profile)
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

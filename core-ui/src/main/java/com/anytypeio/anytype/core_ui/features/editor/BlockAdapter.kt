package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_BOOKMARK_ERROR
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_BULLET
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_DIVIDER_DOTS
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_DIVIDER_LINE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_FILE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_FILE_ERROR
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_FILE_UPLOAD
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_HEADER_ONE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_HEADER_THREE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_HEADER_TWO
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_NUMBERED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PAGE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PAGE_ARCHIVE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PARAGRAPH
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PICTURE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PICTURE_ERROR
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PICTURE_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PICTURE_UPLOAD
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PROFILE_TITLE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_TITLE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_VIDEO
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_VIDEO_ERROR
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_VIDEO_UPLOAD
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.error.BookmarkError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.FileError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.PictureError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.VideoError
import com.anytypeio.anytype.core_ui.features.editor.holders.ext.setup
import com.anytypeio.anytype.core_ui.features.editor.holders.ext.setupPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.media.*
import com.anytypeio.anytype.core_ui.features.editor.holders.other.*
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.BookmarkPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.FilePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.PicturePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.VideoPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.FeaturedRelationListViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.RelationViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.text.*
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.FileUpload
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.PictureUpload
import com.anytypeio.anytype.core_ui.features.editor.holders.upload.VideoUpload
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.tools.LockableFocusChangeListener
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_DESCRIPTION
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_FEATURED_RELATION
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_DEFAULT
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_FILE
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_OBJECT
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_STATUS
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_RELATION_TAGS
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_TODO_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_UNSUPPORTED
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import kotlinx.android.synthetic.main.item_block_description.view.*
import timber.log.Timber
import java.util.*

/**
 * Adapter for rendering list of blocks.
 * @property blocks mutable list of blocks
 * @see BlockView
 * @see BlockViewHolder
 * @see BlockViewDiffUtil
 */
class BlockAdapter(
    private var restore: Queue<Editor.Restore>,
    private var blocks: List<BlockView>,
    private val onDescriptionChanged: (BlockView.Description) -> Unit = {},
    private val onTextBlockTextChanged: (BlockView.Text) -> Unit,
    private val onTextChanged: (String, Editable) -> Unit,
    private val onTitleBlockTextChanged: (BlockView.Title) -> Unit,
    private val onTitleTextInputClicked: () -> Unit,
    private val onSelectionChanged: (String, IntRange) -> Unit,
    private val onCheckboxClicked: (BlockView.Text.Checkbox) -> Unit,
    private val onTitleCheckboxClicked: (BlockView.Title.Todo) -> Unit = {},
    private val onFocusChanged: (Id, Boolean) -> Unit,
    private val onEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
    private val onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
    private val onSplitDescription: (Id, Editable, IntRange) -> Unit,
    private val onTextInputClicked: (String) -> Unit,
    val onClickListener: (ListenerType) -> Unit,
    private val onPageIconClicked: () -> Unit,
    private val onProfileIconClicked: () -> Unit,
    private val onCoverClicked: () -> Unit,
    private val onTogglePlaceholderClicked: (String) -> Unit,
    private val onToggleClicked: (String) -> Unit,
    private val onContextMenuStyleClick: (IntRange) -> Unit,
    private val clipboardInterceptor: ClipboardInterceptor,
    private val onMentionEvent: (MentionEvent) -> Unit,
    private val onSlashEvent: (SlashEvent) -> Unit,
    private val onBackPressedCallback: () -> Boolean,
    private val onKeyPressedEvent: (KeyPressedEvent) -> Unit,
    private val onDragAndDropTrigger: (RecyclerView.ViewHolder) -> Boolean,
    private val onDragListener: View.OnDragListener
) : RecyclerView.Adapter<BlockViewHolder>() {

    val views: List<BlockView> get() = blocks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val holder = when (viewType) {
            HOLDER_PARAGRAPH -> {
                Paragraph(
                    view = inflater.inflate(
                        R.layout.item_block_text,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_TITLE -> {
                Title.Document(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                ).apply {
                    with(content) {
                        enableEnterKeyDetector(
                            onEnterClicked = { range ->
                                onTitleEnterKeyListener(
                                    views = views,
                                    textView = content,
                                    range = range,
                                    onKeyPressedEvent = onKeyPressedEvent
                                )
                            }
                        )
                        selectionWatcher = { selection ->
                            val pos = bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                val view = views[pos]
                                if (view is BlockView.Title.Basic) {
                                    view.cursor = selection.last
                                }
                                onSelectionChanged(view.id, selection)
                            }
                        }
                    }
                }
            }
            HOLDER_PROFILE_TITLE -> {
                Title.Profile(
                    view = inflater.inflate(
                        R.layout.item_block_title_profile,
                        parent,
                        false
                    )
                ).apply {
                    with(content) {
                        enableEnterKeyDetector(
                            onEnterClicked = { range ->
                                onTitleEnterKeyListener(
                                    views = views,
                                    textView = content,
                                    range = range,
                                    onKeyPressedEvent = onKeyPressedEvent
                                )
                            }
                        )
                        selectionWatcher = { selection ->
                            val pos = bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                val view = views[pos]
                                if (view is BlockView.Title.Profile) {
                                    view.cursor = selection.last
                                }
                                onSelectionChanged(view.id, selection)
                            }
                        }
                    }
                }
            }
            HOLDER_TODO_TITLE -> {
                Title.Todo(
                    view = inflater.inflate(
                        R.layout.item_block_title_todo,
                        parent,
                        false
                    )
                ).apply {
                    checkbox.setOnClickListener {
                        val view = views[bindingAdapterPosition]
                        check(view is BlockView.Title.Todo)
                        view.isChecked = !view.isChecked
                        checkbox.isSelected = view.isChecked
                        onTitleCheckboxClicked(view)
                    }
                    with(content) {
                        enableEnterKeyDetector(
                            onEnterClicked = { range ->
                                onTitleEnterKeyListener(
                                    views = views,
                                    textView = content,
                                    range = range,
                                    onKeyPressedEvent = onKeyPressedEvent
                                )
                            }
                        )
                        selectionWatcher = { selection ->
                            val pos = bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                val view = views[pos]
                                if (view is BlockView.Title.Todo) {
                                    view.cursor = selection.last
                                }
                                onSelectionChanged(view.id, selection)
                            }
                        }
                    }
                }
            }
            HOLDER_HEADER_ONE -> {
                HeaderOne(
                    view = inflater.inflate(
                        R.layout.item_block_header_one,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_HEADER_TWO -> {
                HeaderTwo(
                    view = inflater.inflate(
                        R.layout.item_block_header_two,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_HEADER_THREE -> {
                HeaderThree(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
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
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_BULLET -> {
                Bulleted(
                    view = inflater.inflate(
                        R.layout.item_block_bulleted,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_NUMBERED -> {
                Numbered(
                    view = inflater.inflate(
                        R.layout.item_block_numbered,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_TOGGLE -> {
                Toggle(
                    view = inflater.inflate(
                        R.layout.item_block_toggle,
                        parent,
                        false
                    ),
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_DESCRIPTION -> {
                Description(
                    view = inflater.inflate(
                        R.layout.item_block_description,
                        parent,
                        false
                    )
                ).apply {
                    with(content) {
                        enableEnterKeyDetector(
                            onEnterClicked = { range ->
                                onDescriptionEnterKeyListener(
                                    views = views,
                                    textView = content,
                                    range = range,
                                    onKeyPressedEvent = onKeyPressedEvent
                                )
                            }
                        )
                        addTextChangedListener(
                            DefaultTextWatcher { editable ->
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val view = views[pos]
                                    check(view is BlockView.Description)
                                    view.text = editable.toString()
                                    onDescriptionChanged(view)
                                }
                            }
                        )
                        setOnFocusChangeListener { _, hasFocus ->
                            val pos = bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                onFocusChanged(blocks[pos].id, hasFocus)
                            }
                        }
                        setOnEditorActionListener { v, actionId, _ ->
                            if (actionId == TextInputWidget.TEXT_INPUT_WIDGET_ACTION_GO) {
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    onSplitDescription(
                                        views[pos].id,
                                        v.editableText,
                                        v.selectionStart..v.selectionEnd
                                    )
                                    return@setOnEditorActionListener true
                                }
                            }
                            false
                        }
                        selectionWatcher = { selection ->
                            val pos = bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                val view = views[pos]
                                if (view is BlockView.Description) {
                                    view.cursor = selection.last
                                }
                                onSelectionChanged(view.id, selection)
                            }
                        }
                    }
                }
            }
            HOLDER_FILE -> {
                File(
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
                FileError(
                    view = inflater.inflate(
                        R.layout.item_block_file_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_VIDEO -> {
                Video(
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
                VideoError(
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
            HOLDER_PAGE_ARCHIVE -> {
                PageArchive(
                    view = inflater.inflate(
                        R.layout.item_block_page_archived,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK -> {
                Bookmark(
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
                BookmarkError(
                    view = inflater.inflate(
                        R.layout.item_block_bookmark_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE -> {
                Picture(
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
                PictureError(
                    view = inflater.inflate(
                        R.layout.item_block_picture_error,
                        parent,
                        false
                    )
                )
            }
            HOLDER_DIVIDER_LINE -> {
                DividerLine(
                    view = inflater.inflate(
                        R.layout.item_block_divider_line,
                        parent,
                        false
                    )
                )
            }
            HOLDER_DIVIDER_DOTS -> {
                DividerDots(
                    view = inflater.inflate(
                        R.layout.item_block_divider_dots,
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
                    onContextMenuStyleClick = onContextMenuStyleClick
                )
            }
            HOLDER_RELATION_DEFAULT -> {
                RelationViewHolder.Default(
                    view = inflater.inflate(
                        R.layout.item_block_relation_default,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_RELATION_PLACEHOLDER -> {
                RelationViewHolder.Placeholder(
                    view = inflater.inflate(
                        R.layout.item_block_relation_placeholder,
                        parent,
                        false
                    )
                ).setupPlaceholder(this)
            }
            HOLDER_RELATION_STATUS -> {
                RelationViewHolder.Status(
                    view = inflater.inflate(
                        R.layout.item_block_relation_status,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_RELATION_TAGS -> {
                RelationViewHolder.Tags(
                    view = inflater.inflate(
                        R.layout.item_block_relation_tag,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_RELATION_OBJECT -> {
                RelationViewHolder.Object(
                    view = inflater.inflate(
                        R.layout.item_block_relation_object,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_RELATION_FILE -> {
                RelationViewHolder.File(
                    view = inflater.inflate(
                        R.layout.item_block_relation_file,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_RELATION_CHECKBOX -> {
                RelationViewHolder.Checkbox(
                    view = inflater.inflate(
                        R.layout.item_block_relation_checkbox,
                        parent,
                        false
                    )
                ).setup(this)
            }
            HOLDER_FEATURED_RELATION -> {
                FeaturedRelationListViewHolder(
                    view = inflater.inflate(
                        R.layout.item_block_featured_relations,
                        parent,
                        false
                    )
                )
            }
            HOLDER_UNSUPPORTED -> Unsupported(parent)
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }

        if (holder is Text) {
            holder.content.setOnDragListener(onDragListener)
            holder.content.editorTouchProcessor.onLongClick = {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                }
            }
            holder.content.editorTouchProcessor.onDragAndDropTrigger = {
                onDragAndDropTrigger(holder)
            }
            holder.content.setOnClickListener { view ->
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
//                    if (view.hasFocus()) {
//                        view.context.imm().showSoftInput(view, InputMethodManager.SHOW_FORCED)
//                    }
                    onTextInputClicked(blocks[pos].id)
                }
            }
            holder.content.onFocusChangeListener = LockableFocusChangeListener { hasFocus ->
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = views[pos]
                    if (item is BlockView.TextBlockProps) {
                        item.isFocused = hasFocus
                    }
                    onFocusChanged(item.id, hasFocus)
//                    if (hasFocus) {
//                        holder.content.context
//                            .imm()
//                            .showSoftInput(holder.content, InputMethodManager.SHOW_FORCED)
//                    }
                }
            }
            holder.content.selectionWatcher = { selection ->
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val view = views[pos]
                    if (view is BlockView.Text) {
                        view.cursor = selection.last
                    }
                    onSelectionChanged(view.id, selection)
                }
            }
        } else {
            if (holder !is SupportCustomTouchProcessor) {
                holder.itemView.setOnLongClickListener {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                    }
                    true
                }
            } else {
                holder.editorTouchProcessor.onLongClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                    }
                }
                holder.editorTouchProcessor.onDragAndDropTrigger = {
                    onDragAndDropTrigger(holder)
                }
            }
            holder.itemView.setOnDragListener(onDragListener)
        }

        return holder
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
                    is Paragraph -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is Bulleted -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is Checkbox -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
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
                    is Title.Todo -> {
                        holder.processPayloads(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Title.Todo
                        )
                    }
                    is Numbered -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is HeaderOne -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is HeaderTwo -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is HeaderThree -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is Toggle -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is Highlight -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position],
                            onTextChanged = onTextBlockTextChanged,
                            onSelectionChanged = onSelectionChanged,
                            clicked = onClickListener,
                            onMentionEvent = onMentionEvent,
                            onSlashEvent = onSlashEvent
                        )
                    }
                    is File -> {
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
                    is FileError -> {
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
                    is Picture -> {
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
                    is PictureError -> {
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
                    is Video -> {
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
                    is VideoError -> {
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
                    is PageArchive -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is Bookmark -> {
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
                    is BookmarkError -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is Code -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Code,
                            onTextChanged = onTextChanged,
                            onSelectionChanged = onSelectionChanged
                        )
                    }
                    is DividerLine -> onBindViewHolder(holder, position)
                    is DividerDots -> onBindViewHolder(holder, position)
                    is RelationViewHolder.Placeholder -> onBindViewHolder(holder, position)
                    is RelationViewHolder -> onBindViewHolder(holder, position)
                    is Description -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Description
                        )
                    }
                    else -> throw IllegalStateException("Unexpected view holder: $holder")
                }
            }
        }

        if (restore.isNotEmpty()) {
            val block = blocks[position]
            val command = restore.poll()
            if (command is Editor.Restore.Selection) {
                if (block.id == command.target) {
                    if (holder is TextHolder) {
                        holder.content.post {
                            holder.content.setSelection(command.range.first, command.range.last)
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is Paragraph -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Paragraph,
                    onTextChanged = { _, editable ->
                        if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            val item = views[holder.bindingAdapterPosition]
                            check(item is BlockView.Text.Paragraph)
                            item.apply {
                                text = editable.toString()
                                marks = editable.marks()
                            }
                            onTextBlockTextChanged(item)
                        }
                    },
                    onSelectionChanged = onSelectionChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onTextInputClicked = onTextInputClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is HeaderOne -> {
                holder.bind(
                    block = blocks[position] as BlockView.Text.Header.One,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is HeaderTwo -> {
                holder.bind(
                    block = blocks[position] as BlockView.Text.Header.Two,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is HeaderThree -> {
                holder.bind(
                    block = blocks[position] as BlockView.Text.Header.Three,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Checkbox,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    onCheckboxClicked = onCheckboxClicked,
                    onSelectionChanged = onSelectionChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onTextInputClicked = onTextInputClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Bulleted -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Bulleted,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Numbered -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Numbered,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Toggle -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Toggle,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    onTogglePlaceholderClicked = onTogglePlaceholderClicked,
                    onToggleClicked = onToggleClicked,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Highlight -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text.Highlight,
                    onTextBlockTextChanged = onTextBlockTextChanged,
                    clicked = onClickListener,
                    onMentionEvent = onMentionEvent,
                    onSlashEvent = onSlashEvent,
                    onSelectionChanged = onSelectionChanged,
                    onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                    onSplitLineEnterClicked = onSplitLineEnterClicked,
                    onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                    onTextInputClicked = onTextInputClicked,
                    onBackPressedCallback = onBackPressedCallback
                )
            }
            is Title.Document -> {
                holder.apply {
                    bind(
                        item = blocks[position] as BlockView.Title.Basic,
                        onTitleTextChanged = onTitleBlockTextChanged,
                        onFocusChanged = onFocusChanged,
                        onPageIconClicked = onPageIconClicked,
                        onCoverClicked = onCoverClicked
                    )
                    setTextInputClickListener {
                        if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                            content.context.imm()
                                .showSoftInput(content, InputMethodManager.SHOW_FORCED)
                        }
                        onTitleTextInputClicked()
                    }
                    holder.content.clipboardInterceptor = clipboardInterceptor
                }
            }
            is Title.Todo -> {
                holder.apply {
                    bind(
                        item = blocks[position] as BlockView.Title.Todo,
                        onTitleTextChanged = onTitleBlockTextChanged,
                        onFocusChanged = onFocusChanged,
                        onPageIconClicked = onPageIconClicked,
                        onCoverClicked = onCoverClicked
                    )
                    setTextInputClickListener {
                        if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                            content.context.imm()
                                .showSoftInput(content, InputMethodManager.SHOW_FORCED)
                        }
                        onTitleTextInputClicked()
                    }
                    holder.content.clipboardInterceptor = clipboardInterceptor
                }
            }
            is Title.Profile -> {
                holder.apply {
                    bind(
                        item = blocks[position] as BlockView.Title.Profile,
                        onTitleTextChanged = onTitleBlockTextChanged,
                        onFocusChanged = onFocusChanged,
                        onProfileIconClicked = onProfileIconClicked,
                        onCoverClicked = onCoverClicked
                    )
                    setTextInputClickListener {
                        if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                            content.context.imm()
                                .showSoftInput(content, InputMethodManager.SHOW_FORCED)
                        }
                        onTitleTextInputClicked()
                    }
                    holder.content.clipboardInterceptor = clipboardInterceptor
                }
            }
            is Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged,
                    clicked = onClickListener,
                    onTextInputClicked = onTextInputClicked
                )
            }
            is Description -> {
                holder.bind(blocks[position] as BlockView.Description)
            }
            is File -> {
                holder.bind(
                    item = blocks[position] as BlockView.Media.File,
                    clicked = onClickListener
                )
            }
            is FileError -> {
                holder.bind(
                    item = blocks[position] as BlockView.Error.File,
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
            is Video -> {
                holder.bind(
                    item = blocks[position] as BlockView.Media.Video,
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
            is VideoError -> {
                holder.bind(
                    item = blocks[position] as BlockView.Error.Video,
                    clicked = onClickListener
                )
            }
            is Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page,
                    clicked = onClickListener
                )
            }
            is PageArchive -> {
                holder.bind(
                    item = blocks[position] as BlockView.PageArchive,
                    clicked = onClickListener
                )
            }
            is Bookmark -> {
                holder.bind(
                    item = blocks[position] as BlockView.Media.Bookmark,
                    clicked = onClickListener
                )
            }
            is BookmarkPlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.Bookmark,
                    clicked = onClickListener
                )
            }
            is BookmarkError -> {
                val item = blocks[position] as BlockView.Error.Bookmark
                holder.bind(
                    item = item,
                    clicked = onClickListener
                )
                holder.setUrl(item.url)
            }
            is Picture -> {
                holder.bind(
                    item = blocks[position] as BlockView.Media.Picture,
                    clicked = onClickListener
                )
            }
            is PicturePlaceholder -> {
                holder.bind(
                    item = blocks[position] as BlockView.MediaPlaceholder.Picture,
                    clicked = onClickListener
                )
            }
            is PictureError -> {
                holder.bind(
                    item = blocks[position] as BlockView.Error.Picture,
                    clicked = onClickListener
                )
            }
            is PictureUpload -> {
                holder.bind(
                    item = blocks[position] as BlockView.Upload.Picture,
                    clicked = onClickListener
                )
            }
            is DividerLine -> {
                holder.bind(
                    item = blocks[position] as BlockView.DividerLine,
                    clicked = onClickListener
                )
            }
            is DividerDots -> {
                holder.bind(
                    item = blocks[position] as BlockView.DividerDots,
                    clicked = onClickListener
                )
            }
            is RelationViewHolder.Placeholder -> {
                val item = (blocks[position] as BlockView.Relation.Placeholder)
                holder.bind(item = item)
            }
            is RelationViewHolder.Default -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is RelationViewHolder.Status -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view as DocumentRelationView.Status)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is RelationViewHolder.Tags -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view as DocumentRelationView.Tags)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is RelationViewHolder.Object -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view as DocumentRelationView.Object)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is RelationViewHolder.File -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view as DocumentRelationView.File)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is RelationViewHolder.Checkbox -> {
                val item = (blocks[position] as BlockView.Relation.Related)
                holder.bind(item = item.view as DocumentRelationView.Checkbox)
                holder.indentize(item = item)
                holder.setBackgroundColor(item.background)
                val container = holder.itemView.findViewById<ViewGroup>(R.id.content)
                container.isSelected = item.isSelected
            }
            is FeaturedRelationListViewHolder -> {
                holder.bind(
                    item = blocks[position] as BlockView.FeaturedRelation,
                    click = onClickListener
                )
            }
            is Unsupported -> {
                holder.bind(item = blocks[position] as BlockView.Unsupported)
            }
        }

        if (holder is Text) {

            val block = blocks[position]

            if (block is BlockView.Alignable) {
                block.alignment?.let {
                    holder.setAlignment(alignment = it)
                }
            }

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
        if (BuildConfig.DEBUG) {
            logDataSetUpdateEvent(items)
        }
        val result = DiffUtil.calculateDiff(BlockViewDiffUtil(old = blocks, new = items))
        blocks = items
        result.dispatchUpdatesTo(this)
    }

    private fun logDataSetUpdateEvent(items: List<BlockView>) {
        Timber.d("----------Updating BlockView---------------------\n$items")
        Timber.d("----------Finished Updating BlockView------------")
    }
}

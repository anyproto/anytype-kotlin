package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBookmarkBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBookmarkErrorBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBulletedBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockCodeSnippetBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDescriptionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDividerDotsBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDividerLineBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFeaturedRelationsBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderOneBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderThreeBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderTwoBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHighlightBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockLatexBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockNumberedBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkArchiveBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkDeleteBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkLoadingBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockPictureBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationPlaceholderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationTagBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTextBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleProfileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleTodoBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTocBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockToggleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockUnsupportedBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockVideoBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.error.BookmarkError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.FileError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.PictureError
import com.anytypeio.anytype.core_ui.features.editor.holders.error.VideoError
import com.anytypeio.anytype.core_ui.features.editor.holders.ext.setup
import com.anytypeio.anytype.core_ui.features.editor.holders.ext.setupPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Bookmark
import com.anytypeio.anytype.core_ui.features.editor.holders.media.File
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Picture
import com.anytypeio.anytype.core_ui.features.editor.holders.media.Video
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Code
import com.anytypeio.anytype.core_ui.features.editor.holders.other.DividerDots
import com.anytypeio.anytype.core_ui.features.editor.holders.other.DividerLine
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Latex
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObject
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObjectArchive
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObjectCard
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObjectDelete
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObjectLoading
import com.anytypeio.anytype.core_ui.features.editor.holders.other.TableOfContents
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Unsupported
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.BookmarkPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.FilePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.PicturePlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.placeholders.VideoPlaceholder
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.FeaturedRelationListViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.RelationViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Bulleted
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Checkbox
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Description
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderOne
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderThree
import com.anytypeio.anytype.core_ui.features.editor.holders.text.HeaderTwo
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Highlight
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Numbered
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Paragraph
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Text
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Toggle
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
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CODE_SNIPPET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DESCRIPTION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DIVIDER_DOTS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DIVIDER_LINE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FEATURED_RELATION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_ONE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_THREE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_LATEX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_ARCHIVE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_CARD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_DEFAULT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_DELETED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_LOADING
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PROFILE_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_DEFAULT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_FILE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_OBJECT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_STATUS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_TAGS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOC
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TODO_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_UNSUPPORTED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Payload
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
    private val onTitleBlockTextChanged: (Id, String) -> Unit,
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
    private val onCoverClicked: () -> Unit,
    private val onTogglePlaceholderClicked: (String) -> Unit,
    private val onToggleClicked: (String) -> Unit,
    private val clipboardInterceptor: ClipboardInterceptor,
    private val onMentionEvent: (MentionEvent) -> Unit,
    private val onSlashEvent: (SlashEvent) -> Unit,
    private val onBackPressedCallback: () -> Boolean,
    private val onKeyPressedEvent: (KeyPressedEvent) -> Unit,
    private val onDragAndDropTrigger: (RecyclerView.ViewHolder, event: MotionEvent?) -> Boolean,
    private val onDragListener: View.OnDragListener,
    private val lifecycle: Lifecycle,
    private val dragAndDropSelector: DragAndDropSelector,
) : RecyclerView.Adapter<BlockViewHolder>(), DragAndDropSelector by dragAndDropSelector {

    val views: List<BlockView> get() = blocks

    private var isInDragAndDropMode = false

    override fun selectDraggedViewHolder(position: Int) {
        isInDragAndDropMode = true
        dragAndDropSelector.selectDraggedViewHolder(position)
    }

    override fun unSelectDraggedViewHolder() {
        dragAndDropSelector.unSelectDraggedViewHolder()
        isInDragAndDropMode = false
    }

    override fun onViewDetachedFromWindow(holder: BlockViewHolder) {
        when (holder) {
            is Video -> {
                holder.pause()
            }
        }
    }

    override fun onViewRecycled(holder: BlockViewHolder) {
        when (holder) {
            is Video -> {
                holder.recycle()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val holder = when (viewType) {
            HOLDER_PARAGRAPH -> {
                Paragraph(ItemBlockTextBinding.inflate(inflater, parent, false))
            }
            HOLDER_TITLE -> {
                Title.Document(
                    ItemBlockTitleBinding.inflate(inflater, parent, false)
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
                        addTextChangedListener(
                            DefaultTextWatcher { editable ->
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val view = views[pos]
                                    check(view is BlockView.Title.Basic)
                                    view.text = editable.toString()
                                    onTitleBlockTextChanged(view.id, editable.toString())
                                }
                            }
                        )
                    }
                }
            }
            HOLDER_PROFILE_TITLE -> {
                Title.Profile(
                    ItemBlockTitleProfileBinding.inflate(inflater, parent, false)
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
                        addTextChangedListener(
                            DefaultTextWatcher { editable ->
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val view = views[pos]
                                    check(view is BlockView.Title.Profile)
                                    val text = editable.toString()
                                    view.text = text
                                    onTitleBlockTextChanged(view.id, text)
                                    onTitleTextChanged(text)
                                }
                            }
                        )
                    }
                }
            }
            HOLDER_TODO_TITLE -> {
                Title.Todo(
                    ItemBlockTitleTodoBinding.inflate(inflater, parent, false)
                ).apply {
                    checkbox.setOnClickListener {
                        if (!isLocked) {
                            val view = views[bindingAdapterPosition]
                            check(view is BlockView.Title.Todo)
                            view.isChecked = !view.isChecked
                            checkbox.isSelected = view.isChecked
                            onTitleCheckboxClicked(view)
                        }
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
                        addTextChangedListener(
                            DefaultTextWatcher { editable ->
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val view = views[pos]
                                    check(view is BlockView.Title.Todo)
                                    view.text = editable.toString()
                                    onTitleBlockTextChanged(view.id, editable.toString())
                                }
                            }
                        )
                    }
                }
            }
            HOLDER_HEADER_ONE -> {
                HeaderOne(
                    binding = ItemBlockHeaderOneBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_HEADER_TWO -> {
                HeaderTwo(
                    binding = ItemBlockHeaderTwoBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_HEADER_THREE -> {
                HeaderThree(
                    binding = ItemBlockHeaderThreeBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_CODE_SNIPPET -> {
                Code(
                    ItemBlockCodeSnippetBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_CHECKBOX -> {
                Checkbox(
                    binding = ItemBlockCheckboxBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_BULLET -> {
                Bulleted(
                    binding = ItemBlockBulletedBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_NUMBERED -> {
                Numbered(
                    binding = ItemBlockNumberedBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_TOGGLE -> {
                Toggle(
                    binding = ItemBlockToggleBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_DESCRIPTION -> {
                Description(
                    ItemBlockDescriptionBinding.inflate(inflater, parent, false)
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
                    ItemBlockFileBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_FILE_PLACEHOLDER -> {
                FilePlaceholder(
                    ItemBlockMediaPlaceholderBinding.inflate(inflater, parent, false)
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
                    ItemBlockVideoBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_VIDEO_PLACEHOLDER -> {
                VideoPlaceholder(
                    ItemBlockMediaPlaceholderBinding.inflate(inflater, parent, false)
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
            HOLDER_OBJECT_LINK_DEFAULT -> {
                LinkToObject(
                    ItemBlockObjectLinkBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_OBJECT_LINK_CARD -> {
                LinkToObjectCard(
                    binding = ItemBlockObjectLinkCardBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            HOLDER_OBJECT_LINK_ARCHIVE -> {
                LinkToObjectArchive(
                    ItemBlockObjectLinkArchiveBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_OBJECT_LINK_DELETED -> {
                LinkToObjectDelete(
                    ItemBlockObjectLinkDeleteBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_OBJECT_LINK_LOADING -> {
                LinkToObjectLoading(
                    binding = ItemBlockObjectLinkLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK -> {
                Bookmark(
                    ItemBlockBookmarkBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_BOOKMARK_PLACEHOLDER -> {
                BookmarkPlaceholder(
                    ItemBlockMediaPlaceholderBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_BOOKMARK_ERROR -> {
                BookmarkError(
                    ItemBlockBookmarkErrorBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_PICTURE -> {
                Picture(
                    ItemBlockPictureBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_PICTURE_PLACEHOLDER -> {
                PicturePlaceholder(
                    ItemBlockMediaPlaceholderBinding.inflate(inflater, parent, false)
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
                    ItemBlockDividerLineBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_DIVIDER_DOTS -> {
                DividerDots(
                    ItemBlockDividerDotsBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_HIGHLIGHT -> {
                Highlight(
                    binding = ItemBlockHighlightBinding.inflate(
                        inflater, parent, false
                    )
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
                    ItemBlockRelationPlaceholderBinding.inflate(inflater, parent, false)
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
                    ItemBlockRelationTagBinding.inflate(inflater, parent, false)
                ).setup(this)
            }
            HOLDER_RELATION_OBJECT -> {
                RelationViewHolder.Object(
                    ItemBlockRelationObjectBinding.inflate(inflater, parent, false)
                ).setup(this)
            }
            HOLDER_RELATION_FILE -> {
                RelationViewHolder.File(
                    ItemBlockRelationFileBinding.inflate(inflater, parent, false)
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
                    ItemBlockFeaturedRelationsBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_LATEX -> Latex(
                ItemBlockLatexBinding.inflate(inflater, parent, false)
            ).apply {
                //latexView.isLongClickable = true
                latexView.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClickListener(ListenerType.Latex(blocks[pos].id))
                        }
                        v.performClick()
                        true
                    } else {
                        false
                    }
                }
            }
            HOLDER_TOC -> {
                TableOfContents(
                    ItemBlockTocBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_UNSUPPORTED -> Unsupported(
                ItemBlockUnsupportedBinding.inflate(inflater, parent, false)
            )
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
                onDragAndDropTrigger(holder, it)
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
                when (holder) {
                    is RelationViewHolder -> {
                        val processor = EditorTouchProcessor(
                            fallback = { holder.itemView.onTouchEvent(it) },
                            onLongClick = {
                                val pos = holder.bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                                }
                            },
                            onDragAndDropTrigger = { onDragAndDropTrigger(holder, it) }
                        )
                        holder.itemView.setOnTouchListener { v, e -> processor.process(v, e) }
                    }
                    is Code -> {
                        holder.editorTouchProcessor.onLongClick = {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                            }
                        }
                        holder.editorTouchProcessor.onDragAndDropTrigger = {
                            onDragAndDropTrigger(holder, it)
                        }
                    }
                    is TableOfContents -> {
                        holder.editorTouchProcessor.onDragAndDropTrigger = {
                            onDragAndDropTrigger(holder, it)
                        }
                    }
                    else -> {
                        holder.itemView.setOnLongClickListener {
                            val pos = holder.bindingAdapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                            }
                            true
                        }
                    }
                }
            } else {
                holder.editorTouchProcessor.onLongClick = {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onClickListener(ListenerType.LongClick(target = blocks[pos].id))
                    }
                }
                holder.editorTouchProcessor.onDragAndDropTrigger = {
                    onDragAndDropTrigger(holder, it)
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
        if (isInDragAndDropMode) trySetDesiredAppearanceForDraggedItem(holder, position)
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
                    is LinkToObject -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is LinkToObjectCard -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is LinkToObjectArchive -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position]
                        )
                    }
                    is LinkToObjectDelete -> {
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
                    is Latex -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.Latex
                        )
                    }
                    is TableOfContents -> {
                        holder.processChangePayload(
                            payloads = payloads.typeOf(),
                            item = blocks[position] as BlockView.TableOfContents
                        )
                    }
                    else -> throw IllegalStateException("Unexpected view holder: $holder")
                }
                checkIfDecorationChanged(holder, payloads.typeOf(), position)
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
        if (isInDragAndDropMode) trySetDesiredAppearanceForDraggedItem(holder, position)
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
                        onFocusChanged = onFocusChanged,
                        onProfileIconClicked = onClickListener,
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
                    clicked = onClickListener,
                    lifecycle = lifecycle
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
            is LinkToObject -> {
                holder.bind(
                    item = blocks[position] as BlockView.LinkToObject.Default.Text,
                    clicked = onClickListener
                )
            }
            is LinkToObjectCard -> {
                holder.bind(
                    item = blocks[position] as BlockView.LinkToObject.Default.Card,
                    clicked = onClickListener
                )
            }
            is LinkToObjectArchive -> {
                holder.bind(
                    item = blocks[position] as BlockView.LinkToObject.Archived,
                    clicked = onClickListener
                )
            }
            is LinkToObjectDelete -> {
                holder.bind(
                    item = blocks[position] as BlockView.LinkToObject.Deleted,
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
            is Latex -> {
                holder.bind(item = blocks[position] as BlockView.Latex)
            }
            is TableOfContents -> {
                holder.bind(
                    item = blocks[position] as BlockView.TableOfContents,
                    clicked = onClickListener
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

        if (holder is DecoratableViewHolder) {
            val block = blocks[position]
            check(block is BlockView.Decoratable)
            holder.applyDecorations(decorations = block.decorations)
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

    fun updateWithDiffUtil(items: List<BlockView>) {
        if (BuildConfig.DEBUG) {
            Timber.d("----------Blocks dispatched to adapter---------------------")
        }
        val result = DiffUtil.calculateDiff(BlockViewDiffUtil(old = blocks, new = items))
        blocks = items
        result.dispatchUpdatesTo(this)
    }

    private fun checkIfDecorationChanged(
        holder: BlockViewHolder,
        payloads: List<Payload>,
        position: Int
    ) {
        if (holder is DecoratableViewHolder && payloads.any { p -> p.isDecorationChanged }) {
            val block = blocks[position]
            check(block is BlockView.Decoratable)
            holder.onDecorationsChanged(decorations = block.decorations)
        }
    }
}
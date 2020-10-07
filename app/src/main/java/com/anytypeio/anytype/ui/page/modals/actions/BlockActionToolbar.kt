package com.anytypeio.anytype.ui.page.modals.actions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.transition.*
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.common.toSpannable
import com.anytypeio.anytype.core_ui.extensions.addVerticalDivider
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.page.BlockDimensions
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.widgets.ActionItemType
import com.anytypeio.anytype.core_ui.widgets.BlockActionBarItem
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.action_toolbar.*
import timber.log.Timber
import kotlin.math.abs

abstract class BlockActionToolbar : Fragment() {

    companion object {
        const val ARG_BLOCK = "arg.block"
        const val ARG_BLOCK_DIMENSIONS = "arg.block.dimensions"

        const val ANIM_DURATION = 300L
        const val DEFAULT_MARGIN = 0
        const val INTERPOLATOR_OVERSHOOT_TENSION = 1.6f
    }

    abstract fun initUi(view: View, colorView: ImageView? = null, backgroundView: ImageView? = null)
    abstract fun getBlock(): BlockView
    abstract fun blockLayout(): Int

    private var actionClick: (ActionItemType) -> Unit = {}
    private var actionToolbarSize = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.action_toolbar, container, false)
        view.findViewById<FrameLayout>(R.id.block_container).apply {
            addView(inflater.inflate(blockLayout(), this, false))
        }
        setOnBackPressedCallback()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionClick = { actionType: ActionItemType ->
            (parentFragment as? OnFragmentInteractionListener)?.onBlockActionClicked(
                getBlock().id,
                actionType
            )
        }

        container.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onDismissBlockActionToolbar()
        }

        when (val block = getBlock()) {
            is BlockView.Title.Document -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Title.Profile -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Paragraph -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Header.One -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Header.Two -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Header.Three -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Highlight -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Code -> addButtons(view, ACTIONS.CODE)
            is BlockView.Text.Checkbox -> {
                if (block.isChecked) {
                    addButtons(view, ACTIONS.CHECKBOX_CHECKED)
                } else {
                    addButtons(view, ACTIONS.TEXT)
                }
            }
            is BlockView.Text.Bulleted -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Numbered -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Text.Toggle -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Media.File -> addButtons(view, ACTIONS.FILE)
            is BlockView.Upload.File -> addButtons(view, ACTIONS.FILE)
            is BlockView.MediaPlaceholder.File -> addButtons(view, ACTIONS.FILE)
            is BlockView.Error.File -> addButtons(view, ACTIONS.FILE)
            is BlockView.Media.Video -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Upload.Video -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.MediaPlaceholder.Video -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Error.Video -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Page -> addButtons(view, ACTIONS.PAGE)
            is BlockView.PageArchive -> addButtons(view, ACTIONS.PAGE_ARCHIVE)
            is BlockView.Divider -> addButtons(view, ACTIONS.DIVIDER)
            is BlockView.MediaPlaceholder.Bookmark -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Media.Bookmark -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Error.Bookmark -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Media.Picture -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.MediaPlaceholder.Picture -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Error.Picture -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Upload.Picture -> addButtons(view, ACTIONS.VIDEO_PICTURE)
        }
        if (BuildConfig.DEBUG) {
            log(getBlock())
        }
    }

    private fun addButtons(view: View, actions: List<ActionItemType>) =
        with(action_container) {
            var colorView: ImageView? = null
            var backgroundView: ImageView? = null
            actions.forEach { type ->
                when (type) {
                    ActionItemType.Divider -> {
                        val height = resources.getDimensionPixelSize(R.dimen.default_toolbar_action_item_divider_height)
                        actionToolbarSize += height
                        addVerticalDivider(
                            height = height,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    ActionItemType.DividerExtended -> {
                        val height = resources.getDimensionPixelSize(R.dimen.default_toolbar_action_item_divider_extended_height)
                        actionToolbarSize += height
                        addVerticalDivider(
                            height = height,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    else -> {
                        actionToolbarSize += resources.getDimensionPixelSize(R.dimen.default_toolbar_action_item_height)
                        addView(
                            createActionBarItem(
                                type = type,
                                context = requireContext(),
                                actionClick = actionClick
                            )
                        )
                    }
                }
            }
            initUi(
                view = view,
                backgroundView = backgroundView,
                colorView = colorView
            )
        }

    private fun setOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (parentFragment as? OnFragmentInteractionListener)?.onDismissBlockActionToolbar()
                }
            })
    }

    private fun setBlockTextColor(content: TextView, color: String) {
        content.setTextColor(
            ThemeColor.values().first { value ->
                value.title == color
            }.text
        )
    }

    private fun setBlockBackgroundColor(root: View, color: String? = null) {
        if (color != null) {
            root.setBackgroundColor(
                ThemeColor.values().first { value ->
                    value.title == color
                }.background
            )
        } else {
            root.background = null
        }
    }

    private fun setActionBarItemColor(item: ImageView, color: String) {
        val colorRes = ThemeColor.values().first { value ->
            value.title == color
        }.text
        if (colorRes != ThemeColor.DEFAULT.text) {
            item.setImageDrawable(requireContext().drawable(R.drawable.ic_action_background))
            item.setColorFilter(colorRes)
        }
    }

    private fun setActionBarItemBackgroundColor(item: ImageView, color: String) {
        val colorRes = ThemeColor.values().first { value ->
            value.title == color
        }.background
        if (colorRes != ThemeColor.DEFAULT.background) {
            item.setImageDrawable(requireContext().drawable(R.drawable.ic_action_background))
            item.setColorFilter(colorRes)
        }
    }

    fun processTextColor(
        textView: TextView, colorImage: ImageView?, color: String?,
        imageView: ImageView? = null
    ) =
        color?.let {
            setBlockTextColor(content = textView, color = it)
            colorImage?.let { imageView ->
                setActionBarItemColor(
                    item = imageView,
                    color = it
                )
            }
            imageView?.setColorFilter(
                ThemeColor.values().first { value ->
                    value.title == it
                }.text
            )
        }

    fun processBackgroundColor(root: View, bgImage: ImageView?, color: String?) {
        setBlockBackgroundColor(root = root, color = color)
        if (color != null) {
            bgImage?.let {
                setActionBarItemBackgroundColor(
                    item = it,
                    color = color
                )
            }
        }
    }

    fun setBlockText(content: TextInputWidget, text: String, markup: Markup, textColor: Int) =
        when (markup.marks.isEmpty()) {
            true -> content.setText(text)
            false -> setBlockSpannableText(content, markup, textColor)
        }

    private fun setBlockSpannableText(content: TextInputWidget, markup: Markup, textColor: Int) =
        when (markup.marks.any { it.type == Markup.Type.MENTION }) {
            true -> setSpannableWithMention(content, markup, textColor)
            false -> setSpannable(content, markup, textColor)
        }

    private fun setSpannable(content: TextInputWidget, markup: Markup, textColor: Int) {
        content.setText(markup.toSpannable(textColor = textColor), TextView.BufferType.SPANNABLE)
    }

    private fun setSpannableWithMention(content: TextInputWidget, markup: Markup, textColor: Int) {
        with(content) {
            val sizes = getMentionImageSizeAndPadding()
            setText(
                markup.toSpannable(
                    context = context,
                    mentionImageSize = sizes.first,
                    mentionImagePadding = sizes.second,
                    click = {},
                    onImageReady = { param -> refreshMentionSpan(content, param) },
                    textColor = textColor
                ),
                TextView.BufferType.SPANNABLE
            )
        }
    }

    private fun refreshMentionSpan(content: TextInputWidget, param: String) {
        content.text?.let { editable ->
            val spans = editable.getSpans(
                0,
                editable.length,
                MentionSpan::class.java
            )
            spans.forEach { span ->
                if (span.param == param) {
                    editable.setSpan(
                        span,
                        editable.getSpanStart(span),
                        editable.getSpanEnd(span),
                        Markup.MENTION_SPANNABLE_FLAG
                    )
                }
            }
        }
    }

    open fun getMentionImageSizeAndPadding(): Pair<Int, Int> =
        Pair(
            first = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_padding_default)
        )

    private fun createActionBarItem(
        type: ActionItemType,
        context: Context,
        actionClick: (ActionItemType) -> Unit
    ): BlockActionBarItem = BlockActionBarItem(context = context).apply {
        setTypeAndClick(
            itemType = type,
            clickListener = { actionClick(it) }
        )
    }

    private fun log(blockView: BlockView) {
        Timber.d("ActionBar, open block: $blockView")
    }

    open fun getBlockPaddingTop(): Int {
        return resources.getDimensionPixelOffset(R.dimen.action_toolbar_block_padding_top)
    }

    open fun getBlockPaddingBottom(): Int {
        return resources.getDimensionPixelOffset(R.dimen.action_toolbar_block_padding_bottom)
    }

    private fun getUpdatedBlockDimensions(): BlockDimensions {
        val blockPaddingTop = getBlockPaddingTop()
        val blockPaddingBottom = getBlockPaddingBottom()
        val dimensions: BlockDimensions = arguments?.getParcelable(ARG_BLOCK_DIMENSIONS)!!
        return dimensions.copy(
            top = dimensions.top - blockPaddingTop,
            bottom = dimensions.bottom + blockPaddingTop,
            height = dimensions.height + blockPaddingTop + blockPaddingBottom
        )
    }

    private fun createStartSet() : ConstraintSet = ConstraintSet().apply {
        clone(requireContext(), R.layout.action_toolbar)
        setScaleX(R.id.block_container, 1f)
        setScaleY(R.id.block_container, 1f)
        setVisibility(R.id.card, View.INVISIBLE)
        setScaleX(R.id.card, 0.3f)
        setScaleY(R.id.card, 0.3f)
    }

    private fun createEndSet(): ConstraintSet = ConstraintSet().apply {
        clone(requireContext(), R.layout.action_toolbar)
        setScaleX(R.id.block_container, 1f)
        setScaleY(R.id.block_container, 1f)
        setVisibility(R.id.card, View.VISIBLE)
        setScaleX(R.id.card, 1f)
        setScaleY(R.id.card, 1f)
    }

    private fun createTransitionSet() = TransitionSet().apply {
        addTransition(ChangeBounds())
        addTransition(ChangeTransform())
        addTransition(Fade(Visibility.MODE_IN))
        duration = ANIM_DURATION
        interpolator = OvershootInterpolator(INTERPOLATOR_OVERSHOOT_TENSION)
        ordering = TransitionSet.ORDERING_TOGETHER
    }

    fun setConstraints() {

        container.doOnLayout {
            val blockDimensions = getUpdatedBlockDimensions()
            val barMarginBottom =
                resources.getDimensionPixelOffset(R.dimen.action_toolbar_bar_margin_bottom)
            val barMarginTop =
                resources.getDimensionPixelOffset(R.dimen.action_toolbar_bar_margin_top)
            val screenDimensions = PopupExtensions.calculateRectInWindow(container)
            val anchorView = BlockActionToolbarHelper.getAnchorView(
                screenTop = screenDimensions.top,
                screenBottom = screenDimensions.bottom,
                blockTop = blockDimensions.top,
                blockBottom = blockDimensions.bottom,
                barHeight = actionToolbarSize,
                barMarginBottom = barMarginBottom,
                barMarginTop = barMarginTop,
                blockHeight = blockDimensions.height
            )
            val startSet = createStartSet()
            val endSet = createEndSet()
            val blockId = R.id.block_container
            val containerId = R.id.container
            val barId = R.id.card

            when (anchorView) {
                BlockActionToolbarHelper.AnchorView.ACTION_BAR -> {
                    with(startSet) {
                        if (blockDimensions.bottom < screenDimensions.bottom) {
                            val blockMargin = screenDimensions.bottom - blockDimensions.bottom
                            connect(blockId, BOTTOM, containerId, BOTTOM, blockMargin)
                        } else {
                            connect(blockId, BOTTOM, containerId, BOTTOM, DEFAULT_MARGIN)
                            val translationY =
                                (blockDimensions.bottom - screenDimensions.bottom).toFloat()
                            setTranslationY(blockId, translationY)
                        }
                        connect(barId, TOP, blockId, BOTTOM, barMarginTop)
                    }
                    with(endSet) {
                        connect(blockId, BOTTOM, barId, TOP, barMarginTop)
                        connect(barId, BOTTOM, containerId, BOTTOM, barMarginBottom)
                    }
                }
                BlockActionToolbarHelper.AnchorView.BLOCK_GRAVITY_TOP -> {
                    with (startSet) {
                        connect(blockId, TOP, containerId, TOP, DEFAULT_MARGIN)
                        val translationY =
                            (abs(blockDimensions.top) + screenDimensions.top).toFloat()
                        setTranslationY(blockId, -translationY)
                        connect(barId, TOP, blockId, BOTTOM, barMarginTop)
                    }
                    with (endSet) {
                        connect(blockId, TOP, containerId, TOP, barMarginTop)
                        connect(barId, TOP, blockId, BOTTOM, barMarginTop)
                    }
                }
                BlockActionToolbarHelper.AnchorView.BLOCK -> {
                    with (startSet) {
                        val blockMargin = blockDimensions.top - screenDimensions.top
                        connect(blockId, TOP, containerId, TOP, blockMargin)
                        connect(barId, TOP, blockId, BOTTOM, barMarginTop)
                    }
                    with(endSet) {
                        val blockMargin = blockDimensions.top - screenDimensions.top
                        connect(blockId, TOP, containerId, TOP, blockMargin)
                        connect(barId, TOP, blockId, BOTTOM, barMarginTop)
                    }
                }
            }

            startSet.applyTo(container)
            container.post {
                TransitionManager.beginDelayedTransition(container, createTransitionSet())
                endSet.applyTo(container)
            }
        }
    }

    object ACTIONS {

        val PAGE = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val PAGE_ARCHIVE = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete
        )

        val TEXT = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Style
        )

        val CHECKBOX_CHECKED = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Style
        )

        val VIDEO_PICTURE = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.Download,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val FILE = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.Download,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val BOOKMARK = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val CODE = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val DIVIDER = listOf(
            ActionItemType.AddBelow,
            ActionItemType.DividerExtended,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )
    }
}
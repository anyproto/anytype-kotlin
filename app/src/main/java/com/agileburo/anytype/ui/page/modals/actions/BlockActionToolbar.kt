package com.agileburo.anytype.ui.page.modals.actions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.transition.*
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.extensions.addVerticalDivider
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.features.page.BlockDimensions
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_ui.widgets.BlockActionBarItem
import com.agileburo.anytype.core_utils.ext.PopupExtensions
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.action_toolbar.*
import timber.log.Timber
import kotlin.math.abs

abstract class BlockActionToolbar : Fragment() {

    companion object {
        const val ARG_BLOCK = "arg.block"
        const val ARG_BLOCK_DIMENSIONS = "arg.block.dimensions"

        const val ANIM_DURATION = 300L
        const val DEFAULT_MARGIN = 0
        const val DIVIDER_HEIGHT = 1
        const val DIVIDER_BIG_HEIGHT = 8
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
            is BlockView.Paragraph -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Title.Document -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Title.Profile -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Header.One -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Header.Two -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Header.Three -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Highlight -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Code -> addButtons(view, ACTIONS.CODE)
            is BlockView.Checkbox -> {
                if (block.isChecked) {
                    addButtons(view, ACTIONS.CHECKBOX_CHECKED)
                } else {
                    addButtons(view, ACTIONS.TEXT)
                }
            }
            is BlockView.Bulleted -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Numbered -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Toggle -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Contact -> TODO()
            is BlockView.File.View -> addButtons(view, ACTIONS.FILE)
            is BlockView.File.Upload -> addButtons(view, ACTIONS.FILE)
            is BlockView.MediaPlaceholder.File -> addButtons(view, ACTIONS.FILE)
            is BlockView.File.Error -> addButtons(view, ACTIONS.FILE)
            is BlockView.Video.View -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Upload -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.MediaPlaceholder.Video -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Error -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Page -> addButtons(view, ACTIONS.PAGE)
            is BlockView.Divider -> addButtons(view, ACTIONS.DIVIDER)
            is BlockView.MediaPlaceholder.Bookmark -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Bookmark.View -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Bookmark.Error -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Picture.View -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.MediaPlaceholder.Picture -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Error -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Upload -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            BlockView.Footer -> TODO()
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
                        actionToolbarSize += DIVIDER_HEIGHT
                        addVerticalDivider(
                            height = DIVIDER_HEIGHT,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    ActionItemType.DividerExtended -> {
                        actionToolbarSize += DIVIDER_BIG_HEIGHT
                        addVerticalDivider(
                            height = DIVIDER_BIG_HEIGHT,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    ActionItemType.Background -> {
                        actionToolbarSize += resources.getDimensionPixelSize(R.dimen.default_toolbar_action_item_height)
                        val item = createActionBarItem(
                            type = type,
                            context = requireContext(),
                            actionClick = actionClick
                        )
                        addView(item)
                        backgroundView = item.findViewById(R.id.ivAction)
                    }
                    ActionItemType.Color -> {
                        actionToolbarSize += resources.getDimensionPixelSize(R.dimen.default_toolbar_action_item_height)
                        val item = createActionBarItem(
                            type = type,
                            context = requireContext(),
                            actionClick = actionClick
                        )
                        addView(item)
                        colorView = item.findViewById(R.id.ivAction)
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
                setActionBarItemColor(
                    item = it,
                    color = color
                )
            }
        }
    }

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

    private fun getUpdatedBlockDimensions(): BlockDimensions {
        val blockPaddingTop =
            resources.getDimensionPixelOffset(R.dimen.action_toolbar_block_padding_top)
        val blockPaddingBottom =
            resources.getDimensionPixelOffset(R.dimen.action_toolbar_block_padding_bottom)
        val dimensions: BlockDimensions = arguments?.getParcelable(ARG_BLOCK_DIMENSIONS)!!
        return dimensions.copy(
            top = dimensions.top - blockPaddingTop,
            bottom = dimensions.bottom + blockPaddingTop,
            height = dimensions.height + blockPaddingTop + blockPaddingBottom
        )
    }

    private fun createStartSet() : ConstraintSet = ConstraintSet().apply {
        clone(requireContext(), R.layout.action_toolbar)
        setScaleX(R.id.block_container, 0.9f)
        setScaleY(R.id.block_container, 0.9f)
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
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.Rename,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Color,
            ActionItemType.Divider,
            ActionItemType.Background
        )

        val TEXT = listOf(
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Style,
            ActionItemType.Divider,
            ActionItemType.Color,
            ActionItemType.Divider,
            ActionItemType.Background
        )

        val CHECKBOX_CHECKED = listOf(
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Style,
            ActionItemType.Divider,
            ActionItemType.Background
        )

        val VIDEO_PICTURE = listOf(
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.Download,
            ActionItemType.Divider,
            ActionItemType.Replace,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.Divider,
            ActionItemType.AddCaption
        )

        val FILE = listOf(
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.Download,
            ActionItemType.Divider,
            ActionItemType.Replace,
            ActionItemType.Divider,
            ActionItemType.Rename,
            ActionItemType.Divider,
            ActionItemType.MoveTo,
            ActionItemType.DividerExtended,
            ActionItemType.Color,
            ActionItemType.Divider,
            ActionItemType.Background
        )

        val BOOKMARK = listOf(
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val CODE = listOf(
            ActionItemType.TurnInto,
            ActionItemType.Divider,
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )

        val DIVIDER = listOf(
            ActionItemType.Delete,
            ActionItemType.Divider,
            ActionItemType.Duplicate,
            ActionItemType.Divider,
            ActionItemType.MoveTo
        )
    }
}
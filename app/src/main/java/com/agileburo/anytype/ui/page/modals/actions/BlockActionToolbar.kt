package com.agileburo.anytype.ui.page.modals.actions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.extensions.addVerticalDivider
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_ui.widgets.BlockActionBarItem
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.action_toolbar.*
import timber.log.Timber

abstract class BlockActionToolbar : Fragment() {

    companion object {
        const val ARG_BLOCK = "arg.block"
    }

    abstract fun initUi(view: View, colorView: ImageView? = null, backgroundView: ImageView? = null)
    abstract fun getBlock(): BlockView
    abstract fun blockLayout(): Int

    private var actionClick: (ActionItemType) -> Unit = {}

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
            is BlockView.Title -> addButtons(view, ACTIONS.TEXT)
            is BlockView.ProfileTitle -> addButtons(view, ACTIONS.TEXT)
            is BlockView.HeaderOne -> addButtons(view, ACTIONS.TEXT)
            is BlockView.HeaderTwo -> addButtons(view, ACTIONS.TEXT)
            is BlockView.HeaderThree -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Highlight -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Code -> addButtons(view, ACTIONS.CODE)
            is BlockView.Checkbox -> {
                if (block.isChecked) {
                    addButtons(view, ACTIONS.CHECKBOX_CHECKED)
                } else {
                    addButtons(view, ACTIONS.TEXT)
                }
            }
            is BlockView.Task -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Bulleted -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Numbered -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Toggle -> addButtons(view, ACTIONS.TEXT)
            is BlockView.Contact -> TODO()
            is BlockView.File.View -> addButtons(view, ACTIONS.FILE)
            is BlockView.File.Upload -> addButtons(view, ACTIONS.FILE)
            is BlockView.File.Placeholder -> addButtons(view, ACTIONS.FILE)
            is BlockView.File.Error -> addButtons(view, ACTIONS.FILE)
            is BlockView.Video.View -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Upload -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Placeholder -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Error -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Page -> addButtons(view, ACTIONS.PAGE)
            is BlockView.Divider -> addButtons(view, ACTIONS.DIVIDER)
            is BlockView.Bookmark.Placeholder -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Bookmark.View -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Bookmark.Error -> addButtons(view, ACTIONS.BOOKMARK)
            is BlockView.Picture.View -> addButtons(view, ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Placeholder -> addButtons(view, ACTIONS.VIDEO_PICTURE)
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
                        addVerticalDivider(
                            height = 1,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    ActionItemType.DividerExtended -> {
                        addVerticalDivider(
                            height = 8,
                            alpha = 1.0f,
                            color = context.color(R.color.light_grayish)
                        )
                    }
                    ActionItemType.Background -> {
                        val item = createActionBarItem(
                            type = type,
                            context = requireContext(),
                            actionClick = actionClick
                        )
                        addView(item)
                        backgroundView = item.findViewById(R.id.ivAction)
                    }
                    ActionItemType.Color -> {
                        val item = createActionBarItem(
                            type = type,
                            context = requireContext(),
                            actionClick = actionClick
                        )
                        addView(item)
                        colorView = item.findViewById(R.id.ivAction)
                    }
                    else -> {
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
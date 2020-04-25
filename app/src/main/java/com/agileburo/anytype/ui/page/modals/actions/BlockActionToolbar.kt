package com.agileburo.anytype.ui.page.modals.actions

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.extensions.addVerticalDivider
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_ui.widgets.BlockActionBarItem
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.action_toolbar.*

abstract class BlockActionToolbar : Fragment() {

    companion object {
        const val ARG_BLOCK = "arg.block"
    }

    abstract fun initUi(view: View)
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
        initUi(view)

        container.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onDismissBlockActionToolbar()
        }

        when (getBlock()) {
            is BlockView.Paragraph -> addButtons(ACTIONS.TEXT)
            is BlockView.Title -> addButtons(ACTIONS.TEXT)
            is BlockView.HeaderOne -> addButtons(ACTIONS.TEXT)
            is BlockView.HeaderTwo -> addButtons(ACTIONS.TEXT)
            is BlockView.HeaderThree -> addButtons(ACTIONS.TEXT)
            is BlockView.Highlight -> addButtons(ACTIONS.TEXT)
            is BlockView.Code -> addButtons(ACTIONS.CODE)
            is BlockView.Checkbox -> addButtons(ACTIONS.TEXT)
            is BlockView.Task -> addButtons(ACTIONS.TEXT)
            is BlockView.Bulleted -> addButtons(ACTIONS.TEXT)
            is BlockView.Numbered -> addButtons(ACTIONS.TEXT)
            is BlockView.Toggle -> addButtons(ACTIONS.TEXT)
            is BlockView.Contact -> TODO()
            is BlockView.File.View -> addButtons(ACTIONS.FILE)
            is BlockView.File.Upload -> addButtons(ACTIONS.FILE)
            is BlockView.File.Placeholder -> addButtons(ACTIONS.FILE)
            is BlockView.File.Error -> addButtons(ACTIONS.FILE)
            is BlockView.Video.View -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Upload -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Placeholder -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Video.Error -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Page -> addButtons(ACTIONS.PAGE)
            is BlockView.Divider -> addButtons(ACTIONS.DIVIDER)
            is BlockView.Bookmark.Placeholder -> addButtons(ACTIONS.BOOKMARK)
            is BlockView.Bookmark.View -> addButtons(ACTIONS.BOOKMARK)
            is BlockView.Bookmark.Error -> addButtons(ACTIONS.BOOKMARK)
            is BlockView.Picture.View -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Placeholder -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Error -> addButtons(ACTIONS.VIDEO_PICTURE)
            is BlockView.Picture.Upload -> addButtons(ACTIONS.VIDEO_PICTURE)
            BlockView.Footer -> TODO()
        }
    }

    private fun addButtons(actions: List<ActionItemType>) =
        with(action_container) {
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
        }

    fun setBlockTextColor(content: TextView, color: String) {
        content.setTextColor(
            ThemeColor.values().first { value ->
                value.title == color
            }.text
        )
    }

    private fun createActionBarItem(
        type: ActionItemType,
        context: Context,
        actionClick: (ActionItemType) -> Unit
    ) = BlockActionBarItem(context = context).apply {
        setTypeAndClick(
            itemType = type,
            clickListener = { actionClick(it) }
        )
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
            ActionItemType.AddCaption,
            ActionItemType.DividerExtended,
            ActionItemType.Background
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

sealed class ActionType {

    @Parcelize
    data class Text(val id: String, val text: String) : ActionType(), Parcelable

    @Parcelize
    data class MediaImage(val id: String, val url: String) : ActionType(), Parcelable
}

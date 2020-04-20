package com.agileburo.anytype.ui.page.modals

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.addVerticalDivider
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.BlockActionBarItem
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.action_toolbar.*

class BlockActionToolbar : Fragment(R.layout.action_toolbar) {

    companion object {

        val ARG_BLOCK = "arg.block"
        fun newInstance(block: BlockView): BlockActionToolbar =
            BlockActionToolbar().apply {
                arguments = bundleOf(ARG_BLOCK to block)
            }
    }

    private var block: BlockView? = null

    private var actionClick: (ActionItemType) -> Unit = {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        block = arguments?.getParcelable<BlockView.Paragraph>(ARG_BLOCK)
        actionClick = { actionType: ActionItemType ->
            (parentFragment as? OnFragmentInteractionListener)?.onBlockActionClicked(
                block?.id.orEmpty(),
                actionType
            )
        }

        container.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onDismissBlockActionToolbar()
        }

        text.movementMethod = ScrollingMovementMethod()
        text.text = (block as? BlockView.Paragraph)?.text

        when (block) {
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

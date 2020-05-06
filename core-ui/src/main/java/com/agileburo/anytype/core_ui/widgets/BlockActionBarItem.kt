package com.agileburo.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.drawable
import kotlinx.android.synthetic.main.action_item.view.*

sealed class ActionItemType {
    object TurnInto : ActionItemType()
    object Delete : ActionItemType()
    object Duplicate : ActionItemType()
    object Rename : ActionItemType()
    object MoveTo : ActionItemType()
    object Color : ActionItemType()
    object Background : ActionItemType()
    object Style : ActionItemType()
    object Download : ActionItemType()
    object Replace : ActionItemType()
    object AddCaption : ActionItemType()
    object Divider : ActionItemType()
    object DividerExtended : ActionItemType()
}

class BlockActionBarItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var itemType: ActionItemType = ActionItemType.Divider
    private var click: (ActionItemType) -> Unit = {}

    init {
        LayoutInflater.from(context).inflate(R.layout.action_item, this)
    }

    private fun updateView() {
        when (itemType) {
            ActionItemType.TurnInto -> updateContent(
                R.string.action_bar_turn_into,
                R.drawable.ic_action_turn_into
            )
            ActionItemType.Delete -> updateContent(
                R.string.action_bar_delete,
                R.drawable.ic_action_delete
            )
            ActionItemType.Duplicate -> updateContent(
                R.string.action_bar_duplicate,
                R.drawable.ic_action_duplicate
            )
            ActionItemType.Rename -> updateContent(
                R.string.action_bar_rename,
                R.drawable.ic_action_rename
            )
            ActionItemType.MoveTo -> updateContent(
                R.string.action_bar_move_to,
                R.drawable.ic_action_move_to
            )
            ActionItemType.Color -> updateContent(
                R.string.action_bar_color,
                R.drawable.ic_action_color
            )
            ActionItemType.Background -> updateContent(
                R.string.action_bar_background,
                R.drawable.ic_ellipse
            )
            ActionItemType.Style -> updateContent(
                R.string.action_bar_style,
                R.drawable.ic_action_style
            )
            ActionItemType.Download -> updateContent(
                R.string.action_bar_download,
                R.drawable.ic_action_download
            )
            ActionItemType.Replace -> updateContent(
                R.string.action_bar_replace,
                R.drawable.ic_action_replace
            )
            ActionItemType.AddCaption -> updateContent(
                R.string.action_bar_add_caption,
                R.drawable.ic_action_add_caption
            )
            else -> throw RuntimeException("Unknown action item type:$itemType")
        }
    }

    private fun updateContent(text: Int, img: Int) {
        tvAction.text = resources.getText(text)
        ivAction.setImageDrawable(context.drawable(img))
    }

    fun setTypeAndClick(itemType: ActionItemType, clickListener: (ActionItemType) -> Unit) {
        this.click = clickListener
        this.itemType = itemType
        setOnClickListener { click.invoke(itemType) }
        updateView()
    }
}
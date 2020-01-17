package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.Action
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionAdapter
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DELETE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_DUPLICATE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_MENTION
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_REDO
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_SHARE
import com.agileburo.anytype.core_ui.widgets.toolbar.ActionToolbarWidget.ActionConfig.ACTION_UNDO
import kotlinx.android.synthetic.main.item_toolbar_action.view.*
import kotlinx.android.synthetic.main.item_toolbar_action.view.title
import kotlinx.android.synthetic.main.widget_action_toolbar.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * This toolbar widget provides user with different types of actions applicable to blocks.
 * These actions are rendered as scrollable lists.
 * @see Action
 * @see ActionAdapter
 */
class ActionToolbarWidget : LinearLayout {

    private val channel = Channel<Action>()

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    private fun inflate() {
        // TODO remove redundant linear layout
        LayoutInflater.from(context).inflate(R.layout.widget_action_toolbar, this)
        setupAdapter()
    }

    private fun setupAdapter() {
        actionRecycler.apply {

            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_action_item_spacing)
                        .toInt(),
                    firstItemSpacingStart = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_action_item_spacing_first)
                        .toInt(),
                    lastItemSpacingEnd = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_action_item_spacing_last)
                        .toInt()
                )
            )

            adapter = ActionAdapter(
                actions = listOf(
                    Action(ACTION_DELETE),
                    Action(ACTION_DUPLICATE),
                    Action(ACTION_MENTION),
                    Action(ACTION_SHARE),
                    Action(ACTION_UNDO),
                    Action(ACTION_REDO)
                ),
                onActionClicked = channel::sendBlocking
            )
        }
    }

    fun actionClicks(): Flow<Action> = channel.consumeAsFlow()

    fun show() {
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        visible()
    }

    fun hide() {
        layoutParams = LayoutParams(MATCH_PARENT, 0)
        invisible()
    }


    /**
     * Adapter for rendering list of actions
     * @property actions immutable list of actions
     * @property onActionClicked callback event for click handling
     * @see Action
     */
    class ActionAdapter(
        private val actions: List<Action>,
        private val onActionClicked: (Action) -> Unit
    ) : RecyclerView.Adapter<ActionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ) = ViewHolder(
            view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_toolbar_action, parent, false)
        )

        override fun getItemCount(): Int = actions.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(
                action = actions[position],
                onActionClicked = onActionClicked
            )
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val pic = itemView.pic
            private val title = itemView.title

            fun bind(
                action: Action,
                onActionClicked: (Action) -> Unit
            ) {
                itemView.setOnClickListener { onActionClicked(action) }

                when (action.type) {
                    ACTION_DELETE -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_delete)
                        title.setText(R.string.toolbar_action_delete)
                    }
                    ACTION_DUPLICATE -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_duplicate)
                        title.setText(R.string.toolbar_action_duplicate)
                    }
                    ACTION_MENTION -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_mention)
                        title.setText(R.string.toolbar_action_mention)
                    }
                    ACTION_SHARE -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_share)
                        title.setText(R.string.toolbar_action_share)
                    }
                    ACTION_UNDO -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_undo)
                        title.setText(R.string.toolbar_action_undo)
                    }
                    ACTION_REDO -> {
                        pic.setImageResource(R.drawable.ic_toolbar_action_redo)
                        title.setText(R.string.toolbar_action_redo)
                    }
                }
            }
        }
    }

    /**
     * Represents an UI-action applicable to a block.
     * @property type concrete action
     * @see ActionConfig for different type of actions applicable to blocks.
     */
    data class Action(val type: Int)

    /**
     * Constants for different types of actions.
     * @see Action
     */
    object ActionConfig {
        const val ACTION_DELETE = 0
        const val ACTION_DUPLICATE = 1
        const val ACTION_MENTION = 2
        const val ACTION_SHARE = 3
        const val ACTION_UNDO = 4
        const val ACTION_REDO = 5
    }
}
package com.agileburo.anytype.core_ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.common.ViewType
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_title.view.*

class GoalAdapter(
    private val views: List<GoalView> = GoalMock.provideGoals()
) : RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            GOAL_TYPE ->
                ViewHolder.GoalHolder(
                    view = inflater.inflate(
                        R.layout.item_block_checkbox,
                        parent,
                        false
                    )
                )
            HEADER_TYPE ->
                ViewHolder.HeaderHolder(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    )
                )
            TITLE_TYPE ->
                ViewHolder.TitleHolder(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                )
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount() = views.size
    override fun getItemViewType(position: Int) = views[position].getViewType()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.GoalHolder -> holder.bind(
                item = views[position] as GoalView.Goal
            )
            is ViewHolder.TitleHolder -> holder.bind(
                item = views[position] as GoalView.Title
            )
            is ViewHolder.HeaderHolder -> holder.bind(
                item = views[position] as GoalView.Header
            )
        }
    }

    sealed class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        class GoalHolder(view: View) : ViewHolder(view) {

            private val checkbox = itemView.checkboxIcon
            private val content = itemView.checkboxContent

            fun bind(item: GoalView.Goal) {
                checkbox.isSelected = item.checked
                content.setText(item.text)

                itemView.setOnClickListener { checkbox.isSelected = !checkbox.isSelected }
            }

        }

        class HeaderHolder(view: View) : ViewHolder(view) {

            private val header = itemView.headerThree

            fun bind(item: GoalView.Header) {
                header.setText(item.text)
            }

        }

        class TitleHolder(view: View) : ViewHolder(view) {

            private val title = itemView.title
            private val logo = itemView.emojiIcon

            fun bind(item: GoalView.Title) {
                title.setText(item.title)
                logo.text = "⛳"

            }

        }
    }

    companion object {
        const val TITLE_TYPE = 0
        const val HEADER_TYPE = 1
        const val GOAL_TYPE = 2
    }
}

sealed class GoalView : ViewType {
    data class Goal(val text: String, val checked: Boolean = false) : GoalView() {
        override fun getViewType() = GoalAdapter.GOAL_TYPE
    }

    data class Header(val text: String) : GoalView() {
        override fun getViewType() = GoalAdapter.HEADER_TYPE
    }

    data class Title(val title: String) : GoalView() {
        override fun getViewType() = GoalAdapter.TITLE_TYPE
    }
}

object GoalMock {

    fun provideGoals(): List<GoalView> = listOf(
        GoalView.Title("Goals"),
        GoalView.Header("Today"),
        GoalView.Goal("Do yoga"),
        GoalView.Goal("Read producthunt"),
        GoalView.Goal("Do french homework"),
        GoalView.Goal("Buy staff for dinner: meat, vegetables, water & chilly souce"),
        GoalView.Goal("Call Mary"),
        GoalView.Goal("Pay for rent"),
        GoalView.Header("Week"),
        GoalView.Goal("Finish «Atlas Shrugged» book"),
        GoalView.Goal("Visit Grandpa"),
        GoalView.Goal("🎮" + " Play new StarWars PS4 game"),
        GoalView.Goal("Ride a bicycle"),
        GoalView.Header("Month"),
        GoalView.Goal("Use public transport 10 times in a row to save money for vacation"),
        GoalView.Header("Year"),
        GoalView.Goal("Read 50 books"),
        GoalView.Goal("Learn French to upper intermediate"),
        GoalView.Goal("Get 10 000 free cash on bank account")
    )

}
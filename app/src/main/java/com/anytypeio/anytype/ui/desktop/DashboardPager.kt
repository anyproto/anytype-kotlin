package com.anytypeio.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ui.EqualSpacingItemDecoration
import kotlinx.android.synthetic.main.item_dashboard_inbox.view.*
import kotlinx.android.synthetic.main.item_dashboard_page.view.*
import kotlinx.android.synthetic.main.item_dashboard_page_archived.view.*
import kotlinx.android.synthetic.main.item_dashboard_recent.view.*
import kotlinx.android.synthetic.main.item_dashboard_sets.view.*

class DashboardPager(
    private val defaultAdapter: DashboardAdapter,
    private val recentAdapter: DashboardAdapter,
    private val inboxAdapter: DashboardAdapter,
    private val setsAdapter: DashboardAdapter,
    private val archiveAdapter: DashboardAdapter,
    private val dndBehavior: DashboardDragAndDropBehavior
): RecyclerView.Adapter<DashboardPager.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = when(viewType) {
        R.layout.item_dashboard_page -> {
            ViewHolder.Default(parent).apply {
                itemView.rvDashboard.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    val decoration = EqualSpacingItemDecoration(
                        topSpacing = spacing,
                        leftSpacing = spacing,
                        rightSpacing = spacing,
                        bottomSpacing = 0,
                        displayMode = EqualSpacingItemDecoration.GRID,
                        ignoreGridEdgesTop = true
                    )
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(decoration)
                    setHasFixedSize(true)
                    adapter = defaultAdapter
                    ItemTouchHelper(dndBehavior).attachToRecyclerView(this)
                }
            }
        }
        R.layout.item_dashboard_recent -> {
            ViewHolder.Recent(parent).apply {
                itemView.rvDashboardRecent.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    val decoration = EqualSpacingItemDecoration(
                        topSpacing = spacing,
                        leftSpacing = spacing,
                        rightSpacing = spacing,
                        bottomSpacing = 0,
                        displayMode = EqualSpacingItemDecoration.GRID,
                        ignoreGridEdgesTop = true
                    )
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(decoration)
                    setHasFixedSize(true)
                    adapter = recentAdapter
                }
            }
        }
        R.layout.item_dashboard_inbox -> {
            ViewHolder.Inbox(parent).apply {
                itemView.rvDashboardInbox.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    val decoration = EqualSpacingItemDecoration(
                        topSpacing = spacing,
                        leftSpacing = spacing,
                        rightSpacing = spacing,
                        bottomSpacing = 0,
                        displayMode = EqualSpacingItemDecoration.GRID,
                        ignoreGridEdgesTop = true
                    )
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(decoration)
                    setHasFixedSize(true)
                    adapter = inboxAdapter
                }
            }
        }
        R.layout.item_dashboard_sets -> {
            ViewHolder.Sets(parent).apply {
                itemView.rvDashboardSets.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    val decoration = EqualSpacingItemDecoration(
                        topSpacing = spacing,
                        leftSpacing = spacing,
                        rightSpacing = spacing,
                        bottomSpacing = 0,
                        displayMode = EqualSpacingItemDecoration.GRID,
                        ignoreGridEdgesTop = true
                    )
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(decoration)
                    setHasFixedSize(true)
                    adapter = setsAdapter
                }
            }
        }
        R.layout.item_dashboard_page_archived -> {
            ViewHolder.Archived(parent).apply {
                itemView.rvDashboardArchived.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    val decoration = EqualSpacingItemDecoration(
                        topSpacing = spacing,
                        leftSpacing = spacing,
                        rightSpacing = spacing,
                        bottomSpacing = 0,
                        displayMode = EqualSpacingItemDecoration.GRID,
                        ignoreGridEdgesTop = true
                    )
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(decoration)
                    setHasFixedSize(true)
                    adapter = archiveAdapter
                }
            }
        }
         else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount(): Int = PAGE_COUNT

    override fun getItemViewType(position: Int) = when(position) {
        INDEX_FAVOURITES -> R.layout.item_dashboard_page
        INDEX_RECENT -> R.layout.item_dashboard_recent
        INDEX_INBOX -> R.layout.item_dashboard_inbox
        INDEX_SETS-> R.layout.item_dashboard_sets
        INDEX_BIN -> R.layout.item_dashboard_page_archived
        else -> throw IllegalStateException("Unexpected position: $position")
    }

    companion object {
        const val COLUMN_COUNT = 2
        const val PAGE_COUNT = 5
        const val INDEX_FAVOURITES = 0
        const val INDEX_RECENT = 1
        const val INDEX_INBOX = 2
        const val INDEX_SETS = 3
        const val INDEX_BIN = 4
    }

    sealed class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        class Default(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_page,
                parent,
                false
            )
        )
        class Recent(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_recent,
                parent,
                false
            )
        )
        class Inbox(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_inbox,
                parent,
                false
            )
        )
        class Sets(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_sets,
                parent,
                false
            )
        )
        class Archived(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_page_archived,
                parent,
                false
            )
        )
    }
}
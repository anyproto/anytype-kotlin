package com.agileburo.anytype.ui.database.kanban.viewholder

import android.content.ClipData
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.presentation.databaseview.models.KanbanRowView
import com.agileburo.anytype.presentation.databaseview.models.TagView
import com.agileburo.anytype.ui.database.tags.TagAdapter
import com.bumptech.glide.Glide
import com.woxthebox.draglistview.DragItemAdapter
import kotlinx.android.synthetic.main.item_kanban_bookmark.view.*
import kotlinx.android.synthetic.main.item_kanban_demo.view.*
import kotlinx.android.synthetic.main.item_kanban_file.view.*
import kotlinx.android.synthetic.main.item_kanban_page.view.*
import kotlinx.android.synthetic.main.item_kanban_person.view.*
import kotlinx.android.synthetic.main.item_kanban_task.view.*

sealed class KanbanRowViewHolder(view: View, dragResId: Int) :
    DragItemAdapter.ViewHolder(view, dragResId, true) {

    class KanbanDemoViewHolder(view: View, dragResId: Int) : KanbanRowViewHolder(view, dragResId) {

        private val title = itemView.title
        private val avatar = itemView.avatar
        private val recyclerTags = itemView.recyclerTags

        fun bind(model: KanbanRowView.KanbanDemoView) {
            title.text = model.title
            avatar.bind(model.assign)
            initRecyclerView(recyclerTags, model.tags)
        }

        private fun initRecyclerView(rv: RecyclerView, tags: List<TagView>) = with(rv) {
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = 2,
                    spacingBottom = 2,
                    spacingEnd = 2,
                    spacingTop = 2,
                    firstItemSpacingStart = 0
                )
            )
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = TagAdapter().also {
                it.addData(tags)
            }
        }
    }

    class KanbanFileViewHolder(view: View, dragResId: Int) : KanbanRowViewHolder(view, dragResId) {

        private val category = itemView.fileCategory
        private val title = itemView.fileTitle
        private val icon = itemView.fileIcon

        fun bind(model: KanbanRowView.KanbanFileView) {
            category.text = model.category
            title.text = model.title
            if (model.icon.isNotEmpty()) Glide.with(itemView).load(model.icon).into(icon)
        }
    }

    class KanbanPageViewHolder(view: View, dragResId: Int) : KanbanRowViewHolder(view, dragResId) {

        private val category = itemView.pageCategory
        private val title = itemView.pageTitle
        private val icon = itemView.pageIcon

        fun bind(model: KanbanRowView.KanbanPageView) {
            category.text = model.category
            title.text = model.title
            if (model.icon.isNotEmpty()) Glide.with(itemView).load(model.icon).circleCrop().into(
                icon
            )
        }
    }

    class KanbanPeopleViewHolder(view: View, dragResId: Int) :
        KanbanRowViewHolder(view, dragResId) {

        private val category = itemView.peopleCategory
        private val title = itemView.peopleTitle
        private val icon = itemView.peopleIcon

        fun bind(model: KanbanRowView.KanbanPeopleView) {
            category.text = model.category
            title.text = model.name
            if (model.icon.isNotEmpty()) Glide.with(itemView).load(model.icon).circleCrop().into(
                icon
            )
        }
    }

    class KanbanTaskViewHolder(view: View, dragResId: Int) : KanbanRowViewHolder(view, dragResId) {

        private val category = itemView.taskCategory
        private val task = itemView.task

        fun bind(model: KanbanRowView.KanbanTaskView) {
            category.text = model.category
            task.apply {
                text = model.title
                isChecked = model.checked
            }

            itemView.setOnLongClickListener { view ->
                val clip = ClipData.newPlainText("", "")
                val shadow = View.DragShadowBuilder(view)
                view.startDragAndDrop(clip, shadow, view, 0)
                true
            }

        }
    }

    class KanbanBookmarkViewHolder(view: View, dragResId: Int) :
        KanbanRowViewHolder(view, dragResId) {

        private val title = itemView.bookmarkTitle
        private val description = itemView.bookmarkDescription
        private val url = itemView.bookmarkUrl
        private val image = itemView.bookmarkImage
        private val logo = itemView.logo

        fun bind(model: KanbanRowView.KanbanBookmarkView) {
            title.text = model.title
            description.text = model.subtitle
            url.text = model.url
            if (model.image.isNotEmpty()) Glide.with(itemView).load(model.image).centerCrop().into(
                image
            )
            if (model.logo.isNotEmpty()) Glide.with(itemView).load(model.logo).into(logo)
        }
    }

    class KanbanAddNewItemViewHolder(view: View, dragResId: Int) :
        KanbanRowViewHolder(view, dragResId)
}
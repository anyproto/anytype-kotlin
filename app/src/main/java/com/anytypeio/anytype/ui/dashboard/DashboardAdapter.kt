package com.anytypeio.anytype.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.android.synthetic.main.item_dashboard_card_default.view.*
import kotlinx.android.synthetic.main.item_dashboard_card_default.view.shimmer
import kotlinx.android.synthetic.main.item_desktop_archive.view.*
import kotlinx.android.synthetic.main.item_desktop_set_without_icon.view.*
import timber.log.Timber

class DashboardAdapter(
    private var data: List<DashboardView>,
    private val onDocumentClicked: (Id, Boolean) -> Unit,
    private val onArchiveClicked: (Id) -> Unit,
    private val onObjectSetClicked: (Id) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(), SupportDragAndDropBehavior {

    companion object {
        const val VIEW_TYPE_DOCUMENT = 0
        const val VIEW_TYPE_DOCUMENT_WITHOUT_ICON = 1
        const val VIEW_TYPE_DOCUMENT_TASK = 2
        const val VIEW_TYPE_ARCHIVE = 3
        const val VIEW_TYPE_SET = 4
        const val VIEW_TYPE_SET_WITHOUT_ICON = 5
        const val VIEW_TYPE_DOCUMENT_NOTE = 6

        const val UNEXPECTED_TYPE_ERROR_MESSAGE = "Unexpected type"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DOCUMENT -> {
                ViewHolder.DocumentHolder(
                    inflater.inflate(R.layout.item_dashboard_card_default, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            VIEW_TYPE_DOCUMENT_WITHOUT_ICON -> {
                ViewHolder.DocumentWithoutIconViewHolder(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            VIEW_TYPE_DOCUMENT_TASK -> {
                ViewHolder.DocumentTaskViewHolder(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            VIEW_TYPE_ARCHIVE -> {
                ViewHolder.ArchiveHolder(
                    inflater.inflate(R.layout.item_desktop_archive, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Archive)
                            onArchiveClicked(item.target)
                        }
                    }
                }
            }
            VIEW_TYPE_SET -> {
                ViewHolder.ObjectSetHolder(
                    inflater.inflate(R.layout.item_dashboard_card_default, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.ObjectSet)
                            onObjectSetClicked(item.target)
                        }
                    }
                    itemView.typeTitle.setText(R.string.set)
                }
            }
            VIEW_TYPE_SET_WITHOUT_ICON -> {
                ViewHolder.ObjectSetWithoutIconHolder(
                    inflater.inflate(R.layout.item_desktop_set_without_icon, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.ObjectSet)
                            onObjectSetClicked(item.target)
                        }
                    }
                    itemView.tvSetTypeName.setText(R.string.set)
                }
            }
            VIEW_TYPE_DOCUMENT_NOTE -> {
                ViewHolder.DocumentNoteViewHolder(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = data[position]) {
            is DashboardView.Document -> when {
                item.layout == ObjectType.Layout.TODO -> VIEW_TYPE_DOCUMENT_TASK
                item.layout == ObjectType.Layout.NOTE -> VIEW_TYPE_DOCUMENT_NOTE
                item.hasIcon || item.layout == ObjectType.Layout.PROFILE -> VIEW_TYPE_DOCUMENT
                else -> VIEW_TYPE_DOCUMENT_WITHOUT_ICON
            }
            is DashboardView.Archive -> VIEW_TYPE_ARCHIVE
            is DashboardView.ObjectSet -> when (item.icon) {
                ObjectIcon.None -> VIEW_TYPE_SET_WITHOUT_ICON
                else -> VIEW_TYPE_SET
            }
            else -> throw IllegalStateException("$UNEXPECTED_TYPE_ERROR_MESSAGE:\n$item")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.DocumentHolder -> {
                val item = data[position] as DashboardView.Document
                with(holder) {
                    bindTitle(item.title)
                    bindSubtitle(item.typeName)
                    bindIcon(item.icon)
                    bindLoading(item.isLoading)
                }
            }
            is ViewHolder.DocumentWithoutIconViewHolder -> {
                val item = data[position] as DashboardView.Document
                holder.bindTitle(item.title)
                holder.bindSubtitle(item.typeName)
                holder.bindLoading(item.isLoading)
            }
            is ViewHolder.DocumentTaskViewHolder -> {
                val item = data[position] as DashboardView.Document
                holder.bindTitle(item.title)
                holder.bindSubtitle(item.typeName)
                holder.bindLoading(item.isLoading)
                holder.bindDone(item.done)
            }
            is ViewHolder.ObjectSetHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.ObjectSet
                    bindTitle(item.title)
                    bindIcon(item.icon)
                    bindLoading(item.isLoading)
                }
            }
            is ViewHolder.ObjectSetWithoutIconHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.ObjectSet
                    bindTitle(item.title)
                    bindLoading(item.isLoading)
                }
            }
            is ViewHolder.ArchiveHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.Archive
                    bindTitle(item.title)
                }
            }
            is ViewHolder.DocumentNoteViewHolder -> {
                val item = data[position] as DashboardView.Document
                holder.bindTitle(item.snippet)
                holder.bindSubtitle(item.typeName)
                holder.bindLoading(item.isLoading)
            }
        }

        holder.bindSelection(data[position].isSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.typeOf<DesktopDiffUtil.Payload>().forEach { payload ->
                when (holder) {
                    is ViewHolder.ArchiveHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    is ViewHolder.DocumentHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    is ViewHolder.DocumentWithoutIconViewHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    is ViewHolder.DocumentNoteViewHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    else -> Timber.d("Skipping payload update.")
                }
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.ArchiveHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Archive
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.DocumentHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Document
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.emojiChanged()) {
                bindIcon(item.icon)
            }
            if (payload.imageChanged()) {
                bindIcon(item.icon)
            }
            if (payload.isLoadingChanged) {
                bindLoading(item.isLoading)
            }
            if (payload.isSelectionChanged) {
                bindSelection(item.isSelected)
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.DocumentWithoutIconViewHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Document
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.isLoadingChanged) {
                bindLoading(item.isLoading)
            }
            if (payload.isSelectionChanged) {
                bindSelection(item.isSelected)
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.DocumentNoteViewHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Document
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.isLoadingChanged) {
                bindLoading(item.isLoading)
            }
            if (payload.isSelectionChanged) {
                bindSelection(item.isSelected)
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bindSelection(isSelected: Boolean)

        class ArchiveHolder(itemView: View) : ViewHolder(itemView) {

            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindTitle(title: String) {
                if (title.isNotEmpty()) {
                    itemView.archiveTitle.text = title
                }
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.title
            private val tvSubtitle = itemView.typeTitle
            private val shimmer = itemView.shimmer
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
            }

            fun bindSubtitle(subtitle: String?) {
                tvSubtitle.text = subtitle
            }

            fun bindIcon(icon: ObjectIcon) {
                itemView.iconWidget.bind(icon)
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                }
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class DocumentWithoutIconViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_desktop_page_without_icon,
                parent,
                false
            )
        ) {

            private val tvTitle = itemView.findViewById<TextView>(R.id.tvDocTitle)
            private val tvSubtitle = itemView.findViewById<TextView>(R.id.tvDocTypeName)
            private val shimmer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindTitle(title: String?) {
                tvTitle.text = title
            }

            fun bindSubtitle(type: String?) {
                tvSubtitle.text = type
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    tvSubtitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                    tvSubtitle.visible()
                }
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class DocumentNoteViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_desktop_page_without_icon,
                parent,
                false
            )
        ) {

            private val tvTitle = itemView.findViewById<TextView>(R.id.tvDocTitle)
            private val tvSubtitle = itemView.findViewById<TextView>(R.id.tvDocTypeName)
            private val shimmer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindTitle(title: String?) {
                tvTitle.text = title
            }

            fun bindSubtitle(type: String?) {
                tvSubtitle.text = type
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    tvSubtitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                    tvSubtitle.visible()
                }
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class DocumentTaskViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_desktop_page_task,
                parent,
                false
            )
        ) {

            private val tvTitle = itemView.findViewById<TextView>(R.id.tvDocTitle)
            private val tvSubtitle = itemView.findViewById<TextView>(R.id.tvDocTypeName)
            private val checkbox = itemView.findViewById<ImageView>(R.id.ivCheckbox)
            private val shimmer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindTitle(title: String?) {
                tvTitle.text = title
            }

            fun bindSubtitle(type: String?) {
                tvSubtitle.text = type
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    tvSubtitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                    tvSubtitle.visible()
                }
            }

            fun bindDone(done: Boolean?) {
                if (done == true) {
                    checkbox.setImageResource(R.drawable.ic_dashboard_task_checkbox_checked)
                } else {
                    checkbox.setImageResource(R.drawable.ic_dashboard_task_checkbox_not_checked)
                }
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class ObjectSetHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.title
            private val shimmer = itemView.shimmer
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                }
            }

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
            }

            fun bindIcon(icon: ObjectIcon) {
                itemView.iconWidget.bind(icon)
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }

        class ObjectSetWithoutIconHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.findViewById<TextView>(R.id.tvSetTitle)
            private val shimmer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)
            private val selection = itemView.findViewById<ImageView>(R.id.ivSelection)

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                }
            }

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
            }

            override fun bindSelection(isSelected: Boolean) {
                if (isSelected) selection.visible() else selection.invisible()
            }
        }
    }

    fun update(views: List<DashboardView>) {
        try {
            val old = ArrayList(data)
            val callback = DesktopDiffUtil(old = old, new = views)
            val result = DiffUtil.calculateDiff(callback)
            data = views
            result.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            Timber.e(e, "Error while updating dashboard adapter")
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = ArrayList(data).shift(fromPosition, toPosition)
        data = update
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun provideAdapterData(): List<DashboardView> = data.toList()
}
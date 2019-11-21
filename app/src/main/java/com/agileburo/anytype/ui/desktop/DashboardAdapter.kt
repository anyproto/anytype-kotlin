package com.agileburo.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.feature_desktop.utils.DesktopDiffUtil
import com.agileburo.anytype.presentation.desktop.DashboardView
import kotlinx.android.synthetic.main.item_desktop_page.view.*

class DashboardAdapter(
    private val data: MutableList<DashboardView>,
    private val onDocumentClicked: (DashboardView.Document) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DOCUMENT = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DOCUMENT -> {
                inflater.inflate(R.layout.item_desktop_page, parent, false).let {
                    ViewHolder.DocumentHolder(it)
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is DashboardView.Document -> VIEW_TYPE_DOCUMENT
            else -> throw IllegalStateException("Unexpected type")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.DocumentHolder -> {
                holder.bind(
                    doc = data[position] as DashboardView.Document,
                    onClick = onDocumentClicked
                )
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(doc: DashboardView.Document, onClick: (DashboardView.Document) -> Unit) {
                itemView.setOnClickListener { onClick(doc) }
                itemView.title.text = doc.title
            }
        }
    }

    fun update(views: List<DashboardView>) {

        val callback = DesktopDiffUtil(
            old = data,
            new = views
        )
        val result = DiffUtil.calculateDiff(callback)

        data.apply {
            clear()
            addAll(views)
        }

        result.dispatchUpdatesTo(this)
    }
}
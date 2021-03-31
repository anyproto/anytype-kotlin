package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.icon
import com.anytypeio.anytype.core_ui.extensions.text
import com.anytypeio.anytype.core_utils.diff.DefaultDiffUtil
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel.Mode
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel.ViewerSortView
import kotlinx.android.synthetic.main.item_viewer_sort.view.*

class ViewerSortAdapter(
    private val onViewerSortClicked: (ViewerSortView) -> Unit,
    private val onRemoveViewerSortClicked: (ViewerSortView) -> Unit,
    private val onAddViewerSortClicked: () -> Unit
) : RecyclerView.Adapter<ViewerSortAdapter.ViewHolder>() {

    private var views = emptyList<ViewerSortView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_viewer_sort -> {
                ViewHolder.Sort(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onViewerSortClicked(views[bindingAdapterPosition])
                    }
                    itemView.ivRemove.setOnClickListener {
                        onRemoveViewerSortClicked(views[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_viewer_sort_add -> {
                ViewHolder.Add(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener { onAddViewerSortClicked() }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder.Sort) holder.bind(views[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= views.size)
            R.layout.item_viewer_sort_add
        else
            R.layout.item_viewer_sort
    }

    override fun getItemCount(): Int = views.size + 1

    fun update(update: List<ViewerSortView>) {
        val differ = DefaultDiffUtil(old = views, new = update)
        val result = DiffUtil.calculateDiff(differ, false)
        views = update
        result.dispatchUpdatesTo(this)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Add(view: View) : ViewHolder(view)
        class Sort(view: View) : ViewHolder(view) {
            fun bind(item: ViewerSortView) = with(itemView) {
                if (item.mode == Mode.READ) {
                    ivRemove.gone()
                    ivGo.visible()
                } else {
                    ivRemove.visible()
                    ivGo.gone()
                }
                tvTitle.text = item.name
                tvSubtitle.setText(item.type.text(item.format))
                ivRelation.setImageResource(item.format.icon(isMedium = true))
            }
        }
    }
}
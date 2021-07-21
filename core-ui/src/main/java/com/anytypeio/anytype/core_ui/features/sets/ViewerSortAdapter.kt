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
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel.ScreenState
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel.ViewerSortView
import kotlinx.android.synthetic.main.item_viewer_sort.view.*

class ViewerSortAdapter(
    private val onViewerSortClicked: (ViewerSortView) -> Unit,
    private val onRemoveViewerSortClicked: (ViewerSortView) -> Unit
) : RecyclerView.Adapter<ViewerSortAdapter.ViewHolder>() {

    private var views = emptyList<ViewerSortView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder.Sort(
            view = inflater.inflate(R.layout.item_viewer_sort, parent, false)
        ).apply {
            itemView.setOnClickListener {
                onViewerSortClicked(views[bindingAdapterPosition])
            }
            itemView.ivRemove.setOnClickListener {
                onRemoveViewerSortClicked(views[bindingAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder.Sort) holder.bind(views[position])
    }

    override fun getItemCount(): Int = views.size

    fun update(update: List<ViewerSortView>) {
        val differ = DefaultDiffUtil(old = views, new = update)
        val result = DiffUtil.calculateDiff(differ, false)
        views = update
        result.dispatchUpdatesTo(this)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Sort(view: View) : ViewHolder(view) {
            fun bind(item: ViewerSortView) = with(itemView) {
                if (item.mode == ScreenState.READ) {
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
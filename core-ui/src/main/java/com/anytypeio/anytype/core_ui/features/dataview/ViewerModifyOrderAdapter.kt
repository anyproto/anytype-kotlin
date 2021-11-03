package com.anytypeio.anytype.core_ui.features.dataview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.android.synthetic.main.item_modify_viewer_relation_order.view.*

class ViewerModifyOrderAdapter(
    private val dragListener: OnStartDragListener,
    private val onItemClick: (SimpleRelationView) -> Unit,
    private val onDeleteClick: (SimpleRelationView) -> Unit
) : RecyclerView.Adapter<ViewerModifyOrderAdapter.Holder>(), SupportDragAndDropBehavior {

    val order: List<String> get() = items.map { it.key }
    private var items: List<SimpleRelationView> = emptyList()

    fun update(update: List<SimpleRelationView>) {
        items = update
        notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_modify_viewer_relation_order, parent, false)
        return Holder(view).apply {
            itemView.iconDrag.setOnTouchListener { _, event ->
                if (event.action == ACTION_DOWN) dragListener.onStartDrag(this)
                false
            }
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(items[pos])
                }
            }
            itemView.iconDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteClick(items[pos])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return if (items[toPosition].key == ObjectSetConfig.NAME_KEY) {
            false
        } else {
            val update = ArrayList(items).shift(fromPosition, toPosition)
            items = update
            notifyItemMoved(fromPosition, toPosition)
            true
        }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view), ItemTouchHelperViewHolder {

        fun bind(item: SimpleRelationView) {
            if (item.key == ObjectSetConfig.NAME_KEY) {
                itemView.iconDrag.invisible()
            } else {
                itemView.iconDrag.visible()
            }
            itemView.title.text = item.title
            itemView.iconRelation.bind(item.format)
            if (item.isReadonly || item.isDefault) {
                itemView.iconDelete.gone()
            } else {
                itemView.iconDelete.visible()
            }
        }

        override fun onItemSelected() {
            itemView.elevation = ITEM_ELEVATION
            itemView.setBackgroundResource(R.drawable.rectangle_modify_viewer_relation_order_dnd)
        }

        override fun onItemClear() {
            itemView.elevation = 0.0f
            itemView.setBackgroundResource(EMPTY_IMAGE_RES)
        }
    }

    companion object {
        const val EMPTY_IMAGE_RES = 0
        const val ITEM_ELEVATION = 40.0f
    }
}
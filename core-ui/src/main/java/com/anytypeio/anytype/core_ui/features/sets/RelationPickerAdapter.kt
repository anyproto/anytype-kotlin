package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.formatIcon
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.android.synthetic.main.item_list_base.view.*

abstract class RelationPickerAdapter(
    private val relations: List<SimpleRelationView>,
    private val relationSelectedKey: String?,
    private val click: (String?, String) -> Unit
) : RecyclerView.Adapter<RelationPickerAdapter.RelationHolder>() {

    abstract fun isRelationAvailable(key: String): Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RelationHolder(
            inflater.inflate(R.layout.item_list_base, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RelationHolder, position: Int) {
        holder.bind(
            relationSelectedKey = relationSelectedKey,
            relation = relations[position],
            click = click,
            isRelationAvailable = isRelationAvailable(relations[position].key)
        )
    }

    override fun getItemCount(): Int = relations.size

    inner class RelationHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val root = itemView
        private val title = itemView.text
        private val icon = itemView.icon

        fun bind(
            relationSelectedKey: String?,
            isRelationAvailable: Boolean,
            relation: SimpleRelationView,
            click: (String?, String) -> Unit
        ) {
            setIcon(icon, relation.format)
            title.text = relation.title
            root.isSelected = relation.key == relationSelectedKey
            root.alpha = if (root.isSelected || isRelationAvailable) {
                root.setOnClickListener { click(relationSelectedKey, relation.key) }
                ALPHA_ENABLE
            } else {
                ALPHA_DISABLE
            }
        }

        private fun setIcon(icon: ImageView, format: ColumnView.Format) {
            icon.setImageDrawable(itemView.context.formatIcon(format))
        }
    }

    companion object {
        private const val ALPHA_ENABLE = 1.0f
        private const val ALPHA_DISABLE = 0.2f
    }
}
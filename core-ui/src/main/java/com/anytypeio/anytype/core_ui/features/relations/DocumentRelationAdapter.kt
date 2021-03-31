package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.diff.DefaultDiffUtil
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import timber.log.Timber
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.RelationViewHolder as ViewHolder

class DocumentRelationAdapter(
    private var items: List<DocumentRelationView>,
    private val onRelationClicked: (DocumentRelationView) -> Unit,
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_document_relation_default -> {
                ViewHolder.Default(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        onRelationClicked(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_document_relation_object -> {
                ViewHolder.Object(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        onRelationClicked(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_document_relation_status -> {
                ViewHolder.Status(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        onRelationClicked(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_document_relation_tag -> {
                ViewHolder.Tags(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        onRelationClicked(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_document_relation_file -> {
                ViewHolder.File(view = inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        onRelationClicked(items[bindingAdapterPosition])
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Status -> {
                holder.bind(items[position] as DocumentRelationView.Status)
            }
            is ViewHolder.Tags -> {
                holder.bind(items[position] as DocumentRelationView.Tags)
            }
            is ViewHolder.Object -> {
                holder.bind(items[position] as DocumentRelationView.Object)
            }
            is ViewHolder.File -> {
                holder.bind(items[position] as DocumentRelationView.File)
            }
            is ViewHolder.Default -> {
                holder.bind(items[position])
            }
            else -> { Timber.d("Skipping binding for: $holder") }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is DocumentRelationView.Object -> R.layout.item_document_relation_object
        is DocumentRelationView.Status -> R.layout.item_document_relation_status
        is DocumentRelationView.Tags -> R.layout.item_document_relation_tag
        is DocumentRelationView.File -> R.layout.item_document_relation_file
        else -> R.layout.item_document_relation_default
    }

    fun update(update: List<DocumentRelationView>) {
        Timber.d("Updating adapter: $update")
        val differ = DefaultDiffUtil(old = items, new = update)
        val result = DiffUtil.calculateDiff(differ, false)
        items = update
        result.dispatchUpdatesTo(this)
    }
}
package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemFeaturedRelationTagsBinding
import com.anytypeio.anytype.core_ui.features.relations.holders.FeaturedRelationViewHolder
import com.anytypeio.anytype.presentation.relations.DocumentRelationView

class FeaturedRelationAdapter(
    private var items: List<DocumentRelationView> = emptyList()
): RecyclerView.Adapter<FeaturedRelationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): FeaturedRelationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.item_featured_relation_default -> {
                FeaturedRelationViewHolder.Default(
                    binding = ItemFeaturedRelationDefaultBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            R.layout.item_featured_relation_tags -> {
                FeaturedRelationViewHolder.Tags(
                    binding = ItemFeaturedRelationTagsBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            R.layout.item_featured_relation_status -> {
                FeaturedRelationViewHolder.Status(
                    binding = ItemFeaturedRelationStatusBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: FeaturedRelationViewHolder, position: Int) {
        when(holder) {
            is FeaturedRelationViewHolder.Default -> {
                holder.bind(items[position])
            }
            is FeaturedRelationViewHolder.Tags -> {
                holder.bind(items[position] as DocumentRelationView.Tags)
            }
            is FeaturedRelationViewHolder.Status -> {
                holder.bind(items[position] as DocumentRelationView.Status)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when(items[position]) {
        is DocumentRelationView.Default -> R.layout.item_featured_relation_default
        is DocumentRelationView.Tags -> R.layout.item_featured_relation_tags
        is DocumentRelationView.Status -> R.layout.item_featured_relation_status
        is DocumentRelationView.Checkbox -> TODO()
        is DocumentRelationView.File -> TODO()
        is DocumentRelationView.Object -> TODO()
        is DocumentRelationView.ObjectType -> R.layout.item_featured_relation_default
    }

    fun update(items: List<DocumentRelationView>) {
        this.items = items
        notifyDataSetChanged()
    }
}
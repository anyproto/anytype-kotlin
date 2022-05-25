package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.linking.LinkToItemView

class ObjectLinksAdapter(
    private val onClicked: (LinkToItemView) -> Unit
) : ListAdapter<LinkToItemView, ObjectLinksAdapter.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_link_to_object_subheading -> ViewHolder.SubheadingViewHolder(
                inflater.inflate(viewType, parent, false)
            )
            R.layout.item_link_to_object_create -> ViewHolder.CreateObjectViewHolder(
                inflater.inflate(viewType, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onClicked(getItem(pos))
                    }
                }
            }
            R.layout.item_list_object ->
                ViewHolder.ObjectViewHolder(
                    inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            R.layout.item_link_to_web -> {
                ViewHolder.WebLinkViewHolder(
                    inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is LinkToItemView.CreateObject -> R.layout.item_link_to_object_create
        is LinkToItemView.Object -> R.layout.item_list_object
        is LinkToItemView.WebItem -> R.layout.item_link_to_web
        is LinkToItemView.Subheading -> R.layout.item_link_to_object_subheading
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ObjectViewHolder -> {
                holder.bind(getItem(position) as LinkToItemView.Object)
            }
            is ViewHolder.SubheadingViewHolder -> {
                holder.bind(getItem(position) as LinkToItemView.Subheading)
            }
            is ViewHolder.CreateObjectViewHolder -> {
                holder.bind(getItem(position) as LinkToItemView.CreateObject)
            }
            is ViewHolder.WebLinkViewHolder -> {
                holder.bind(getItem(position) as LinkToItemView.WebItem)
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class CreateObjectViewHolder(view: View) : ViewHolder(view) {

            private val title = itemView.findViewById<TextView>(R.id.tvTitle)

            fun bind(item: LinkToItemView.CreateObject) {
                title.text =
                    itemView.context.getString(R.string.create_object_with_name, item.title)
            }
        }

        class SubheadingViewHolder(view: View) : ViewHolder(view) {

            private val title = itemView.findViewById<TextView>(R.id.subheading)

            fun bind(item: LinkToItemView.Subheading) {
                title.text = when (item) {
                    LinkToItemView.Subheading.Objects -> itemView.context.getString(R.string.objects)
                    LinkToItemView.Subheading.Web -> itemView.context.getString(R.string.web_pages)
                }
            }
        }

        class WebLinkViewHolder(view: View) : ViewHolder(view) {

            private val title = itemView.findViewById<TextView>(R.id.tvTitle)

            fun bind(item: LinkToItemView.WebItem) {
                title.text = item.url
            }
        }

        class ObjectViewHolder(view: View) : ViewHolder(view) {

            private val title = itemView.findViewById<TextView>(R.id.tvTitle)
            private val subtitle = itemView.findViewById<TextView>(R.id.tvSubtitle)
            private val icon = itemView.findViewById<ObjectIconWidget>(R.id.ivIcon)
            private val divider = itemView.findViewById<View>(R.id.divider)

            fun bind(link: LinkToItemView.Object) {
                title.text = link.title
                subtitle.text = link.subtitle
                icon.setIcon(link.icon)
                divider.visible()
            }
        }
    }

    object Differ : DiffUtil.ItemCallback<LinkToItemView>() {
        override fun areItemsTheSame(oldItem: LinkToItemView, newItem: LinkToItemView): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: LinkToItemView, newItem: LinkToItemView): Boolean =
            oldItem == newItem
    }
}
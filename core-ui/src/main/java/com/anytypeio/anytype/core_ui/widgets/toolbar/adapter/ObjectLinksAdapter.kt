package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToCopyLinkBinding
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToObjectCreateBinding
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToObjectSubheadingBinding
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToPasteFromClipboardBinding
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToRemoveLinkBinding
import com.anytypeio.anytype.core_ui.databinding.ItemLinkToWebBinding
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectBinding
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.linking.LinkToItemView

class ObjectLinksAdapter(
    private val onClicked: (LinkToItemView) -> Unit
) : ListAdapter<LinkToItemView, ObjectLinksAdapter.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUBHEADING -> ViewHolder.SubheadingViewHolder(
                ItemLinkToObjectSubheadingBinding.inflate(inflater, parent, false)
            )
            TYPE_CREATE_OBJECT -> ViewHolder.CreateObjectViewHolder(
                ItemLinkToObjectCreateBinding.inflate(inflater, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onClicked(getItem(pos))
                    }
                }
            }
            TYPE_OBJECT ->
                ViewHolder.ObjectViewHolder(
                    ItemListObjectBinding.inflate(inflater, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            TYPE_WEB -> {
                ViewHolder.WebLinkViewHolder(
                    ItemLinkToWebBinding.inflate(inflater, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            TYPE_COPY_LINK -> {
                ViewHolder.CopyLinkViewHolder(
                    ItemLinkToCopyLinkBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            TYPE_REMOVE_LINK -> {
                ViewHolder.RemoveLinkViewHolder(
                    ItemLinkToRemoveLinkBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            TYPE_PASTE_FROM_CLIPBOARD -> {
                ViewHolder.PasteFromClipboardViewHolder(
                    ItemLinkToPasteFromClipboardBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            TYPE_LINKED_TO_OBJECT -> {
                ViewHolder.LinkedToObject(
                    ItemListObjectBinding.inflate(inflater, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(getItem(pos))
                        }
                    }
                }
            }
            TYPE_LINKED_TO_URL -> {
                ViewHolder.LinkedToWeb(
                    ItemLinkToWebBinding.inflate(inflater, parent, false)
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
        is LinkToItemView.CreateObject -> TYPE_CREATE_OBJECT
        is LinkToItemView.Object -> TYPE_OBJECT
        is LinkToItemView.WebItem -> TYPE_WEB
        is LinkToItemView.Subheading -> TYPE_SUBHEADING
        is LinkToItemView.CopyLink -> TYPE_COPY_LINK
        LinkToItemView.RemoveLink -> TYPE_REMOVE_LINK
        LinkToItemView.PasteFromClipboard -> TYPE_PASTE_FROM_CLIPBOARD
        is LinkToItemView.LinkedTo.Object -> TYPE_LINKED_TO_OBJECT
        is LinkToItemView.LinkedTo.Url -> TYPE_LINKED_TO_URL
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
            is ViewHolder.LinkedToObject -> {
                holder.bind(getItem(position) as LinkToItemView.LinkedTo.Object)
            }
            is ViewHolder.LinkedToWeb -> {
                holder.bind(getItem(position) as LinkToItemView.LinkedTo.Url)
            }
            is ViewHolder.CopyLinkViewHolder -> {}
            is ViewHolder.PasteFromClipboardViewHolder -> {}
            is ViewHolder.RemoveLinkViewHolder -> {}
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class CreateObjectViewHolder(binding: ItemLinkToObjectCreateBinding) :
            ViewHolder(binding.root) {

            private val title = itemView.findViewById<TextView>(R.id.tvTitle)

            fun bind(item: LinkToItemView.CreateObject) {
                title.text =
                    itemView.context.getString(R.string.create_object_with_name, item.title)
            }
        }

        class SubheadingViewHolder(binding: ItemLinkToObjectSubheadingBinding) :
            ViewHolder(binding.root) {

            private val title = binding.subheading

            fun bind(item: LinkToItemView.Subheading) {
                title.text = when (item) {
                    LinkToItemView.Subheading.Objects -> itemView.context.getString(R.string.objects)
                    LinkToItemView.Subheading.Web -> itemView.context.getString(R.string.web_pages)
                    LinkToItemView.Subheading.LinkedTo -> itemView.context.getString(R.string.linked_to)
                    LinkToItemView.Subheading.Actions -> itemView.context.getString(R.string.actions)
                }
            }
        }

        open class WebLinkViewHolder(binding: ItemLinkToWebBinding) : ViewHolder(binding.root) {

            private val title = binding.tvTitle

            fun bind(item: LinkToItemView.WebItem) {
                title.text = item.url
            }
        }

        open class ObjectViewHolder(binding: ItemListObjectBinding) : ViewHolder(binding.root) {

            private val title = binding.tvTitle
            private val subtitle = binding.tvSubtitle
            private val icon = binding.ivIcon
            private val divider = binding.divider

            fun bind(link: LinkToItemView.Object) {
                title.text = link.title
                subtitle.text = link.subtitle
                icon.setIcon(link.icon)
                divider.visible()
            }
        }

        class CopyLinkViewHolder(binding: ItemLinkToCopyLinkBinding) : ViewHolder(binding.root)
        class RemoveLinkViewHolder(binding: ItemLinkToRemoveLinkBinding) : ViewHolder(binding.root)
        class PasteFromClipboardViewHolder(binding: ItemLinkToPasteFromClipboardBinding) :
            ViewHolder(binding.root)

        class LinkedToObject(binding: ItemListObjectBinding) : ViewHolder(binding.root) {

            private val title = binding.tvTitle
            private val subtitle = binding.tvSubtitle
            private val icon = binding.ivIcon
            private val divider = binding.divider

            fun bind(link: LinkToItemView.LinkedTo.Object) {
                title.text = link.title
                subtitle.text = link.subtitle
                icon.setIcon(link.icon)
                divider.visible()
            }
        }

        class LinkedToWeb(binding: ItemLinkToWebBinding) : ViewHolder(binding.root) {
            private val title = binding.tvTitle

            fun bind(item: LinkToItemView.LinkedTo.Url) {
                title.text = item.url
            }
        }
    }

    companion object {

        const val TYPE_SUBHEADING = 1
        const val TYPE_WEB = 5
        const val TYPE_CREATE_OBJECT = 6
        const val TYPE_OBJECT = 7
        const val TYPE_LINKED_TO_URL = 8
        const val TYPE_LINKED_TO_OBJECT = 9
        const val TYPE_REMOVE_LINK = 10
        const val TYPE_COPY_LINK = 11
        const val TYPE_PASTE_FROM_CLIPBOARD = 12
    }

    object Differ : DiffUtil.ItemCallback<LinkToItemView>() {
        override fun areItemsTheSame(oldItem: LinkToItemView, newItem: LinkToItemView): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: LinkToItemView, newItem: LinkToItemView): Boolean =
            oldItem == newItem
    }
}
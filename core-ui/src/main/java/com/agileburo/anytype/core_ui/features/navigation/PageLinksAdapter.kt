package com.agileburo.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_utils.ext.gone
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_page_link.view.*

class PageLinksAdapter(
    private val data: MutableList<PageLinkView>,
    private val onClick: (String) -> Unit,
    private var filterText: String = EMPTY
) : RecyclerView.Adapter<PageLinksAdapter.PageLinkHolder>() {

    fun updateLinks(links: List<PageLinkView>) {
        data.clear()
        data.addAll(links)
        notifyDataSetChanged()
    }

    fun filterBy(filterText: String) {
        this.filterText = filterText
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        filterText = EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageLinkHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_page_link, parent, false)
        return PageLinkHolder(view)
    }

    override fun getItemCount(): Int = filterData().size

    override fun onBindViewHolder(holderLink: PageLinkHolder, position: Int) {
        holderLink.bind(filterData()[position], onClick)
    }

    private fun filterData(): List<PageLinkView> =
        if (filterText.isNotEmpty()) {
            data.filter { it.isContainsText(filterText) }
        } else {
            data
        }

    class PageLinkHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val title = itemView.tvTitle
        private val subtitle = itemView.tvSubtitle
        private val icon = itemView.icon
        private val image = itemView.image

        fun bind(link: PageLinkView, onClick: (String) -> Unit) {
            itemView.setOnClickListener { onClick(link.id) }
            title.text = if (link.title.isEmpty()) untitled else link.title
            if (link.subtitle.isBlank()) {
                subtitle.gone()
            } else {
                subtitle.visible()
                subtitle.text = link.subtitle
            }

            image.setImageDrawable(null)
            icon.setImageDrawable(null)
            link.image?.let { url ->
                Glide
                    .with(image)
                    .load(url)
                    .centerInside()
                    .circleCrop()
                    .into(image)
            }
            if (link.emoji != null) {
                Glide
                    .with(icon)
                    .load(Emojifier.uri(link.emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(icon)
            }
        }
    }

    companion object {
        const val EMPTY = ""
    }
}
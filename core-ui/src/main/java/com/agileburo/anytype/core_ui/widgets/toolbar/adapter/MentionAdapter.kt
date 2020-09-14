package com.agileburo.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_mention.view.*
import timber.log.Timber

class MentionAdapter(
    private var data: ArrayList<Mention>,
    private var mentionFilter: String = "",
    private val clicked: (Mention, String) -> Unit,
    private val newClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setData(mentions: List<Mention>) {
        if (mentions.isEmpty()) {
            data.clear()
        } else {
            data.clear()
            data.addAll(mentions)
            notifyDataSetChanged()
        }
    }

    fun updateFilter(filter: String) {
        mentionFilter = filter
        notifyDataSetChanged()
    }

    /**
     * Filter all mentions by mentionFilter without symbol @
     *
     */
    private fun getFilteredData(): List<Mention> =
        data.filterBy(mentionFilter.removePrefix(MENTION_PREFIX))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NEW_PAGE -> NewPageViewHolder(
                inflater.inflate(
                    R.layout.item_mention_new_page,
                    parent,
                    false
                )
            )
            TYPE_MENTION -> MentionViewHolder(
                inflater.inflate(
                    R.layout.item_mention,
                    parent,
                    false
                )
            )
            else -> throw RuntimeException("Wrong viewType")
        }
    }

    override fun getItemCount(): Int = getFilteredData().size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        POSITION_NEW_PAGE -> TYPE_NEW_PAGE
        else -> TYPE_MENTION
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_NEW_PAGE -> (holder as NewPageViewHolder).bind(newClicked)
            TYPE_MENTION -> (holder as MentionViewHolder).bind(
                getFilteredData()[position - 1],
                clicked,
                mentionFilter
            )
        }
    }

    class NewPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(newClicked: () -> Unit) {
            itemView.setOnClickListener { newClicked() }
        }
    }

    class MentionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTitle: TextView = itemView.text
        private val image: ImageView = itemView.image

        fun bind(item: Mention, clicked: (Mention, String) -> Unit, filter: String) {
            itemView.setOnClickListener { clicked(item, filter) }
            tvTitle.text = item.title
            when {
                !item.emoji.isNullOrBlank() -> {
                    try {
                        Glide
                            .with(image)
                            .load(Emojifier.uri(item.emoji))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(image)
                    } catch (e: Throwable) {
                        Timber.e(e, "Error while setting emoji icon for: ${item.emoji}")
                    }
                }
                !item.image.isNullOrBlank() -> {
                    Glide
                        .with(image)
                        .load(item.image)
                        .centerInside()
                        .circleCrop()
                        .into(image)
                }
                else -> {
                    image.setImageResource(R.drawable.ic_block_empty_page)
                }
            }
        }
    }

    companion object {
        const val MENTION_PREFIX = "@"
        const val POSITION_NEW_PAGE = 0
        const val TYPE_NEW_PAGE = 1
        const val TYPE_MENTION = 2
    }
}

data class Mention(
    val id: String,
    val title: String,
    val emoji: String?,
    val image: String?
)

fun List<Mention>.filterBy(text: String): List<Mention> =
    if (text.isNotEmpty()) filter { it.isContainsText(text) } else this

fun Mention.isContainsText(text: String): Boolean = title.contains(text, true)
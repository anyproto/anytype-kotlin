package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.sets.CreateObjectSetObjectTypeView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_create_set_object_type.view.*
import timber.log.Timber

class CreateSetObjectTypeAdapter(
    private val onObjectTypeSelected: (String) -> Unit
) : RecyclerView.Adapter<CreateSetObjectTypeAdapter.ViewHolder>() {

    var views: List<CreateObjectSetObjectTypeView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            view = inflater.inflate(
                R.layout.item_create_set_object_type,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(views[position], onObjectTypeSelected)
    }

    override fun getItemCount(): Int = views.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val emoji = itemView.linkEmoji
        private val title = itemView.pageTitle

        fun bind(
            view: CreateObjectSetObjectTypeView,
            onObjectTypeSelected: (String) -> Unit
        ) {
            title.text = view.name
            setEmojiIcon(view)
            itemView.setOnClickListener {
                onObjectTypeSelected(view.url)
            }
        }

        private fun setEmojiIcon(view: CreateObjectSetObjectTypeView) {
            try {
                Glide
                    .with(emoji)
                    .load(Emojifier.uri(view.emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(emoji)
            } catch (e: Throwable) {
                Timber.e(e, "Error while setting emoji icon for: ${view.emoji}")
            }
        }
    }
}
package com.agileburo.anytype.library_page_icon_picker_widget.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.emojifier.Emojifier
import com.agileburo.anytype.emojifier.data.Emoji
import com.agileburo.anytype.library_page_icon_picker_widget.R
import com.agileburo.anytype.library_page_icon_picker_widget.model.EmojiPickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_category_header.view.*
import kotlinx.android.synthetic.main.item_page_icon_picker_emoji_item.view.*
import timber.log.Timber

sealed class DocumentEmojiIconPickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class CategoryHeader(view: View) : DocumentEmojiIconPickerViewHolder(view) {

        private val category = itemView.category

        fun bind(item: EmojiPickerView.GroupHeader) {
            when (item.category) {
                Emoji.CATEGORY_SMILEYS_AND_PEOPLE -> category.setText(R.string.category_smileys_and_people)
                Emoji.CATEGORY_ANIMALS_AND_NATURE -> category.setText(R.string.category_animals_and_nature)
                Emoji.CATEGORY_FOOD_AND_DRINK -> category.setText(R.string.category_food_and_drink)
                Emoji.CATEGORY_ACTIVITY_AND_SPORT -> category.setText(R.string.category_activity_and_sport)
                Emoji.CATEGORY_TRAVEL_AND_PLACES -> category.setText(R.string.category_travel_and_places)
                Emoji.CATEGORY_OBJECTS -> category.setText(R.string.category_objects)
                Emoji.CATEGORY_SYMBOLS -> category.setText(R.string.category_symbols)
                Emoji.CATEGORY_FLAGS -> category.setText(R.string.category_flags)
                else -> Timber.d("Unexpected category: ${item.category}")
            }
        }
    }

    class EmojiItem(view: View) : DocumentEmojiIconPickerViewHolder(view) {

        private val image = itemView.image

        fun bind(
            item: EmojiPickerView.Emoji,
            onEmojiClicked: (String) -> Unit
        ) {
            Glide
                .with(image)
                .load(Emojifier.uri(item.page, item.index))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image)

            itemView.setOnClickListener { onEmojiClicked(item.unicode) }
        }
    }

    companion object {
        const val HOLDER_EMOJI_CATEGORY_HEADER = 1
        const val HOLDER_EMOJI_ITEM = 2
    }
}
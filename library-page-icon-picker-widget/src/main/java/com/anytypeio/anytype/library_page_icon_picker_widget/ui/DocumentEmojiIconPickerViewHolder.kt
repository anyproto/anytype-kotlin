package com.anytypeio.anytype.library_page_icon_picker_widget.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.library_page_icon_picker_widget.R
import com.anytypeio.anytype.library_page_icon_picker_widget.databinding.ItemPageIconPickerEmojiCategoryHeaderBinding
import com.anytypeio.anytype.library_page_icon_picker_widget.databinding.ItemPageIconPickerEmojiItemBinding
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

sealed class DocumentEmojiIconPickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class CategoryHeader(val binding: ItemPageIconPickerEmojiCategoryHeaderBinding) : DocumentEmojiIconPickerViewHolder(binding.root) {

        private val category = binding.category

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

    class EmojiItem(val binding: ItemPageIconPickerEmojiItemBinding) : DocumentEmojiIconPickerViewHolder(binding.root) {

        private val image = binding.image

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
}
package com.agileburo.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerView
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerAdapter
import com.agileburo.anytype.library_page_icon_picker_widget.ui.PageIconPickerViewHolder
import com.vdurmont.emoji.EmojiManager
import kotlinx.android.synthetic.main.sample_page_icon_picker_activity.*

class PageIconPickerSampleActivity : AppCompatActivity(R.layout.sample_page_icon_picker_activity) {

    private val emojis by lazy {
        EmojiManager
            .getAll()
            .map { emoji ->
                PageIconPickerView.Emoji(
                    unicode = emoji.unicode
                )
            }

    }

    private val pageIconPickerAdapter = PageIconPickerAdapter(
        views = listOf(
            PageIconPickerView.Action.UploadPhoto,
            PageIconPickerView.Action.PickRandomly,
            PageIconPickerView.Action.ChooseEmoji,
            PageIconPickerView.EmojiFilter
        ) + emojis,
        onUploadPhotoClicked = {},
        onSetRandomEmojiClicked = {},
        onFilterQueryChanged = {}
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyler.apply {
            setItemViewCacheSize(100)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when (val type = pageIconPickerAdapter.getItemViewType(position)) {
                            PageIconPickerViewHolder.HOLDER_UPLOAD_PHOTO -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_CHOOSE_EMOJI -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_PICK_RANDOM_EMOJI -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_CATEGORY_HEADER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_FILTER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            PageIconPickerViewHolder.HOLDER_EMOJI_ITEM -> 1
                            else -> throw IllegalStateException("Unexpected view type: $type")
                        }
                }
            }
            adapter = pageIconPickerAdapter.apply {
                setHasStableIds(true)
            }
        }
    }

    companion object {
        const val PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT = 8
    }
}
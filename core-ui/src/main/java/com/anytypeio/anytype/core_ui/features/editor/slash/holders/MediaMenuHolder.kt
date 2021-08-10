package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_style.view.*

class MediaMenuHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: SlashItem.Media) = with(itemView) {
        when (item) {
            SlashItem.Media.Bookmark -> {
                tvTitle.setText(R.string.slash_widget_media_bookmark)
                tvSubtitle.setText(R.string.slash_widget_media_bookmark_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_media_bookmark)
            }
            SlashItem.Media.Code -> {
                tvTitle.setText(R.string.slash_widget_media_code)
                tvSubtitle.setText(R.string.slash_widget_media_code_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_media_code)
            }
            SlashItem.Media.File -> {
                tvTitle.setText(R.string.slash_widget_media_file)
                tvSubtitle.setText(R.string.slash_widget_media_file_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_media_file)
            }
            SlashItem.Media.Picture -> {
                tvTitle.setText(R.string.slash_widget_media_picture)
                tvSubtitle.setText(R.string.slash_widget_media_picture_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_media_picture)
            }
            SlashItem.Media.Video -> {
                tvTitle.setText(R.string.slash_widget_media_video)
                tvSubtitle.setText(R.string.slash_widget_media_video_subtitle)
                ivIcon.setImageResource(R.drawable.ic_slash_media_video)
            }
        }
    }
}
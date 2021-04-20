package com.anytypeio.anytype.core_ui.features.page.slash.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_main.view.*

class MainMenuHolder(view: View): RecyclerView.ViewHolder(view) {

    fun bind(item: SlashItem.Main) = with(itemView) {
        textMain.setText(item.title)
        iconMain.setImageResource(item.icon)
    }
}
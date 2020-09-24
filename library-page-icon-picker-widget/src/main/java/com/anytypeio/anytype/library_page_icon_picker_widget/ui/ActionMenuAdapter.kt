package com.anytypeio.anytype.library_page_icon_picker_widget.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.library_page_icon_picker_widget.R
import kotlinx.android.synthetic.main.action_toolbar_page_icon_item.view.*

class ActionMenuAdapter(
    private val options : IntArray,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ActionMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = ViewHolder(
        view = LayoutInflater.from(parent.context).inflate(
            R.layout.action_toolbar_page_icon_item,
            parent,
            false
        )
    )

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) = holder.bind(options[position], onClick)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title = itemView.title
        private val icon = itemView.icon

        fun bind(
            option: Int,
            onClick: (Int) -> Unit
        ) {
            itemView.setOnClickListener { onClick(option) }
            when(option) {
                OPTION_CHOOSE_EMOJI -> {
                    title.setText(R.string.page_icon_picker_choose_emoji)
                    icon.setImageResource(R.drawable.ic_page_icon_picker_choose_emoji)
                }
                OPTION_CHOOSE_RANDOM_EMOJI -> {
                    title.setText(R.string.page_icon_picker_pick_emoji_randomly)
                    icon.setImageResource(R.drawable.ic_page_icon_picker_random_emoji)
                }
                OPTION_CHOOSE_UPLOAD_PHOTO -> {
                    title.setText(R.string.page_icon_picker_upload_photo)
                    icon.setImageResource(R.drawable.ic_page_icon_picker_upload_photo)
                }
                OPTION_REMOVE -> {
                    title.setText(R.string.page_icon_picker_remove_text)
                    icon.setImageResource(R.drawable.ic_remove_page_icon)
                }
            }
        }
    }

    companion object {
        const val OPTION_CHOOSE_EMOJI = 0
        const val OPTION_CHOOSE_RANDOM_EMOJI = 1
        const val OPTION_CHOOSE_UPLOAD_PHOTO = 2
        const val OPTION_REMOVE = 3
    }
}
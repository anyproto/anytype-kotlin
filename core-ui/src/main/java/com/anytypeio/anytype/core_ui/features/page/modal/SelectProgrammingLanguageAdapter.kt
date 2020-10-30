package com.anytypeio.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.modal.SelectProgrammingLanguageAdapter.Holder
import kotlinx.android.synthetic.main.item_select_programming_language.view.*

class SelectProgrammingLanguageAdapter(
    private val items: List<Pair<String, String>>,
    private val onLangSelected: (String) -> Unit
) : RecyclerView.Adapter<Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(
            view = inflater.inflate(
                R.layout.item_select_programming_language,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (key, value) = items[position]
        holder.bind(value) { onLangSelected(key) }
    }

    override fun getItemCount(): Int = items.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val lang: TextView = itemView.lang
        fun bind(value: String, onClick: () -> Unit) {
            lang.text = value
            itemView.setOnClickListener { onClick() }
        }
    }
}
package com.anytypeio.anytype.ui.database.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.databaseview.models.ListItem
import com.anytypeio.anytype.ui.database.list.viewholder.ListBoardViewHolder
import com.anytypeio.anytype.ui.database.table.adapter.toView

class ListBoardAdapter(
    private val click: (String) -> Unit
) :
    RecyclerView.Adapter<ListBoardViewHolder>() {

    private val data = mutableListOf<ListItem>()

    fun setData(items: List<ListItem>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListBoardViewHolder =
        ListBoardViewHolder(
            LayoutInflater.from(parent.context).toView(
                id = R.layout.item_list_board,
                parent = parent
            )
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ListBoardViewHolder, position: Int) {
        holder.bind(data[position], click)
    }
}
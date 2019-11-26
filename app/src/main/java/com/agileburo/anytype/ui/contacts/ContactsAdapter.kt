package com.agileburo.anytype.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.contacts.model.ContactView
import com.agileburo.anytype.ui.contacts.viewholders.ContactViewHolder
import com.agileburo.anytype.ui.table.toView

class ContactsAdapter(
    private val click: (String) -> Unit
) :
    RecyclerView.Adapter<ContactViewHolder>() {

    private val data = mutableListOf<ContactView>()

    fun updateData(contacts: List<ContactView>) {
        val size = data.size
        data.addAll(contacts)
        notifyItemRangeInserted(size, contacts.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder(
            LayoutInflater.from(parent.context).toView(
                id = R.layout.item_list_contact,
                parent = parent
            )
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(data[position], click)
    }
}
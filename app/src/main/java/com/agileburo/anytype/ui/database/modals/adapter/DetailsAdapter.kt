package com.agileburo.anytype.ui.database.modals.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ItemTouchHelperAdapter
import com.agileburo.anytype.presentation.databaseview.models.ColumnView
import com.agileburo.anytype.presentation.databaseview.models.Swap
import com.agileburo.anytype.ui.database.modals.viewholder.details.*
import com.agileburo.anytype.ui.database.table.adapter.toView

class DetailsAdapter(
    private val swap: (Swap) -> Unit,
    private val data: MutableList<ColumnView>,
    private val click: (ColumnView) -> Unit,
    private val isDragOn: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

    override fun getItemViewType(position: Int): Int =
        when (data[position]) {
            is ColumnView.Title -> VIEW_TYPE_TITLE
            is ColumnView.Text -> VIEW_TYPE_TEXT
            is ColumnView.Number -> VIEW_TYPE_NUMBER
            is ColumnView.Date -> VIEW_TYPE_DATE
            is ColumnView.Select -> VIEW_TYPE_SELECT
            is ColumnView.Multiple -> VIEW_TYPE_MULTIPLE
            is ColumnView.Person -> VIEW_TYPE_PERSON
            is ColumnView.File -> VIEW_TYPE_FILE
            is ColumnView.Checkbox -> VIEW_TYPE_CHECKBOX
            is ColumnView.URL -> VIEW_TYPE_URL
            is ColumnView.Email -> VIEW_TYPE_EMAIL
            is ColumnView.Phone -> VIEW_TYPE_PHONE
            ColumnView.AddNew -> VIEW_TYPE_NEW
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        LayoutInflater.from(parent.context).run {
            when (viewType) {
                VIEW_TYPE_TITLE ->
                    TitleViewHolder(
                        itemView = this.toView(R.layout.item_property_title, parent)
                    )
                VIEW_TYPE_TEXT ->
                    TextViewHolder(
                        itemView = this.toView(R.layout.item_property_text, parent)
                    )
                VIEW_TYPE_PERSON ->
                    PersonViewHolder(
                        itemView = this.toView(R.layout.item_property_person, parent)
                    )
                VIEW_TYPE_SELECT ->
                    SelectViewHolder(
                        itemView = this.toView(R.layout.item_property_select, parent)
                    )
                VIEW_TYPE_EMAIL ->
                    EmailViewHolder(
                        itemView = this.toView(R.layout.item_property_email, parent)
                    )
                VIEW_TYPE_NEW ->
                    AddNewViewHolder(
                        itemView = this.toView(R.layout.item_property_add_new, parent)
                    )
                VIEW_TYPE_NUMBER ->
                    NumberViewHolder(
                        itemView = this.toView(R.layout.item_property_number, parent)
                    )
                VIEW_TYPE_MULTIPLE ->
                    MultipleSelectViewHolder(
                        itemView = this.toView(R.layout.item_property_multiple, parent)
                    )
                VIEW_TYPE_DATE ->
                    DateViewHolder(
                        itemView = this.toView(R.layout.item_property_date, parent)
                    )
                VIEW_TYPE_FILE ->
                    FileMediaViewHolder(
                        itemView = this.toView(R.layout.item_property_file, parent)
                    )
                VIEW_TYPE_CHECKBOX ->
                    CheckboxViewHolder(
                        itemView = this.toView(R.layout.item_property_checkbox, parent)
                    )
                VIEW_TYPE_URL ->
                    UrlViewHolder(
                        itemView = this.toView(R.layout.item_property_url, parent)
                    )
                VIEW_TYPE_PHONE ->
                    PhoneViewHolder(
                        itemView = this.toView(R.layout.item_property_phone, parent)
                    )
                else -> throw RuntimeException(Throwable("Unknown view type!"))
            }
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detail = data[position]
        when (detail) {
            is ColumnView.Title -> (holder as TitleViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Text -> (holder as TextViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Number -> (holder as NumberViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Date -> (holder as DateViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Select -> (holder as SelectViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Multiple -> (holder as MultipleSelectViewHolder).bind(
                click,
                detail, isDragOn
            )
            is ColumnView.Person -> (holder as PersonViewHolder).bind(click, detail, isDragOn)
            is ColumnView.File -> (holder as FileMediaViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Checkbox -> (holder as CheckboxViewHolder).bind(click, detail, isDragOn)
            is ColumnView.URL -> (holder as UrlViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Email -> (holder as EmailViewHolder).bind(click, detail, isDragOn)
            is ColumnView.Phone -> (holder as PhoneViewHolder).bind(click, detail, isDragOn)
            is ColumnView.AddNew -> (holder as AddNewViewHolder).bind(click, detail)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                data[i] = data.set(i + 1, data[i])
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                data[i] = data.set(i - 1, data[i])
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        swap(Swap(fromPosition, toPosition))
        return true
    }

    companion object {
        const val VIEW_TYPE_TITLE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_NUMBER = 3
        const val VIEW_TYPE_DATE = 4
        const val VIEW_TYPE_SELECT = 5
        const val VIEW_TYPE_MULTIPLE = 6
        const val VIEW_TYPE_PERSON = 7
        const val VIEW_TYPE_FILE = 9
        const val VIEW_TYPE_URL = 10
        const val VIEW_TYPE_EMAIL = 11
        const val VIEW_TYPE_PHONE = 12
        const val VIEW_TYPE_NEW = 13
        const val VIEW_TYPE_CHECKBOX = 14
    }
}
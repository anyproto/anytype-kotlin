package com.agileburo.anytype.ui.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.databaseview.models.Cell
import com.agileburo.anytype.presentation.databaseview.models.Column
import com.agileburo.anytype.presentation.databaseview.models.Row
import com.agileburo.anytype.ui.table.viewholder.columns.*
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder

class TableAdapter(context: Context) :
    AbstractTableAdapter<Column, Row, Cell>(context) {


    // -------------- Cell --------------------

    override fun getCellItemViewType(position: Int): Int = 0

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder =
        LayoutInflater.from(parent.context).run {
            when (viewType) {
                VIEW_TYPE_TITLE ->
                    ColumnTitleViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_title, parent)
                    )
                VIEW_TYPE_TEXT ->
                    ColumnTextViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_text, parent)
                    )
                VIEW_TYPE_NUMBER ->
                    ColumnNumberViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_number, parent)
                    )
                VIEW_TYPE_DATE ->
                    ColumnDateViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_date, parent)
                    )
                VIEW_TYPE_ACCOUNT ->
                    ColumnAccountViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_account, parent)
                    )
                VIEW_TYPE_SELECT ->
                    ColumnSelectViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_select, parent)
                    )
                VIEW_TYPE_MULTIPLE ->
                    ColumnMultipleViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_multiple, parent)
                    )
                VIEW_TYPE_LINK ->
                    ColumnLinkViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_link, parent)
                    )
                VIEW_TYPE_PHONE ->
                    ColumnPhoneViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_phone, parent)
                    )
                VIEW_TYPE_EMAIL ->
                    ColumnEmailViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_email, parent)
                    )
                VIEW_TYPE_BOOL ->
                    ColumnBoolViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_bool, parent)
                    )
                VIEW_TYPE_FILE ->
                    ColumnFileViewHolder(
                        itemView = this.toView(R.layout.item_table_cell_file, parent)
                    )
                else -> throw RuntimeException(Throwable("Unknown view type!"))
            }
        }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder?,
        cellItemModel: Any?,
        columnPosition: Int,
        rowPosition: Int
    ) {}


    // -------------- Column --------------------

    override fun getColumnHeaderItemViewType(position: Int): Int =
        when (mColumnHeaderItems[position]) {
            is Column.Title -> VIEW_TYPE_TITLE
            is Column.Text -> VIEW_TYPE_TEXT
            is Column.Number -> VIEW_TYPE_NUMBER
            is Column.Date -> VIEW_TYPE_DATE
            is Column.Select -> VIEW_TYPE_SELECT
            is Column.Multiple -> VIEW_TYPE_MULTIPLE
            is Column.Account -> VIEW_TYPE_ACCOUNT
            is Column.File -> VIEW_TYPE_FILE
            is Column.Bool -> VIEW_TYPE_BOOL
            is Column.Link -> VIEW_TYPE_LINK
            is Column.Email -> VIEW_TYPE_EMAIL
            is Column.Phone -> VIEW_TYPE_PHONE
        }

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = (LayoutInflater.from(parent.context)).run {
        when (viewType) {
            VIEW_TYPE_TITLE ->
                ColumnTitleViewHolder(
                    itemView = this.toView(R.layout.item_table_column_title, parent)
                )
            VIEW_TYPE_TEXT ->
                ColumnTextViewHolder(
                    itemView = this.toView(R.layout.item_table_column_text, parent)
                )
            VIEW_TYPE_NUMBER ->
                ColumnNumberViewHolder(
                    itemView = this.toView(R.layout.item_table_column_number, parent)
                )
            VIEW_TYPE_DATE ->
                ColumnDateViewHolder(
                    itemView = this.toView(R.layout.item_table_column_date, parent)
                )
            VIEW_TYPE_ACCOUNT ->
                ColumnAccountViewHolder(
                    itemView = this.toView(R.layout.item_table_column_account, parent)
                )
            VIEW_TYPE_SELECT ->
                ColumnSelectViewHolder(
                    itemView = this.toView(R.layout.item_table_column_select, parent)
                )
            VIEW_TYPE_MULTIPLE ->
                ColumnMultipleViewHolder(
                    itemView = this.toView(R.layout.item_table_column_multiple, parent)
                )
            VIEW_TYPE_LINK ->
                ColumnLinkViewHolder(
                    itemView = this.toView(R.layout.item_table_column_link, parent)
                )
            VIEW_TYPE_PHONE ->
                ColumnPhoneViewHolder(
                    itemView = this.toView(R.layout.item_table_column_phone, parent)
                )
            VIEW_TYPE_EMAIL ->
                ColumnEmailViewHolder(
                    itemView = this.toView(R.layout.item_table_column_email, parent)
                )
            VIEW_TYPE_BOOL ->
                ColumnBoolViewHolder(
                    itemView = this.toView(R.layout.item_table_column_bool, parent)
                )
            VIEW_TYPE_FILE ->
                ColumnFileViewHolder(
                    itemView = this.toView(R.layout.item_table_column_file, parent)
                )
            else -> throw RuntimeException(Throwable("Unknown view type!"))
        }
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder?,
        columnHeaderItemModel: Any?,
        columnPosition: Int
    ) {}

    // -------------- Row --------------------

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder?,
        rowHeaderItemModel: Any?,
        rowPosition: Int
    ) = Unit

    override fun onCreateRowHeaderViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): AbstractViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateCornerView(): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        val VIEW_TYPE_TITLE = 1
        val VIEW_TYPE_TEXT = 2
        val VIEW_TYPE_NUMBER = 3
        val VIEW_TYPE_DATE = 4
        val VIEW_TYPE_SELECT = 5
        val VIEW_TYPE_MULTIPLE = 6
        val VIEW_TYPE_ACCOUNT = 7
        val VIEW_TYPE_BOOL = 8
        val VIEW_TYPE_FILE = 9
        val VIEW_TYPE_LINK = 10
        val VIEW_TYPE_EMAIL = 11
        val VIEW_TYPE_PHONE = 112
    }
}

fun LayoutInflater.toView(id: Int, parent: ViewGroup): View = this.inflate(id, parent, false)
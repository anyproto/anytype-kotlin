package com.agileburo.anytype.ui.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.databaseview.models.Cell
import com.agileburo.anytype.presentation.databaseview.models.Column
import com.agileburo.anytype.presentation.databaseview.models.Row
import com.agileburo.anytype.ui.table.holder.CellNameViewHolder
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder

private const val NAME_CELL_TYPE = 1
private const val DATE_CELL_TYPE = 2


class TableAdapter(context: Context) :
    AbstractTableAdapter<Column, Row, Cell>(context) {

    // -------------- Cell --------------------

    override fun getCellItemViewType(position: Int): Int = 0

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder =
        LayoutInflater.from(parent.context).run {
            CellNameViewHolder(this.inflate(R.layout.item_table_name_cell, parent, false))
//            when (viewType) {
//                NAME_CELL_TYPE ->
//                DATE_CELL_TYPE -> CellDateViewHolder(
//                    itemView = this.inflate(R.layout.item_table_date_cell, parent, false)
//                )
//                else -> throw RuntimeException("Unknown cell type")
//            }
        }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder?,
        cellItemModel: Any?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    // -------------- Column --------------------

    override fun getColumnHeaderItemViewType(position: Int): Int = 0

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): AbstractViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder?,
        columnHeaderItemModel: Any?,
        columnPosition: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // -------------- Row --------------------

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder?,
        rowHeaderItemModel: Any?,
        rowPosition: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateRowHeaderViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): AbstractViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }





    override fun onCreateCornerView(): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }





}
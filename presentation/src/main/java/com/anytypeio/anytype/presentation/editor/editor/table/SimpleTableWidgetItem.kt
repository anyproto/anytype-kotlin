package com.anytypeio.anytype.presentation.editor.editor.table

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

sealed class SimpleTableWidgetItem {

    sealed class Cell : SimpleTableWidgetItem() {
        object ClearContents : Cell()
        object Color : Cell()
        object Style : Cell()
        object ResetStyle : Cell()
    }

    sealed class Column : SimpleTableWidgetItem() {
        data class InsertLeft(val column: BlockView.Table.ColumnId) : Column()
        data class InsertRight(val column: BlockView.Table.ColumnId) : Column()
        data class MoveLeft(val column: BlockView.Table.ColumnId) : Column()
        data class MoveRight(val column: BlockView.Table.ColumnId) : Column()
        data class Duplicate(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class Delete(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class Copy(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class ClearContents(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class Sort(val column: BlockView.Table.ColumnId) : Column()
        data class Color(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class Style(val columns: List<BlockView.Table.ColumnId>) : Column()
        data class ResetStyle(val columns: List<BlockView.Table.ColumnId>) : Column()
    }

    sealed class Row : SimpleTableWidgetItem() {
        data class InsertAbove(val row: BlockView.Table.RowId) : Row()
        data class InsertBelow(val row: BlockView.Table.RowId) : Row()
        data class MoveUp(val row: BlockView.Table.RowId) : Row()
        data class MoveDown(val row: BlockView.Table.RowId) : Row()
        data class Duplicate(val rows: List<BlockView.Table.RowId>) : Row()
        data class Delete(val rows: List<BlockView.Table.RowId>) : Row()
        data class Copy(val rows: List<BlockView.Table.RowId>) : Row()
        data class ClearContents(val rows: List<BlockView.Table.RowId>) : Row()
        data class Color(val rows: List<BlockView.Table.RowId>) : Row()
        data class Style(val rows: List<BlockView.Table.RowId>) : Row()
        data class ResetStyle(val rows: List<BlockView.Table.RowId>) : Row()
    }

    sealed class Tab : SimpleTableWidgetItem() {
        object Cell : Tab()
        object Row : Tab()
        object Column : Tab()
    }
}
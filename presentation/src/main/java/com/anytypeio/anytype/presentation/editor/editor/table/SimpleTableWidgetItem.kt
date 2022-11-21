package com.anytypeio.anytype.presentation.editor.editor.table

sealed class SimpleTableWidgetItem {

    sealed class Cell : SimpleTableWidgetItem() {
        object ClearContents : Cell()
        object Color : Cell()
        object Style : Cell()
        object ClearStyle : Cell()
    }

    sealed class Column : SimpleTableWidgetItem() {
        object InsertLeft : Column()
        object InsertRight : Column()
        object MoveLeft : Column()
        object MoveRight : Column()
        object Duplicate : Column()
        object Delete : Column()
        object ClearContents : Column()
        object Sort : Column()
        object Color : Column()
        object Style : Column()
    }

    sealed class Row : SimpleTableWidgetItem() {
        object InsertAbove : Row()
        object InsertBelow : Row()
        object MoveUp : Row()
        object MoveDown : Row()
        object Duplicate : Row()
        object Delete : Row()
        object ClearContents : Row()
        object Sort : Row()
        object Color : Row()
        object Style : Row()
    }

    sealed class Tab : SimpleTableWidgetItem() {
        object Cell : Tab()
        object Row : Tab()
        object Column : Tab()
    }
}
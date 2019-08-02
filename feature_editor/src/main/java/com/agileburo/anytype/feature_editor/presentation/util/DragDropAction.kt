package com.agileburo.anytype.feature_editor.presentation.util

sealed class DragDropAction {
    data class Consume(val target: Int, val consumer: Int) : DragDropAction()
    data class Shift(val from: Int, val to: Int) : DragDropAction()
}
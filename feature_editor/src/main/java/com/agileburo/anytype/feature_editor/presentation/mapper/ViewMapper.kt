package com.agileburo.anytype.feature_editor.presentation.mapper

interface ViewMapper<in D, out V> {
    fun mapToView(model: D): V
}
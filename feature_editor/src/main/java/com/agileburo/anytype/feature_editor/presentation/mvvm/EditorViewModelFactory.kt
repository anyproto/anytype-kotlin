package com.agileburo.anytype.feature_editor.presentation.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.core_utils.ext.BaseSchedulerProvider
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter

class EditorViewModelFactory(
    private val editorInteractor: EditorInteractor,
    private val contentTypeConverter: BlockContentTypeConverter,
    private val schedulerProvider: BaseSchedulerProvider

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        EditorViewModel(
            interactor = editorInteractor,
            contentTypeConverter = contentTypeConverter,
            schedulerProvider = schedulerProvider
        ) as T
}
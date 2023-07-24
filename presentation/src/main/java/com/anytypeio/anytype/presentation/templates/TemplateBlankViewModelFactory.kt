package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import javax.inject.Inject

class TemplateBlankViewModelFactory @Inject constructor(
    private val renderer: DefaultBlockViewRenderer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplateBlankViewModel(
            renderer = renderer
        ) as T
    }
}
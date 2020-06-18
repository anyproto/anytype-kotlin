package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.icon.SetIconName

class PageIconPickerViewModelFactory(
    private val setIconName: SetIconName
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = DocumentIconPickerViewModel(
        setIconName = setIconName
    ) as T
}
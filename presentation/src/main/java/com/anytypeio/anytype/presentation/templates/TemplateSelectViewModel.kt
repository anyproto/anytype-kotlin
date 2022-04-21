package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import javax.inject.Inject

class TemplateSelectViewModel(
    private val createPage: CreatePage
) : BaseViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onCancel(type: Id) {
        // TODO
    }

    fun onUseTemplate(type: Id, template: Id) {
        // TODO
    }

    class Factory @Inject constructor(
        private val createPage: CreatePage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateSelectViewModel(
                createPage = createPage
            ) as T
        }
    }
}
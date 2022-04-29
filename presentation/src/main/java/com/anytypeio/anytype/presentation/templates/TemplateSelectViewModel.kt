package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TemplateSelectViewModel(
    private val applyTemplate: ApplyTemplate
) : BaseViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> = MutableLiveData()

    fun onUseTemplate(ctx: Id, template: Id) {
        viewModelScope.launch {
            val result = applyTemplate.execute(
                ApplyTemplate.Params(
                    ctx = ctx,
                    template = template
                )
            )
            if (result.isFailure) {
                sendToast("Something went wrong. Please, try again later.")
            }
            isDismissed.emit(true)
        }
    }

    class Factory @Inject constructor(
        private val applyTemplate: ApplyTemplate
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateSelectViewModel(
                applyTemplate = applyTemplate
            ) as T
        }
    }
}
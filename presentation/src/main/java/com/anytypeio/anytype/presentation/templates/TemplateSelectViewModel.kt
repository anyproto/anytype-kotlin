package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class TemplateSelectViewModel(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getTemplates: GetTemplates,
    private val applyTemplate: ApplyTemplate
) : BaseViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val viewState: StateFlow<ViewState> = _viewState

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onStart(type: Id, withoutBlankTemplate: Boolean) {
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(type)
            if (objType != null) {
                proceedWithGettingTemplates(objType, withoutBlankTemplate)
            }
        }
    }

    private fun proceedWithGettingTemplates(
        objType: ObjectWrapper.Type, withoutBlankTemplate: Boolean
    ) {
        val params = GetTemplates.Params(objType.id)
        viewModelScope.launch {
            getTemplates.async(params)
                .fold(
                    onSuccess = { buildTemplateViews(objType, it, withoutBlankTemplate) },
                    onFailure = { Timber.e(it, "Error while getting templates") })
        }
    }

    private suspend fun buildTemplateViews(
        objType: ObjectWrapper.Type,
        templates: List<ObjectWrapper.Basic>,
        withoutBlankTemplate: Boolean
    ) {
        val templateViews = buildList {
            if (!withoutBlankTemplate) add(
                TemplateView.Blank(
                    typeId = objType.id,
                    typeName = objType.name.orEmpty(),
                    layout = objType.recommendedLayout?.code ?: 0
                )
            )
            addAll(templates.map { TemplateView.Template(it.id) })
        }
        _viewState.emit(
            ViewState.Success(
                objectTypeName = objType.name.orEmpty(),
                templates = templateViews,
            )
        )
    }

    fun onUseTemplateButtonPressed(ctx: Id, currentItem: Int) {
        when (val state = _viewState.value) {
            is ViewState.Success -> {
                when (val template = state.templates[currentItem]) {
                    is TemplateView.Blank -> {
                        dismiss()
                    }
                    is TemplateView.Template -> {
                        proceedWithApplyingTemplate(ctx, template)
                    }
                }
            }
            else -> {
                Timber.e("onUseTemplate: unexpected state $state")
            }
        }
    }

    private fun proceedWithApplyingTemplate(ctx: Id, template: TemplateView.Template) {
        val params = ApplyTemplate.Params(ctx = ctx, template = template.id)
        viewModelScope.launch {
            applyTemplate.async(params).fold(
                onSuccess = {
                    Timber.d("Template ${template.id} applied successfully")
                    dismiss()
                },
                onFailure = {
                    Timber.e(it, "Error while applying template")
                    sendToast("Something went wrong. Please, try again later.")
                    dismiss()
                }
            )
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            isDismissed.emit(true)
        }
    }

    class Factory @Inject constructor(
        private val applyTemplate: ApplyTemplate,
        private val getTemplates: GetTemplates,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateSelectViewModel(
                applyTemplate = applyTemplate,
                getTemplates = getTemplates,
                storeOfObjectTypes = storeOfObjectTypes
            ) as T
        }
    }

    sealed class ViewState {
        data class Success(
            val objectTypeName: String, val templates: List<TemplateView>
        ) : ViewState()

        object Init : ViewState()
        object ErrorGettingType : ViewState()
    }

    sealed class TemplateView {
        data class Blank(
            val typeId: Id, val typeName: String, val layout: Int
        ) : TemplateView()

        data class Template(val id: Id) : TemplateView()
    }
}
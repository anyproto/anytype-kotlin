package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSelectTemplateEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TemplateSelectViewModel(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getTemplates: GetTemplates,
    private val applyTemplate: ApplyTemplate,
    private val analytics: Analytics,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val urlBuilder: UrlBuilder
) : BaseViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val isDismissed = MutableStateFlow(false)

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val viewState: StateFlow<ViewState> = _viewState

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onStart(type: Id, withoutBlankTemplate: Boolean) {
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(type)
            if (objType != null) {
                Timber.d("onStart, Object type $objType")
                proceedWithGettingTemplates(objType, withoutBlankTemplate)
            } else {
                Timber.e("onStart, Object type $type not found")
            }
        }
    }

    private fun proceedWithGettingTemplates(
        objType: ObjectWrapper.Type, withoutBlankTemplate: Boolean
    ) {
        val params = GetTemplates.Params(
            type = TypeId(objType.id)
        )
        viewModelScope.launch {
            getTemplates.async(params).fold(
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
                TemplateSelectView.Blank(
                    typeId = objType.id,
                    typeName = objType.name.orEmpty(),
                    layout = objType.recommendedLayout?.code ?: 0
                )
            )
            addAll(templates.mapNotNull {
                val typeKey = objType.key
                if (typeKey != null) {
                    TemplateSelectView.Template(
                        id = it.id,
                        layout = it.layout ?: ObjectType.Layout.BASIC,
                        typeId = objType.id,
                        typeKey = typeKey
                    )
                } else null
            })
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
                    is TemplateSelectView.Blank -> {
                        proceedWithApplyingTemplate(ctx, "")
                    }
                    is TemplateSelectView.Template -> {
                        proceedWithApplyingTemplate(ctx, template.id)
                    }
                }
                viewModelScope.launch {
                    sendAnalyticsSelectTemplateEvent(analytics)
                }
            }
            else -> {
                Timber.e("onUseTemplate: unexpected state $state")
                isDismissed.value = true
            }
        }
    }

    private fun proceedWithApplyingTemplate(ctx: Id, id: Id) {
        val params = ApplyTemplate.Params(ctx = ctx, template = id)
        viewModelScope.launch {
            applyTemplate.async(params).fold(
                onSuccess = {
                    isDismissed.value = true
                    Timber.d("Template ${id} applied successfully")
                },
                onFailure = {
                    isDismissed.value = true
                    Timber.e(it, "Error while applying template")
                    sendToast("Something went wrong. Please, try again later.")
                }
            )
        }
    }

    fun onSkipButtonClicked() {
        isDismissed.value = true
    }

    class Factory @Inject constructor(
        private val applyTemplate: ApplyTemplate,
        private val getTemplates: GetTemplates,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val analytics: Analytics,
        private val coverImageHashProvider: CoverImageHashProvider,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateSelectViewModel(
                applyTemplate = applyTemplate,
                getTemplates = getTemplates,
                storeOfObjectTypes = storeOfObjectTypes,
                analytics = analytics,
                coverImageHashProvider = coverImageHashProvider,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    sealed class ViewState {
        data class Success(
            val objectTypeName: String, val templates: List<TemplateSelectView>
        ) : ViewState()

        object Init : ViewState()
    }

}
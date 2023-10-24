package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.settings.SPACE_STORAGE_SUBSCRIPTION_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsViewModel(
    private val analytics: Analytics,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceManager: SpaceManager,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val gradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder
): BaseViewModel() {

    val spaceViewState = MutableStateFlow<ViewState<SpaceData>>(ViewState.Init)

    init {
        viewModelScope.launch {
            spaceManager
                .observe()
                .flatMapLatest { config ->
                    storelessSubscriptionContainer.subscribe(
                        StoreSearchByIdsParams(
                            subscription = SPACE_STORAGE_SUBSCRIPTION_ID,
                            targets = listOf(config.spaceView),
                            keys = listOf(
                                Relations.ID,
                                Relations.SPACE_ID,
                                Relations.NAME,
                                Relations.ICON_EMOJI,
                                Relations.ICON_IMAGE,
                                Relations.ICON_OPTION,
                                Relations.CREATED_DATE,
                                Relations.CREATOR,
                                Relations.TARGET_SPACE_ID
                            )
                        )
                    ).mapNotNull { results ->
                        results.firstOrNull()
                    }.map { wrapper ->
                        SpaceData(
                            name = wrapper.name.orEmpty(),
                            icon = wrapper.spaceIcon(
                                builder = urlBuilder,
                                spaceGradientProvider = gradientProvider
                            ),
                            createdDate = wrapper
                                .getValue<Double?>(Relations.CREATED_DATE)
                                .toString(),
                            createdBy = wrapper
                                .getValue<Id?>(Relations.CREATOR)
                                .toString(),
                            spaceId = wrapper.getValue<Id>(Relations.TARGET_SPACE_ID)
                        )
                    }
                }.collect {
                    Timber.d("Setting space data: ${it}")
                    spaceViewState.value = ViewState.Success(it)
                }
        }
    }

    fun onNameSet(name: String) {
        Timber.d("onNameSet")
        if (name.isEmpty()) return
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            if (config != null) {
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = SpaceId(config.space),
                        details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        Timber.d("Name successfully set for current space: ${config.space}")
                    }
                )
            } else {
                Timber.w("Something went wrong: config is empty")
            }
        }
    }

    fun onStop() {
        // TODO unsubscribe
    }

    data class SpaceData(
        val spaceId: Id?,
        val createdDate: String?,
        val createdBy: Id?,
        val name: String,
        val icon: SpaceIconView,
    )

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val setSpaceDetails: SetSpaceDetails,
        private val gradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = SpaceSettingsViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            spaceManager = spaceManager,
            setSpaceDetails = setSpaceDetails,
            gradientProvider = gradientProvider,
            analytics = analytics
        ) as T
    }
}
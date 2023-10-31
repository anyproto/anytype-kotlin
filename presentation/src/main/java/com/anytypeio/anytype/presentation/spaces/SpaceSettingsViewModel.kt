package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.settings.SPACE_STORAGE_SUBSCRIPTION_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsViewModel(
    private val analytics: Analytics,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceManager: SpaceManager,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val gradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace,
    private val configStorage: ConfigStorage,
    private val debugSpaceShareDownloader: DebugSpaceShareDownloader
): BaseViewModel() {

    val commands = MutableSharedFlow<Command>()
    val isDismissed = MutableStateFlow(false)
    val spaceViewState = MutableStateFlow<ViewState<SpaceData>>(ViewState.Init)

    init {
        viewModelScope.launch {
            spaceManager
                .observe()
                .take(1)
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
                            createdDateInMillis = wrapper
                                .getValue<Double?>(Relations.CREATED_DATE)
                                ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() },
                            createdBy = wrapper
                                .getValue<Id?>(Relations.CREATOR)
                                .toString(),
                            spaceId = config.space,
                            network = config.network,
                            isDeletable = isSpaceDeletable(config.space)
                        )
                    }
                }.collect { spaceViewState.value = ViewState.Success(it) }
        }
    }

    private fun isSpaceDeletable(space: Id) : Boolean {
        val personalSpace = resolvePersonalSpace()
        return if (personalSpace == null)
            false
        else
            personalSpace != space
    }

    private fun resolvePersonalSpace() : Id? {
        return configStorage.getOrNull()?.space
    }

    fun onNameSet(name: String) {
        Timber.d("onNameSet")
        if (name.isEmpty()) return
        if (isDismissed.value) return
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
                        sendToast("Something went wrong. Please try again")
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

    fun onSpaceDebugClicked() {
        proceedWithSpaceDebug()
    }

    fun onDeleteSpaceClicked() {
        val state = spaceViewState.value
        if (state is ViewState.Success) {
            val space = state.data.spaceId
            val config = configStorage.getOrNull()
            if (config == null) {
                sendToast("Account config not found")
                return
            }
            val personalSpaceId = config.space
            if (space != null && space != personalSpaceId) {
                viewModelScope.launch {
                    deleteSpace.async(params = SpaceId(space)).fold(
                        onSuccess = {
                            fallbackToPersonalSpaceAfterDeletion(personalSpaceId)
                        },
                        onFailure = {
                            Timber.e(it, "Error while deleting space")
                        }
                    )
                }
            } else {
                sendToast("Space not found. Please, try again later")
            }
        }
    }

    private suspend fun fallbackToPersonalSpaceAfterDeletion(personalSpaceId: Id) {
        spaceManager.set(personalSpaceId)
        isDismissed.value = true
    }

    private fun proceedWithSpaceDebug() {
        viewModelScope.launch {
            debugSpaceShareDownloader
                .stream(Unit)
                .collect { result ->
                    result.fold(
                        onLoading = { sendToast(SPACE_DEBUG_MSG) },
                        onSuccess = { path -> commands.emit(Command.ShareSpaceDebug(path)) }
                    )
                }
        }
    }

    data class SpaceData(
        val spaceId: Id?,
        val createdDateInMillis: Long?,
        val createdBy: Id?,
        val network: Id?,
        val name: String,
        val icon: SpaceIconView,
        val isDeletable: Boolean = false
    )

    sealed class Command {
        data class ShareSpaceDebug(val filepath: Filepath) : Command()
    }

    class Factory @Inject constructor(
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val setSpaceDetails: SetSpaceDetails,
        private val gradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager,
        private val deleteSpace: DeleteSpace,
        private val configStorage: ConfigStorage,
        private val debugFileShareDownloader: DebugSpaceShareDownloader
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
            analytics = analytics,
            deleteSpace = deleteSpace,
            configStorage = configStorage,
            debugSpaceShareDownloader = debugFileShareDownloader
        ) as T
    }

    companion object {
        const val SPACE_DEBUG_MSG = "Kindly share this debug logs with Anytype developers."
    }
}
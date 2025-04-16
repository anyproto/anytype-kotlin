package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val createSpace: CreateSpace,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    val commands = MutableSharedFlow<Command>(replay = 0)

    val spaceIconView : MutableStateFlow<SpaceIconView.Placeholder> = MutableStateFlow(
        SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
    )

    init {
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.screenSettingsSpaceCreate)
        }
    }

    val isDismissed = MutableStateFlow(false)

    fun onCreateSpace(name: String, isSpaceLevelChatSwitchChecked: Boolean) {
        Timber.d("onCreateSpace, isSpaceLevelChatSwitchChecked: $isSpaceLevelChatSwitchChecked")
        if (isDismissed.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        val numberOfActiveSpaces = spaceViewContainer.get().filter { it.isActive }.size
        viewModelScope.launch {
            createSpace.stream(
                CreateSpace.Params(
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_OPTION to spaceIconView.value.color.index.toDouble()
                    ),
                    shouldApplyEmptyUseCase = numberOfActiveSpaces >= MAX_SPACE_COUNT_WITH_GET_STARTED_USE_CASE,
                    withChat = BuildConfig.DEBUG && isSpaceLevelChatSwitchChecked
                )
            ).collect { result ->
                result.fold(
                    onLoading = { isInProgress.value = true },
                    onSuccess = { space: Id ->
                        analytics.sendEvent(
                            eventName = EventsDictionary.createSpace,
                            props = Props(
                                mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation)
                            )
                        )
                        setNewSpaceAsCurrentSpace(space)
                        Timber.d("Successfully created space: $space").also {
                            isInProgress.value = false
                            commands.emit(Command.SwitchSpace(space = Space(space)))
                        }
                    },
                    onFailure = {
                        Timber.e(it, "Error while creating space").also {
                            sendToast("Error while creating space, please try again.")
                            isInProgress.value = false
                        }
                    }
                )
            }
        }
    }

    private suspend fun setNewSpaceAsCurrentSpace(space: Id) {
        spaceManager.set(space)
    }

    fun onSpaceIconClicked() {
        proceedWithResettingRandomSpaceGradient()
    }

    private fun proceedWithResettingRandomSpaceGradient() {
        spaceIconView.value = SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
    }

    class Factory @Inject constructor(
        private val createSpace: CreateSpace,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val spaceViewContainer: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace,
            spaceManager = spaceManager,
            analytics = analytics,
            spaceViewContainer = spaceViewContainer
        ) as T
    }

    sealed class Command {
        data class SwitchSpace(
            val space: Space
        ): Command()
    }

    companion object {
        // Always applying "empty" use-case when creating new space
        const val MAX_SPACE_COUNT_WITH_GET_STARTED_USE_CASE = 0
    }
}
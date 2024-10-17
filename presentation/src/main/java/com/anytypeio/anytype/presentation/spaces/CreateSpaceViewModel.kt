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
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateSpaceViewModel(
    private val createSpace: CreateSpace,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    val isInProgress = MutableStateFlow(false)

    private var spaceGradientId = spaceGradientProvider.randomId()

    val spaceGradient : MutableStateFlow<SpaceIconView.Placeholder>

    init {
        val view = SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
        spaceGradient = MutableStateFlow(view)

        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.screenSettingsSpaceCreate)
        }
    }

    val isDismissed = MutableStateFlow(false)
    val isSucceeded = MutableStateFlow(false)
    val exitWithMultiplayerTip = MutableStateFlow(false)

    fun onCreateSpace(name: String) {
        if (isDismissed.value || isSucceeded.value) {
            return
        }
        if (isInProgress.value) {
            sendToast("Please wait...")
            return
        }
        val isSingleSpace = spaceViewContainer.get().size == 1
        viewModelScope.launch {
            createSpace.stream(
                CreateSpace.Params(
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_OPTION to spaceGradientId.toDouble()
                    )
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
                            if (isSingleSpace) {
                                exitWithMultiplayerTip.value = true
                            } else {
                                isSucceeded.value = true
                            }
                            isInProgress.value = false
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
        spaceGradientId = spaceGradientProvider.randomId()
        spaceGradient.value = SpaceIconView.Placeholder(
            color = SystemColor.entries.random()
        )
    }

    class Factory @Inject constructor(
        private val createSpace: CreateSpace,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val spaceViewContainer: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel(
            createSpace = createSpace,
            spaceGradientProvider = spaceGradientProvider,
            spaceManager = spaceManager,
            analytics = analytics,
            spaceViewContainer = spaceViewContainer
        ) as T
    }
}
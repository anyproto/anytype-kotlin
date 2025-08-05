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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.allUniqueBy
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSpaceViewModel(
    private val container: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val analytics: Analytics,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val jobs = mutableListOf<Job>()

    private val spaces: Flow<List<ObjectWrapper.SpaceView>> = spaceViewContainer
        .observe()
        .map { results ->
            results.filter { space ->
                space.spaceLocalStatus == SpaceStatus.OK
                        && !space.spaceAccountStatus.isDeletedOrRemoving()
            }
        }
        .catch {
            Timber.e(it, "Error in spaces subscriptions")
            emit(emptyList())
        }.map { spaces ->
            if (BuildConfig.DEBUG) {
                assert(spaces.allUniqueBy { it.id }) {
                    "There were duplicated objects. Need to investigate this issue"
                }
            }
            spaces.distinctBy { it.id }
        }

    private fun buildUI() {
        jobs += viewModelScope.launch {
            combine(
                spaces,
                spaceManager.observe()
            ) { spaces, config ->
                buildList {
                    val spaceViews = spaces.mapNotNull { wrapper ->
                        val space = wrapper.targetSpaceId
                        if (space != null) {
                            SelectSpaceView.Space(
                                WorkspaceView(
                                    id = wrapper.id,
                                    name = wrapper.name,
                                    space = space,
                                    isSelected = space == config.space,
                                    icon = wrapper.spaceIcon(
                                        builder = urlBuilder,
                                        spaceGradientProvider = spaceGradientProvider
                                    ),
                                    isShared = wrapper.spaceAccessType == SpaceAccessType.SHARED
                                )
                            )
                        } else {
                            null
                        }
                    }
                    val (active, others) = spaceViews.partition { view -> view.view.isSelected }
                    addAll(active)
                    addAll(others)
                    if (size < MAX_SPACE_COUNT) {
                        add(SelectSpaceView.Create)
                    }
                }
            }
                .catch {
                    Timber.e(it, "Error in building UI for select space screen")
                    emit(emptyList())
                }
                .collect { results ->
                    views.value = results
                }
        }
    }

    fun onStart() {
        buildUI()
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenVault,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to EventsDictionary.Type.menu
                    )
                )
            )
        }
    }

    fun onStop() {
        jobs.cancel()
        proceedWithUnsubscribing()
    }

    fun onSpaceClicked(view: WorkspaceView) {
        viewModelScope.launch {
            Timber.d("Setting space: $view")
            if (!view.isSelected) {
                analytics.sendEvent(eventName = EventsDictionary.switchSpace)
                val space = SpaceId(view.space)
                spaceManager.set(space.id).fold(
                    onSuccess = {
                        saveCurrentSpace.async(SaveCurrentSpace.Params(space)).fold(
                            onFailure = {
                                Timber.e(it, "Error while saving current space in user settings")
                            },
                            onSuccess = {
                                commands.emit(Command.SwitchToNewSpace(space))
                            }
                        )
                    },
                    onFailure = {
                        Timber.e(it, "Could not select space")
                    }
                )
            } else {
                commands.emit(Command.Dismiss)
            }
        }
    }

    fun onCreateSpaceClicked() {
        val count = views.value.count { view -> view is SelectSpaceView.Space }
        if (count >= MAX_SPACE_COUNT) {
            sendToast(SPACE_COUNT_EXCEEDED_ERROR)
        } else {
            viewModelScope.launch {
                commands.emit(Command.CreateSpace)
            }
        }
    }

    private fun proceedWithUnsubscribing() {
        viewModelScope.launch {
            container.unsubscribe(
                subscriptions = listOf(
                    SELECT_SPACE_SUBSCRIPTION
                )
            )
        }
    }

    class Factory @Inject constructor(
        private val container: StorelessSubscriptionContainer,
        private val spaceManager: SpaceManager,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val urlBuilder: UrlBuilder,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val analytics: Analytics,
        private val spaceViewContainer: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            container = container,
            spaceManager = spaceManager,
            spaceGradientProvider = spaceGradientProvider,
            urlBuilder = urlBuilder,
            saveCurrentSpace = saveCurrentSpace,
            analytics = analytics,
            spaceViewContainer = spaceViewContainer
        ) as T
    }

    companion object {
        const val SELECT_SPACE_SUBSCRIPTION = "select_space_subscription.spaces"
        const val SPACE_COUNT_EXCEEDED_ERROR = "Space max count exceeded. You cannot create more."
        const val MAX_SPACE_COUNT = 50
    }
}

data class WorkspaceView(
    val id: Id,
    val space: Id,
    val name: String?,
    val isSelected: Boolean = false,
    val isShared: Boolean,
    val icon: SpaceIconView,
)

sealed class SelectSpaceView {
    data class Space(
        val view: WorkspaceView
    ) : SelectSpaceView()
    sealed class Profile : SelectSpaceView() {
        data class Default(
            val name: String,
            val icon: ProfileIconView,
        ) : Profile()
        data object Loading : Profile()
    }
    data object Create : SelectSpaceView()
}

sealed class Command {
    data object CreateSpace : Command()
    data object Dismiss : Command()
    data class SwitchToNewSpace(val space: Space): Command()
}
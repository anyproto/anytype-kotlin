package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.allUniqueBy
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSpaceViewModel(
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val appActionManager: AppActionManager,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val analytics: Analytics
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val jobs = mutableListOf<Job>()

    private val profile = spaceManager
        .observe()
        .flatMapLatest { config ->
            storelessSubscriptionContainer.subscribe(
                StoreSearchByIdsParams(
                    subscription = SELECT_SPACE_PROFILE_SUBSCRIPTION,
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.ICON_IMAGE,
                        Relations.ICON_EMOJI,
                        Relations.ICON_OPTION
                    ),
                    targets = listOf(config.profile)
                )
            ).map { results ->
                if (results.isNotEmpty())
                    results.first()
                else {
                    ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to config.profile
                        )
                    )
                }
            }
        }

    private val spaces: Flow<List<ObjectWrapper.Basic>> = storelessSubscriptionContainer.subscribe(
        StoreSearchParams(
            subscription = SELECT_SPACE_SUBSCRIPTION,
            keys = listOf(
                Relations.ID,
                Relations.TARGET_SPACE_ID,
                Relations.NAME,
                Relations.ICON_IMAGE,
                Relations.ICON_EMOJI,
                Relations.ICON_OPTION,
                Relations.SPACE_ACCOUNT_STATUS
            ),
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                ),
                DVFilter(
                    relation = Relations.SPACE_ACCOUNT_STATUS,
                    value = SpaceStatus.SPACE_DELETED.code.toDouble(),
                    condition = DVFilterCondition.NOT_EQUAL
                ),
                DVFilter(
                    relation = Relations.SPACE_LOCAL_STATUS,
                    value = SpaceStatus.OK.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                )
            ),
            sorts = listOf(
                DVSort(
                    relationKey = Relations.LAST_OPENED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true
                )
            )
        )
    ).catch {
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
                profile,
                spaceManager.observe()
            ) { spaces, profile, config ->
                buildList {
                    add(
                        SelectSpaceView.Profile(
                            name = profile.name.orEmpty(),
                            icon = profile.profileIcon(builder = urlBuilder)
                        )
                    )
                    addAll(
                        spaces.mapNotNull { wrapper ->
                            val space = wrapper.getValue<String>(Relations.TARGET_SPACE_ID)
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
                                        )
                                    )
                                )
                            } else {
                                null
                            }
                        }
                    )
                    val numberOfSpaces = count { view -> view is SelectSpaceView.Space }
                    if (numberOfSpaces < MAX_SPACE_COUNT) {
                        add(SelectSpaceView.Create)
                    }
                }
            }.collect { results ->
                views.value = results
            }
        }
    }

    fun onStart() {
        buildUI()
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
                spaceManager.set(view.space)
                saveCurrentSpace.async(SaveCurrentSpace.Params(SpaceId(view.space))).fold(
                    onFailure = {
                        Timber.e(it, "Error while saving current space in user settings")
                    },
                    onSuccess = {
                        resetDefaultObjectTypeForAppActions(
                            onExit = { commands.emit(Command.Dismiss) }
                        )
                    }
                )
            } else {
                commands.emit(Command.Dismiss)
            }
        }
    }

    private suspend fun resetDefaultObjectTypeForAppActions(onExit: suspend () -> Unit) {
        getDefaultObjectType.async(Unit).fold(
            onSuccess = { result ->
                val type = result.type
                appActionManager.setup(
                    AppActionManager.Action.CreateNew(
                        type = type,
                        name = result.name ?: "Object"
                    )
                )
                onExit()
            },
            onFailure = { onExit() }
        )
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
            storelessSubscriptionContainer.unsubscribe(
                subscriptions = listOf(
                    SELECT_SPACE_PROFILE_SUBSCRIPTION,
                    SELECT_SPACE_SUBSCRIPTION
                )
            )
        }
    }

    class Factory @Inject constructor(
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val spaceManager: SpaceManager,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val urlBuilder: UrlBuilder,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val appActionManager: AppActionManager,
        private val getDefaultObjectType: GetDefaultObjectType,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceManager = spaceManager,
            spaceGradientProvider = spaceGradientProvider,
            urlBuilder = urlBuilder,
            saveCurrentSpace = saveCurrentSpace,
            appActionManager = appActionManager,
            getDefaultObjectType = getDefaultObjectType,
            analytics = analytics
        ) as T
    }

    companion object {
        const val SELECT_SPACE_SUBSCRIPTION = "select_space_subscription.spaces"
        const val SELECT_SPACE_PROFILE_SUBSCRIPTION = "select_space_subscription.profile"
        const val MAX_SPACE_COUNT = 10
        const val SPACE_COUNT_EXCEEDED_ERROR = "Space max count exceeded. You cannot create more."
    }
}

data class WorkspaceView(
    val id: Id,
    val space: Id,
    val name: String?,
    val isSelected: Boolean = false,
    val icon: SpaceIconView
)

sealed class SelectSpaceView {
    data class Space(
        val view: WorkspaceView
    ) : SelectSpaceView()
    data class Profile(
        val name: String,
        val icon: ProfileIconView,
    ) : SelectSpaceView()
    object Create : SelectSpaceView()
}

sealed class Command {
    object CreateSpace : Command()
    object Dismiss : Command()
}
package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val saveCurrentSpace: SaveCurrentSpace
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>()

    val profile = spaceManager
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

    init {
        viewModelScope.launch {
            combine(
                storelessSubscriptionContainer.subscribe(
                    StoreSearchParams(
                        subscription = SELECT_SPACE_SUBSCRIPTION,
                        keys = listOf(
                            Relations.ID,
                            Relations.SPACE_ID,
                            Relations.NAME,
                            Relations.ICON_IMAGE,
                            Relations.ICON_EMOJI,
                            Relations.ICON_OPTION
                        ),
                        filters = listOf(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                value = ObjectType.Layout.SPACE.code.toDouble(),
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                    )
                ),
                profile,
                spaceManager.observe()
            ) { spaces, profile, config ->
                buildList {
                    add(
                        SelectSpaceView.Profile(
                            name = profile.name.orEmpty(),
                            icon = profile.profileIcon(
                                builder = urlBuilder,
                                gradientProvider = spaceGradientProvider
                            )
                        )
                    )
                    addAll(
                        spaces.mapNotNull { wrapper ->
                            val space = wrapper.getValue<String>(Relations.SPACE_ID)
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
                    add(SelectSpaceView.Create)
                }
            }.collect { results ->
                views.value = results
            }
        }
    }

    fun onSpaceClicked(view: WorkspaceView) {
        viewModelScope.launch {
            Timber.d("Setting space: $view")
            spaceManager.set(view.space)
            saveCurrentSpace.async(SaveCurrentSpace.Params(SpaceId(view.space))).fold(
                onFailure = {
                    Timber.e(it, "Error while saving current space in user settings")
                }
            )
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

    override fun onCleared() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                subscriptions = listOf(
                    SELECT_SPACE_PROFILE_SUBSCRIPTION,
                    SELECT_SPACE_PROFILE_SUBSCRIPTION
                )
            )
        }
        super.onCleared()
    }

    class Factory @Inject constructor(
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val spaceManager: SpaceManager,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val urlBuilder: UrlBuilder,
        private val saveCurrentSpace: SaveCurrentSpace
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceManager = spaceManager,
            spaceGradientProvider = spaceGradientProvider,
            urlBuilder = urlBuilder,
            saveCurrentSpace = saveCurrentSpace
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
}
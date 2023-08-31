package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSpaceViewModel @Inject constructor(
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val views = MutableStateFlow<List<WorkspaceView>>(emptyList())

    init {
        viewModelScope.launch {
            combine(
                storelessSubscriptionContainer.subscribe(
                    StoreSearchParams(
                        subscription = SELECT_SPACE_SUBSCRIPTION,
                        keys = listOf(Relations.ID, Relations.SPACE_ID, Relations.NAME),
                        filters = listOf(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                value = ObjectType.Layout.SPACE.code.toDouble(),
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                    )
                ),
                spaceManager.observe()
            ) { spaces, config ->
                spaces.mapNotNull { wrapper ->
                    val space = wrapper.getValue<String>(Relations.SPACE_ID)
                    if (space != null) {
                        WorkspaceView(
                            id = wrapper.id,
                            name = wrapper.name,
                            space = space,
                            isSelected = space == config.space
                        )
                    } else {
                        null
                    }
                }
            }.collect { results ->
                views.value = results
            }
        }
    }

    fun onSpaceClicked(view: WorkspaceView) {
        viewModelScope.launch {
            Timber.d("Setting space: ${view}")
            spaceManager.set(view.space)
        }
    }

    fun onCreateSpaceClicked() {
        // TODO
    }

    fun onStop() {
        // TODO unsubscribe
    }

    class Factory @Inject constructor(
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceManager = spaceManager
        ) as T
    }

    companion object {
        const val SELECT_SPACE_SUBSCRIPTION = "select_space_subscription"
    }
}

data class WorkspaceView(
    val id: Id,
    val space: Id,
    val name: String?,
    val isSelected: Boolean = false
)
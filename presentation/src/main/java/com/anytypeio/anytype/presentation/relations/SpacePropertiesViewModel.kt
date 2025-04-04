package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.relations.SpacePropertiesViewModel.VmParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class SpacePropertiesViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val userPermissionProvider: UserPermissionProvider,
    private val storeOfRelations: StoreOfRelations
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiItemsState =
        MutableStateFlow<UiSpacePropertiesScreenState>(UiSpacePropertiesScreenState.Empty)

    val commands = MutableSharedFlow<Command>()

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    data class VmParams(
        val spaceId: SpaceId
    )

    init {
        Timber.d("Space Properties ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfRelations.trackChanges()
                .collectLatest { event ->
                    when (event) {
                        StoreOfRelations.TrackedEvent.Change -> {
                            val allProperties =
                                storeOfRelations.getAll().map { relation ->
                                    UiSpacePropertyItem(
                                        id = relation.id,
                                        key = RelationKey(relation.key),
                                        name = relation.name.orEmpty(),
                                        format = relation.format
                                    )
                                }
                            uiItemsState.value = UiSpacePropertiesScreenState.Content(allProperties)
                        }

                        StoreOfRelations.TrackedEvent.Init -> {

                        }
                    }

                }
        }
    }

    fun onBackClicked() {
        // Handle back click
    }

    fun onCreateNewTypeClicked() {
        // Handle add new type click
    }

    sealed class Command {
        object Back : Command()
        object CreateNewProperty : Command()
    }
}

class SpacePropertiesVmFactory @Inject constructor(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val fieldParser: FieldParser,
    private val storeOfRelations: StoreOfRelations
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpacePropertiesViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser,
            storeOfRelations = storeOfRelations
        ) as T
}

sealed class UiSpacePropertiesScreenState {
    data object Empty : UiSpacePropertiesScreenState()
    data class Content(val items: List<UiSpacePropertyItem>) : UiSpacePropertiesScreenState()
}

data class UiSpacePropertyItem(
    val id: Id,
    val key: RelationKey,
    val name: String,
    val format: RelationFormat
)

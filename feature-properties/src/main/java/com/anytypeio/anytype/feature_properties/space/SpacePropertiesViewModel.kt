package com.anytypeio.anytype.feature_properties.space

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
import javax.inject.Inject
import kotlin.collections.map
import kotlin.collections.sortedBy
import kotlin.text.orEmpty
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

    val notAllowedPropertiesFormats = listOf(
        RelationFormat.UNDEFINED,
        RelationFormat.RELATIONS,
    )

    init {
        Timber.d("Space Properties ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfRelations.trackChanges()
                .collectLatest { event ->
                    val allProperties = storeOfRelations.getAll().map { relation ->
                        UiSpacePropertyItem(
                            id = relation.id,
                            key = RelationKey(relation.key),
                            name = relation.name.orEmpty(),
                            format = relation.format
                        )
                    }.sortedBy { it.name }

                    uiItemsState.value = UiSpacePropertiesScreenState(allProperties)
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateNewPropertyClicked() {
        if (permission.value?.isOwnerOrEditor() == true)  {
            viewModelScope.launch {
                commands.emit(Command.CreateNewProperty(vmParams.spaceId.id))
            }
        } else {
            viewModelScope.launch {
                commands.emit(Command.ShowToast("You don't have permission to create new properties"))
            }
        }
    }

    fun onPropertyClicked(item: UiSpacePropertyItem) {
        viewModelScope.launch {
            commands.emit(Command.OpenPropertyDetails(item.id))
        }
    }

    data class VmParams(
        val spaceId: SpaceId
    )

    sealed class Command {
        data object Back : Command()
        data class CreateNewProperty(val spaceId:Id) : Command()
        data class OpenPropertyDetails(val id: Id) : Command()
        data class ShowToast(val message: String) : Command()
    }
}

class SpacePropertiesVmFactory @Inject constructor(
    private val vmParams: SpacePropertiesViewModel.VmParams,
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

data class UiSpacePropertiesScreenState(
    val items: List<UiSpacePropertyItem>
) {
    companion object {
        val Empty = UiSpacePropertiesScreenState(emptyList())
    }
}

data class UiSpacePropertyItem(
    val id: Id,
    val key: RelationKey,
    val name: String,
    val format: RelationFormat
)

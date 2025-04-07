package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.types.SpaceTypesViewModel.VmParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceTypesViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val userPermissionProvider: UserPermissionProvider,
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiItemsState =
        MutableStateFlow<UiSpaceTypesScreenState>(UiSpaceTypesScreenState(emptyList()))
    val commands = MutableSharedFlow<Command>()

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    val notAllowedTypesLayouts = listOf(
        ObjectType.Layout.RELATION,
        ObjectType.Layout.RELATION_OPTION,
        ObjectType.Layout.DASHBOARD,
        ObjectType.Layout.SPACE,
        ObjectType.Layout.SPACE_VIEW,
        ObjectType.Layout.TAG,
        ObjectType.Layout.CHAT_DERIVED,
        ObjectType.Layout.DATE,
        ObjectType.Layout.OBJECT_TYPE,
    )

    init {
        Timber.d("Space Types ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfObjectTypes.trackChanges()
                .collectLatest { event ->
                    val allTypes =
                        storeOfObjectTypes.getAll().mapNotNull { objectType ->
                            val resolvedLayout = objectType.recommendedLayout ?: return@mapNotNull null
                            if (notAllowedTypesLayouts.contains(resolvedLayout)) {
                                return@mapNotNull null
                            } else {
                                objectType.toUiItem()
                            }
                        }
                            .sortedBy { it.name }
                    uiItemsState.value = UiSpaceTypesScreenState(allTypes)
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateNewTypeClicked() {
        if (permission.value?.isOwnerOrEditor() == true)  {
            viewModelScope.launch {
                commands.emit(Command.CreateNewType(vmParams.spaceId.id))
            }
        } else {
            viewModelScope.launch {
                commands.emit(Command.ShowToast("You don't have permission to create new type"))
            }
        }
    }

    fun onTypeClicked(type: UiSpaceTypeItem) {
        viewModelScope.launch {
            commands.emit(
                Command.OpenType(
                    id = type.id,
                    space = vmParams.spaceId.id
                )
            )
        }
    }

    sealed class Command {
        data object Back : Command()
        data class CreateNewType(val space: Id) : Command()
        data class OpenType(val id: Id, val space: Id) : Command()
        data class ShowToast(val message: String) : Command()
    }

    private fun ObjectWrapper.Type.toUiItem() = UiSpaceTypeItem(
        id = id,
        name = fieldParser.getObjectName(this),
        icon = this.objectIcon()

    )

    data class VmParams(
        val spaceId: SpaceId
    )
}

class SpaceTypesVmFactory @Inject constructor(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val fieldParser: FieldParser
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceTypesViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser
        ) as T
}

data class UiSpaceTypesScreenState(val items: List<UiSpaceTypeItem>)

data class UiSpaceTypeItem(
    val id: Id,
    val name: String,
    val icon: ObjectIcon.TypeIcon
)
package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
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
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiItemsState =
        MutableStateFlow(UiSpaceTypesScreenState(emptyList()))
    val commands = MutableSharedFlow<Command>()

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    companion object {
        /**
         * Base system layouts that are always excluded from the Object Types list,
         * regardless of space type.
         */
        val baseSystemLayouts = listOf(
            ObjectType.Layout.RELATION,
            ObjectType.Layout.RELATION_OPTION,
            ObjectType.Layout.DASHBOARD,
            ObjectType.Layout.SPACE,
            ObjectType.Layout.SPACE_VIEW,
            ObjectType.Layout.TAG,
            ObjectType.Layout.DATE,
            ObjectType.Layout.OBJECT_TYPE,
        )
    }

    init {
        Timber.d("Space Types ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfObjectTypes.trackChanges()
                .collectLatest { event ->
                    // Get space UX type to determine which layouts to exclude
                    val spaceView = spaceViewContainer.get(vmParams.spaceId)
                    val spaceUxType = spaceView?.spaceUxType
                    
                    // Build dynamic exclusion list based on space type
                    val excludedLayouts = buildList {
                        addAll(baseSystemLayouts)
                        // Only exclude CHAT_DERIVED and CHAT in Chat Spaces
                        if (spaceUxType == SpaceUxType.CHAT) {
                            add(ObjectType.Layout.CHAT_DERIVED)
                            add(ObjectType.Layout.CHAT)
                        }
                    }
                    
                    val allTypes =
                        storeOfObjectTypes.getAll().mapNotNull { objectType ->
                            val resolvedLayout =
                                objectType.recommendedLayout ?: return@mapNotNull null
                            if (excludedLayouts.contains(resolvedLayout) || objectType.isArchived == true) {
                                return@mapNotNull null
                            } else {
                                objectType
                            }
                        }
                    val (myTypes, systemTypes) = allTypes.partition {
                        !it.restrictions.contains(
                            ObjectRestriction.DELETE
                        )
                    }

                    val list = buildList {
                        add(UiSpaceTypeItem.Section.MyTypes())
                        addAll(myTypes.map { type ->
                            UiSpaceTypeItem.Type(
                                id = type.id,
                                name = fieldParser.getObjectName(type),
                                icon = type.objectIcon(),
                                isPossibleMoveToBin = true
                            )
                        }.sortedBy { it.name })
                        add(UiSpaceTypeItem.Section.System())
                        addAll(systemTypes.map { type ->
                            UiSpaceTypeItem.Type(
                                id = type.id,
                                name = fieldParser.getObjectName(type),
                                icon = type.objectIcon(),
                                isPossibleMoveToBin = false
                            )
                        }.sortedBy { it.name })
                    }

                    uiItemsState.value = UiSpaceTypesScreenState(list)
                }
        }
    }

    fun onMoveToBin(item: UiSpaceTypeItem.Type) {
        val typeId = item.id
        viewModelScope.launch {
            val params = SetObjectListIsArchived.Params(
                targets = listOf(typeId),
                isArchived = true
            )
            setObjectListIsArchived.async(params).fold(
                onSuccess = {
                    Timber.d("Object Type $typeId moved to bin")
                },
                onFailure = {
                    Timber.e(it, "Error while moving pbject type $typeId to bin")
                }
            )
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateNewTypeClicked() {
        if (permission.value?.isOwnerOrEditor() == true) {
            viewModelScope.launch {
                commands.emit(Command.CreateNewType(vmParams.spaceId.id))
            }
        } else {
            viewModelScope.launch {
                commands.emit(Command.ShowToast("You don't have permission to create new type"))
            }
        }
    }

    fun onTypeClicked(type: UiSpaceTypeItem.Type) {
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
    private val fieldParser: FieldParser,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceTypesViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser,
            setObjectListIsArchived = setObjectListIsArchived,
            spaceViewContainer = spaceViewContainer
        ) as T
}

data class UiSpaceTypesScreenState(val items: List<UiSpaceTypeItem>)


sealed class UiSpaceTypeItem {
    abstract val id: Id

    data class Type(
        override val id: Id,
        val name: String,
        val icon: ObjectIcon.TypeIcon,
        val isPossibleMoveToBin: Boolean = false
    ) : UiSpaceTypeItem()

    sealed class Section : UiSpaceTypeItem() {
        data class MyTypes(override val id: Id = "section_my_types") : Section()
        data class System(override val id: Id = "section_system") : Section()
    }
}
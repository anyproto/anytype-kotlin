package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
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

    val uiItemsState = MutableStateFlow<UiSpaceTypesScreenState>(UiSpaceTypesScreenState(emptyList()))
    val commands = MutableSharedFlow<Command>()

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.d("Space Types ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfObjectTypes.trackChanges()
                .collectLatest { event ->
                    val allTypes =
                        storeOfObjectTypes.getAll().map { objectType ->
                            objectType.toUiItem()
                        }
                    uiItemsState.value = UiSpaceTypesScreenState(allTypes)
                }
        }
    }

    fun onBackClicked() {
        // Handle back click
    }

    fun onCreateNewTypeClicked() {
        // Handle add new type click
    }

    sealed class Command{
        object Back : Command()
        object CreateNewType : Command()
    }

    private fun ObjectWrapper.Type.toUiItem() = UiSpaceTypeItem(
        id = id,
        name = fieldParser.getObjectName(this),
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
    //val icon: ObjectIcon
)
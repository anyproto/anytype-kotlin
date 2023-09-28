package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.spaces.AddObjectTypeToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager

class ObjectTypeChangeViewModelFactory(
    private val getObjectTypes: GetObjectTypes,
    private val addObjectTypeToSpace: AddObjectTypeToSpace,
    private val dispatchers: AppCoroutineDispatchers,
    private val spaceManager: SpaceManager,
    private val getDefaultPageType: GetDefaultPageType
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            getObjectTypes = getObjectTypes,
            addObjectTypeToSpace = addObjectTypeToSpace,
            dispatchers = dispatchers,
            spaceManager = spaceManager,
            getDefaultPageType = getDefaultPageType
        ) as T
    }
}
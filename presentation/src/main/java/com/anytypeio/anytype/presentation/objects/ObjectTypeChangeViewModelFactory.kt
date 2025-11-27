package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.AddObjectTypeToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager

class ObjectTypeChangeViewModelFactory(
    private val getObjectTypes: GetObjectTypes,
    private val addObjectTypeToSpace: AddObjectTypeToSpace,
    private val dispatchers: AppCoroutineDispatchers,
    private val spaceManager: SpaceManager,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val urlBuilder: UrlBuilder,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            getObjectTypes = getObjectTypes,
            addObjectTypeToSpace = addObjectTypeToSpace,
            dispatchers = dispatchers,
            spaceManager = spaceManager,
            getDefaultObjectType = getDefaultObjectType,
            urlBuilder = urlBuilder,
            spaceViews = spaceViews
        ) as T
    }
}
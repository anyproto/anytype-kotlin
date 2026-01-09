package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import javax.inject.Inject

class ObjectTypeChangeViewModelFactory @Inject constructor(
    private val vmParams: ObjectTypeChangeViewModel.VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val dispatchers: AppCoroutineDispatchers,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            dispatchers = dispatchers,
            spaceViews = spaceViews
        ) as T
    }
}
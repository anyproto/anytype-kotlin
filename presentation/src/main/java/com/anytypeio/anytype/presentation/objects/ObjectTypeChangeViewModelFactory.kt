package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace

class ObjectTypeChangeViewModelFactory(
    private val getObjectTypes: GetObjectTypes,
    private val addObjectToWorkspace: AddObjectToWorkspace,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectTypeChangeViewModel(
            getObjectTypes = getObjectTypes,
            addObjectToWorkspace = addObjectToWorkspace,
            dispatchers = dispatchers
        ) as T
    }
}
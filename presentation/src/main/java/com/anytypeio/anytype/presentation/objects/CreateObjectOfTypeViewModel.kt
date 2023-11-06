package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CreateObjectOfTypeViewModel(
    private val getObjectTypes: GetObjectTypes,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    private val query = MutableSharedFlow<String>().onStart {
        emit("")
    }

    init {
        viewModelScope.launch {
            query.flatMapLatest { query ->
                getObjectTypes.stream(
                    GetObjectTypes.Params(
                        sorts = emptyList(),
                        filters = buildList {
                            addAll(
                                ObjectSearchConstants.filterObjectTypeLibrary(
                                    space = spaceManager.get()
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.RECOMMENDED_LAYOUT,
                                    condition = DVFilterCondition.IN,
                                    value = SupportedLayouts.editorLayouts.map {
                                        it.code.toDouble()
                                    }
                                )
                            )
                        },
                        keys = ObjectSearchConstants.defaultKeysObjectType,
                        query = query
                    )
                ).map { result ->
                    // TODO
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val getObjectTypes: GetObjectTypes,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateObjectOfTypeViewModel(
            getObjectTypes = getObjectTypes,
            spaceManager = spaceManager
        ) as T
    }
}
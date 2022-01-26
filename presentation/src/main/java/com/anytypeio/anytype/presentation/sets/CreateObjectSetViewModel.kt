package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.*
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class CreateSetViewState {
    data class AddObjectType(val data: ArrayList<CreateObjectTypeView>) : CreateSetViewState()
}

@Deprecated("LEGACY SUSPECT")
class CreateObjectSetViewModel(
    private val createObjectSet: CreateObjectSet,
    private val getObjectTypes: GetObjectTypes,
    private val createObjectType: CreateObjectType
) : ViewModel() {

    private val _state = MutableLiveData<CreateSetViewState>()
    val state: LiveData<CreateSetViewState> = _state

    val objectTypeViews = MutableStateFlow<List<CreateObjectSetObjectTypeView>>(emptyList())

    private val objectTypes = MutableStateFlow<List<ObjectType>>(emptyList())

    private val typeLayouts = arrayListOf(
        CreateObjectTypeView(
            name = ObjectType.Layout.BASIC.name,
            layout = ObjectType.Layout.BASIC.ordinal,
            isSelected = true
        ),
        CreateObjectTypeView(
            name = ObjectType.Layout.PROFILE.name,
            layout = ObjectType.Layout.PROFILE.ordinal
        ),
        CreateObjectTypeView(
            name = ObjectType.Layout.TODO.name,
            layout = ObjectType.Layout.TODO.ordinal
        )
    )

    init {
        viewModelScope.launch {
            objectTypes.collect { types ->
                Timber.d("New types: $types")
                objectTypeViews.value = types.map { type ->
                    CreateObjectSetObjectTypeView(
                        url = type.url,
                        name = type.name,
                        emoji = type.emoji
                    )
                }
            }
        }
        viewModelScope.launch {
            val params = GetObjectTypes.Params(filterArchivedObjects = true)
            getObjectTypes(params).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { result ->
                    Timber.d("Result: $result")
                    objectTypes.value = result
                }
            )
        }
    }

    fun onCreateNewObjectType() {
        _state.value = CreateSetViewState.AddObjectType(typeLayouts)
    }

    fun onCreateObjectTypeClicked(type: CreateObjectTypeView, name: String) {
        viewModelScope.launch {
            createObjectType(
                CreateObjectType.Params(
                    name = name,
                    layout = type.layout
                )
            ).process(
                failure = { Timber.e(it, "Error while creating object type") },
                success = { result ->
                    val update = objectTypes.value.toMutableList().apply {
                        add(result)
                    }
                    objectTypes.value = update
                }
            )
        }
    }

    fun onObjectTypeSelected(type: Url, context: Id) {
        Timber.d("on object type selected: $type")
        viewModelScope.launch {
            createObjectSet(
                CreateObjectSet.Params(
                    ctx = context,
                    type = type
                )
            ).process(
                failure = { Timber.e(it, "Error while creating new set for type: $type") },
                success = {
                    // TODO navigate to created set
                }
            )
        }
    }

    class Factory(
        private val createObjectSet: CreateObjectSet,
        private val getObjectTypes: GetObjectTypes,
        private val createObjectType: CreateObjectType
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateObjectSetViewModel(
                getObjectTypes = getObjectTypes,
                createObjectSet = createObjectSet,
                createObjectType = createObjectType
            ) as T
        }
    }
}
package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class MoveToViewModel(
    urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics
) : ViewModel() {

    private val _viewState: MutableStateFlow<MoveToView> = MutableStateFlow(MoveToView.Init)
    val viewState: StateFlow<MoveToView> get() = _viewState

    val commands = MutableSharedFlow<Command>(replay = 0)
    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val types = MutableStateFlow(emptyList<ObjectType>())
    val objects = MutableStateFlow(emptyList<ObjectWrapper.Basic>())

    init {
        viewModelScope.launch {
            combine(objects, types) { listOfObjects, listOfTypes ->
                listOfObjects.map { obj ->
                    val targetType = listOfTypes.find { type ->
                        obj.type.contains(type.url)
                    }
                    DefaultObjectView(
                        id = obj.id,
                        name = obj.name.orEmpty(),
                        typeName = targetType?.name.orEmpty(),
                        typeLayout = obj.layout,
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        )
                    )
                }
            }.collectLatest { views ->
                if (views.isNotEmpty())
                    _viewState.value = MoveToView.Success(views)
                else
                    _viewState.value = MoveToView.NoResults(userInput.value)
            }
        }
    }

    fun onStart(ctx: Id) {
        getObjectTypes(ctx)
    }

    private fun startProcessingSearchQuery(ctx: Id) {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                val params = getSearchObjectsParams().copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { raw ->
                        setObjects(
                            ctx = ctx,
                            data = raw.map { ObjectWrapper.Basic(it) })
                    },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    private fun getObjectTypes(ctx: Id) {
        viewModelScope.launch {
            val params = GetObjectTypes.Params(filterArchivedObjects = true)
            getObjectTypes.invoke(params).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = {
                    types.value = it
                    startProcessingSearchQuery(ctx)
                }
            )
        }
    }

    private fun getSearchObjectsParams(): SearchObjects.Params {

        val filteredTypes = types.value
            .filter { objectType -> objectType.smartBlockTypes.contains(SmartBlockType.PAGE) }
            .map { objectType -> objectType.url }

        return SearchObjects.Params(
            limit = SEARCH_LIMIT,
            filters = ObjectSearchConstants.filterMoveTo(filteredTypes),
            sorts = ObjectSearchConstants.sortMoveTo,
            fulltext = EMPTY_QUERY
        )
    }

    fun onObjectClicked(target: Id, layout: ObjectType.Layout?) {
        viewModelScope.launch {
            commands.emit(Command.Move(target = target))
        }
    }

    fun onDialogCancelled() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    fun setObjects(ctx: Id, data: List<ObjectWrapper.Basic>) {
        objects.value = data
            .filter {
                SupportedLayouts.layouts.contains(it.layout) && it.id != ctx
            }
    }

    sealed class Command {
        object Exit : Command()
        data class Move(val target: Id) : Command()
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}
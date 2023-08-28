package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ui.TextInputDialogBottomBehaviorApplier
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class MoveToViewModel(
    urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val workspaceManager: WorkspaceManager
) : ViewModel(), TextInputDialogBottomBehaviorApplier.OnDialogCancelListener {

    private val _viewState: MutableStateFlow<MoveToView> = MutableStateFlow(MoveToView.Init)
    val viewState: StateFlow<MoveToView> get() = _viewState

    val commands = MutableStateFlow<Command>(Command.Init)
    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val types = MutableStateFlow<Resultat<List<ObjectWrapper.Type>>>(Resultat.Loading())
    val objects = MutableStateFlow<Resultat<List<ObjectWrapper.Basic>>>(Resultat.Loading())

    init {
        viewModelScope.launch {
            combine(objects, types) { listOfObjects, listOfTypes ->
                if (listOfObjects.isLoading || listOfTypes.isLoading) {
                    Resultat.Loading()
                } else {
                    Resultat.success(
                        listOfObjects.getOrThrow().toViews(
                            urlBuilder = urlBuilder,
                            objectTypes = listOfTypes.getOrThrow()
                        )
                    )
                }
            }.collectLatest { views ->
                if (views.isSuccess) {
                    with(views.getOrThrow()) {
                        if (this.isEmpty()) {
                            _viewState.value = MoveToView.NoResults(userInput.value)
                        } else {
                            _viewState.value = MoveToView.Success(this)
                        }
                    }
                } else {
                    _viewState.value = MoveToView.Loading
                }
            }
        }
    }

    fun onStart(ctx: Id) {
        Timber.d("onStart, ctx:[$ctx]")
        getObjectTypes(ctx)
    }

    private fun startProcessingSearchQuery(ctx: Id) {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                objects.value = Resultat.Loading()
                val params = getSearchObjectsParams(ctx).copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { objects ->
                        setObjects(
                            ctx = ctx,
                            data = objects
                        )
                    },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    private fun sendSearchResultEvent(id: String) {
        val value = _viewState.value
        if (value is MoveToView.Success) {
            val index = value.objects.indexOfFirst { it.id == id }
            if (index != -1) {
                viewModelScope.sendAnalyticsSearchResultEvent(
                    analytics = analytics,
                    pos = index + 1,
                    length = userInput.value.length
                )
            }
        }
    }

    private fun getObjectTypes(ctx: Id) {
        viewModelScope.launch {
            val params = GetObjectTypes.Params(
                sorts = emptyList(),
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterObjectTypeLibrary(
                            // TODO MULTISPACES fix object types
                            space = workspaceManager.getCurrentWorkspace()
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
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
            getObjectTypes.execute(params).fold(
                onFailure = { Timber.e(it, "Error while getting object types") },
                onSuccess = {
                    types.value = Resultat.success(it)
                    startProcessingSearchQuery(ctx)
                }
            )
        }
    }

    private suspend fun getSearchObjectsParams(ctx: Id): SearchObjects.Params {
        val filteredTypes = types.value.getOrDefault(emptyList()).map { objectType -> objectType.id }

        return SearchObjects.Params(
            limit = SEARCH_LIMIT,
            filters = ObjectSearchConstants.filterMoveTo(
                ctx = ctx,
                types = filteredTypes,
                workspaceId = workspaceManager.getCurrentWorkspace()
            ),
            sorts = ObjectSearchConstants.sortMoveTo,
            fulltext = EMPTY_QUERY,
            keys = ObjectSearchConstants.defaultKeys
        )
    }

    fun onObjectClicked(view: DefaultObjectView) {
        Timber.d("onObjectClicked, view:[$view]")
        commands.value = Command.Move(view = view)
        sendSearchResultEvent(view.id)
    }

    override fun onDialogCancelled() {
        commands.value = Command.Exit
    }

    fun onSearchTextChanged(searchText: String) {
        Timber.d("onSearchTextChanged, searchText:[$searchText]")
        userInput.value = searchText
    }

    private fun setObjects(ctx: Id, data: List<ObjectWrapper.Basic>) {
        objects.value = Resultat.success(data
            .filter {
                SupportedLayouts.layouts.contains(it.layout) && it.id != ctx
            })
    }

    sealed class Command {
        object Init : Command()
        object Exit : Command()
        data class Move(val view: DefaultObjectView) : Command()
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}
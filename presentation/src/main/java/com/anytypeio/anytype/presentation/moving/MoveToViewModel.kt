package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.TextInputDialogBottomBehaviorApplier
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.buildDeletedFilter
import com.anytypeio.anytype.presentation.search.buildLayoutFilter
import com.anytypeio.anytype.presentation.search.buildSpaceIdFilter
import com.anytypeio.anytype.presentation.search.buildTemplateFilter
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
    private val vmParams: VmParams,
    urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModel(), TextInputDialogBottomBehaviorApplier.OnDialogCancelListener,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val _viewState: MutableStateFlow<MoveToView> = MutableStateFlow(MoveToView.Init)
    val viewState: StateFlow<MoveToView> get() = _viewState

    val commands = MutableStateFlow<Command>(Command.Init)
    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val objects = MutableStateFlow<Resultat<List<ObjectWrapper.Basic>>>(Resultat.Loading())

    init {
        viewModelScope.launch {
            combine(objects, storeOfObjectTypes.trackChanges()) { listOfObjects, _ ->
                if (listOfObjects.isLoading) {
                    Resultat.Loading()
                } else {
                    Resultat.success(
                        listOfObjects.getOrThrow().toViews(
                            urlBuilder = urlBuilder,
                            fieldParser = fieldParser,
                            storeOfObjectTypes = storeOfObjectTypes
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
        startProcessingSearchQuery(ctx)
    }

    private fun startProcessingSearchQuery(ctx: Id) {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                objects.value = Resultat.Loading()
                val params = getSearchObjectsParams().copy(fulltext = query)
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
                viewModelScope.launch {
                    sendAnalyticsSearchResultEvent(
                        analytics = analytics,
                        pos = index + 1,
                        length = userInput.value.length,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                }
            }
        }
    }

    private fun getSearchObjectsParams(): SearchObjects.Params {
        val filters = buildList {
            addAll(buildDeletedFilter())
            add(buildLayoutFilter(layouts = DEFAULT_MOVE_LAYOUTS))
            add(buildSpaceIdFilter(listOf(vmParams.space.id)))
            add(buildTemplateFilter())
        }

        return SearchObjects.Params(
            space = vmParams.space,
            limit = SEARCH_LIMIT,
            filters = filters,
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
        objects.value = Resultat.success(
            data.filter { it.id != ctx })
    }

    data class VmParams(
        val space: SpaceId
    )

    sealed class Command {
        data object Init : Command()
        data object Exit : Command()
        data class Move(val view: DefaultObjectView) : Command()
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200

        val DEFAULT_MOVE_LAYOUTS = listOf(
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE
        )
    }
}
package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchQueryEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.toLinkToView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class LinkToObjectOrWebViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics
) : ViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val userInput = MutableStateFlow(ObjectSearchViewModel.EMPTY_QUERY)
    private val searchQuery = userInput
        .onEach { proceedWithNewFilter(it) }
        .debounce(300)
        .distinctUntilChanged()

    private val types = MutableStateFlow(emptyList<ObjectType>())
    private val objects = MutableStateFlow(emptyList<ObjectWrapper.Basic>())

    init {
        viewModelScope.launch {
            combine(objects, types) { listOfObjects, listOfTypes ->
                listOfObjects.toLinkToView(
                    urlBuilder = urlBuilder,
                    objectTypes = listOfTypes
                )
            }.collectLatest { views -> updateObjects(views) }
        }
    }

    fun onStart(uri: String) {
        if (uri.isNotEmpty()) {
            viewState.value = ViewState.SetFilter(uri)
        }
        getObjectTypes()
        startProcessingSearchQuery()
    }

    fun onClicked(item: LinkToItemView) {
        Timber.d("onClicked, item:[$item] ")
        viewModelScope.launch {
            when (item) {
                is LinkToItemView.CreateObject -> {
                    commands.emit(Command.CreateObject(item.title))
                }
                is LinkToItemView.Object -> {
                    onObjectClickEvent(item.position)
                    commands.emit(Command.SetObjectLink(item.id))
                }
                is LinkToItemView.WebItem -> {
                    commands.emit(Command.SetWebLink(item.url))
                }
                else -> Unit
            }
        }
    }

    private fun onObjectClickEvent(pos: Int) {
        viewModelScope.sendAnalyticsSearchResultEvent(
            analytics = analytics,
            pos = pos + 1,
            length = userInput.value.length
        )
    }

    private fun getObjectTypes() {
        viewModelScope.launch {
            val params = GetObjectTypes.Params(filterArchivedObjects = true)
            getObjectTypes.invoke(params).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { types.value = it }
            )
        }
    }

    private fun getSearchObjectsParams() = SearchObjects.Params(
        limit = ObjectSearchViewModel.SEARCH_LIMIT,
        filters = ObjectSearchConstants.filterLinkTo,
        sorts = ObjectSearchConstants.sortLinkTo,
        fulltext = ObjectSearchViewModel.EMPTY_QUERY
    )

    private fun startProcessingSearchQuery() {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                sendSearchQueryEvent(query.length)
                val params = getSearchObjectsParams().copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { objects -> setObjects(objects) },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    private fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.value = data.filter {
            SupportedLayouts.layouts.contains(it.layout)
        }
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    private fun proceedWithNewFilter(filter: String) {
        if (filter.isEmpty()) {
            onEmptyFilterState()
        } else {
            onNotEmptyFilterState(filter)
        }
    }

    private fun updateObjects(objects: List<LinkToItemView.Object>) {
        val state = viewState.value
        if (state is ViewState.Success) {
            val items = state.items.toMutableList()
            val objectsIndex = items.indexOfFirst { it is LinkToItemView.Object }
            if (objectsIndex != -1) {
                val sublist = items.subList(0, objectsIndex)
                viewState.value = ViewState.Success(sublist + objects)
            } else {
                viewState.value = ViewState.Success(items + objects)
            }
        } else {
            viewState.value =
                ViewState.Success(listOf(LinkToItemView.Subheading.Objects) + objects)
        }
    }

    private fun onEmptyFilterState() {
        val state = viewState.value
        val objects = mutableListOf<LinkToItemView>()
        if (state is ViewState.Success) {
            val items = state.items.toMutableList()
            val objectsIndex = items.indexOfFirst { it is LinkToItemView.Object }
            if (objectsIndex != -1) {
                objects.addAll(items.subList(objectsIndex, items.size))
            }
        }
        viewState.value = ViewState.Success(
            listOf(LinkToItemView.Subheading.Objects) + objects
        )
    }

    private fun onNotEmptyFilterState(filter: String) {
        val state = viewState.value
        val objects = mutableListOf<LinkToItemView>()
        if (state is ViewState.Success) {
            val items = state.items.toMutableList()
            val objectsIndex = items.indexOfFirst { it is LinkToItemView.Object }
            if (objectsIndex != -1) {
                objects.addAll(items.subList(objectsIndex, items.size))
            }
        }
        viewState.value = ViewState.Success(
            listOf(
                LinkToItemView.Subheading.Web,
                LinkToItemView.WebItem(filter),
                LinkToItemView.Subheading.Objects,
                LinkToItemView.CreateObject(filter),
            ) + objects
        )
    }

    private fun sendSearchQueryEvent(length: Int) {
        viewModelScope.sendAnalyticsSearchQueryEvent(
            analytics = analytics,
            route = EventsDictionary.Routes.searchMenu,
            length = length
        )
    }


    sealed class Command {
        object Exit : Command()
        data class SetWebLink(val url: String) : Command()
        data class SetObjectLink(val target: Id) : Command()
        data class CreateObject(val name: String) : Command()
    }

    sealed class ViewState {
        data class Success(
            val items: List<LinkToItemView>
        ) : ViewState()

        object Init : ViewState()
        data class SetFilter(val filter: String) : ViewState()
    }
}

sealed class LinkToItemView {
    sealed class Subheading : LinkToItemView() {
        object Objects : Subheading()
        object Web : Subheading()
    }

    data class WebItem(val url: String) : LinkToItemView()
    data class CreateObject(val title: String) : LinkToItemView()
    data class Object(
        val id: Id,
        val title: String?,
        val subtitle: String?,
        val type: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val position: Int = 0
    ) : LinkToItemView()
}
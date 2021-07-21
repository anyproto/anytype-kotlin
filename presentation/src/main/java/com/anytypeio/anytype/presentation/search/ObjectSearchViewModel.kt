package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.`object`.ObjectTypes
import com.anytypeio.anytype.domain.`object`.ObjectWrapper
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val getFlavourConfig: GetFlavourConfig
) : ViewStateViewModel<ObjectSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    private val types = MutableStateFlow(emptyList<ObjectType>())
    private val objects = MutableStateFlow(emptyList<ObjectWrapper.Basic>())

    private var links: MutableList<ObjectView> = mutableListOf()

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val supportedObjectTypes = if (getFlavourConfig.isDataViewEnabled())
        listOf(ObjectTypes.PAGE, ObjectTypes.SET)
    else
        listOf(ObjectTypes.PAGE)

    init {
        viewModelScope.launch {
            getObjectTypes.invoke(Unit).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { types.value = it }
            )
        }
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                searchObjects(
                    SearchObjects.Params(
                        fulltext = query,
                        limit = 200,
                        objectTypeFilter = supportedObjectTypes
                    )
                ).process(
                    success = { raw -> objects.value = raw.map { ObjectWrapper.Basic(it) } },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
        viewModelScope.launch {
            combine(objects, types) { listOfObjects, listOfTypes ->
                listOfObjects.map { obj ->
                    val targetType = listOfTypes.find { type ->
                        obj.type.contains(type.url)
                    }
                    ObjectView(
                        id = obj.id,
                        title = obj.name.orEmpty(),
                        subtitle = targetType?.name.orEmpty(),
                        emoji = obj.iconEmoji,
                        image = obj.iconImage?.let { hash -> urlBuilder.thumbnail(hash) },
                        layout = targetType?.layout
                    )
                }
            }.collectLatest { views ->
                stateData.postValue(ObjectSearchView.Success(views))
            }
        }
    }

    fun onViewCreated() {
        links.clear()
        stateData.postValue(ObjectSearchView.Init)
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    fun onOpenPageClicked(pageId: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DOCUMENT
        )
        navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = pageId)))
    }

    fun onBottomSheetHidden() {
        navigateToDesktop()
    }

    private fun navigateToDesktop() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DASHBOARD
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
    }
}
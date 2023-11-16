package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ui.TextInputDialogBottomBehaviorApplier
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.toViews
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
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

open class ObjectSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : ViewStateViewModel<ObjectSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    TextInputDialogBottomBehaviorApplier.OnDialogCancelListener {

    private val jobs = mutableListOf<Job>()

    protected val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
        }

    protected val types = MutableStateFlow<Resultat<List<ObjectWrapper.Type>>>(Resultat.Loading())
    protected val objects = MutableStateFlow<Resultat<List<ObjectWrapper.Basic>>>(Resultat.Loading())

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private var eventRoute = ""

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
            }.collectLatest { result ->
                resolveViews(result)
            }
        }
    }

    protected open fun resolveViews(result: Resultat<List<DefaultObjectView>>) {
        if (result.isSuccess) {
            with(result.getOrThrow()) {
                if (this.isEmpty()) {
                    stateData.postValue(ObjectSearchView.NoResults(userInput.value))
                } else {
                    if (userInput.value.isEmpty()) {
                        val items =
                            mutableListOf<DefaultSearchItem>(ObjectSearchSection.RecentlyOpened)
                        items.addAll(this)
                        stateData.postValue(ObjectSearchView.Success(items))
                    } else {
                        stateData.postValue(ObjectSearchView.Success(this))
                    }
                }
            }
        } else {
            stateData.postValue(ObjectSearchView.Loading)
        }
    }

    fun onStart(route: String, ignore: Id? = null) {
        eventRoute = route
        getObjectTypes()
        startProcessingSearchQuery(ignore)
    }

    fun onStop() {
        jobs.cancel()
    }

    protected fun getObjectTypes() {
        jobs += viewModelScope.launch {
            val params = GetObjectTypes.Params(
                sorts = emptyList(),
                filters = ObjectSearchConstants.filterTypes(
                    spaceId = spaceManager.get()
                ),
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
            getObjectTypes.async(params).fold(
                onFailure = { Timber.e(it, "Error while getting object types") },
                onSuccess = {
                    types.value = Resultat.success(it)
                }
            )
        }
    }

    protected fun startProcessingSearchQuery(ignore: Id?) {
        jobs += viewModelScope.launch {
            searchQuery.collectLatest { query ->
                objects.emit(Resultat.Loading())
                val params = getSearchObjectsParams(ignore).copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { objects -> setObjects(objects) },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    open suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.emit(Resultat.success(data))
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    open fun onObjectClicked(view: DefaultObjectView) {
        val target = view.id
        sendSearchResultEvent(target)
        when (view.layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.BOOKMARK -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = target)))
            }
            ObjectType.Layout.PROFILE -> {
                val obj = objects
                    .value
                    .getOrNull()
                    ?.find { obj -> obj.id == view.id }
                val identity = obj?.getValue<Id>(Relations.IDENTITY_PROFILE_LINK)
                if (identity != null) {
                    navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = identity)))
                } else {
                    navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = target)))
                }
            }
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchObjectSet(target = target)))
            }
            else -> {
                Timber.e("Unexpected layout: ${view.layout}")
            }
        }
    }

    protected fun sendSearchResultEvent(id: String) {
        val value = state.value
        if (value is ObjectSearchView.Success) {
            val index = value.objects.indexOfFirst { (it as? DefaultObjectView)?.id == id }
            if (index != -1) {
                viewModelScope.sendAnalyticsSearchResultEvent(
                    analytics = analytics,
                    pos = index + 1,
                    length = userInput.value.length
                )
            }
        }
    }

    open suspend fun getSearchObjectsParams(ignore: Id?) = SearchObjects.Params(
        limit = SEARCH_LIMIT,
        filters = ObjectSearchConstants.filterSearchObjects(
            spaces = buildList {
                val config = spaceManager.getConfig()
                if (config != null) {
                    add(config.space)
                    add(config.techSpace)
                } else {
                    add(spaceManager.get())
                }
            }
        ),
        sorts = ObjectSearchConstants.sortsSearchObjects,
        fulltext = EMPTY_QUERY,
        keys = buildList {
            addAll(ObjectSearchConstants.defaultKeys)
            add(Relations.IDENTITY_PROFILE_LINK)
        }
    )

    override fun onDialogCancelled() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}
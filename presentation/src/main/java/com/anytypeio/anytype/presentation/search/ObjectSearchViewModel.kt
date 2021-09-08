package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

open class ObjectSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics
) : ViewStateViewModel<ObjectSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val userInput = MutableStateFlow(EMPTY_QUERY)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    protected val types = MutableStateFlow(emptyList<ObjectType>())
    protected val objects = MutableStateFlow(emptyList<ObjectWrapper.Basic>())

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val supportedObjectTypes = listOf(ObjectTypeConst.PAGE, ObjectTypeConst.SET)

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
                    stateData.postValue(ObjectSearchView.Success(views))
                else
                    stateData.postValue(ObjectSearchView.NoResults(userInput.value))
            }
        }
    }

    fun onStart() {
        getObjectTypes()
        startProcessingSearchQuery()
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

    open fun getSearchObjectsParams(): SearchObjects.Params {
        val filters = listOf(
            DVFilter(
                condition = DVFilterCondition.EQUAL,
                value = false,
                relationKey = Relations.IS_ARCHIVED,
                operator = DVFilterOperator.AND
            ),
            DVFilter(
                relationKey = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_OPENED_DATE,
                type = DVSortType.DESC
            )
        )
        return SearchObjects.Params(
            limit = SEARCH_LIMIT,
            objectTypeFilter = supportedObjectTypes,
            filters = filters,
            sorts = sorts,
            fulltext = EMPTY_QUERY
        )
    }

    private fun startProcessingSearchQuery() {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                val params = getSearchObjectsParams().copy(fulltext = query)
                searchObjects(params = params).process(
                    success = { raw -> setObjects(raw.map { ObjectWrapper.Basic(it) }) },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    open suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.value = data
    }

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    open fun onObjectClicked(target: Id, layout: ObjectType.Layout?) {
        when (layout) {
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.FILE -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = target)))
            }
            ObjectType.Layout.SET -> {
                navigate(EventWrapper(AppNavigation.Command.LaunchObjectSet(target = target)))
            }
            else -> {
                Timber.e("Unexpected layout: $layout")
            }
        }
    }

    open fun onDialogCancelled() {
        navigateToDesktop()
    }

    private fun navigateToDesktop() {
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}
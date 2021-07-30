package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.core_models.ObjectTypeConst
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.`object`.ObjectIcon
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.relations.addIsHiddenFilter
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

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val supportedObjectTypes = if (getFlavourConfig.isDataViewEnabled())
        listOf(ObjectTypeConst.PAGE, ObjectTypeConst.SET)
    else
        listOf(ObjectTypeConst.PAGE)

    init {
        viewModelScope.launch {
            val params = GetObjectTypes.Params(filterArchivedObjects = true)
            getObjectTypes.invoke(params).process(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { types.value = it }
            )
        }
        val filters = listOf<DVFilter>().addIsHiddenFilter()
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                searchObjects(
                    SearchObjects.Params(
                        fulltext = query,
                        limit = SEARCH_LIMIT,
                        objectTypeFilter = supportedObjectTypes,
                        filters = filters
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

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    fun onObjectClicked(target: Id, layout: ObjectType.Layout?) {
        when(layout) {
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

    fun onBottomSheetHidden() {
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
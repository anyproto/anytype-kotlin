package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.`object`.ObjectTypes
import com.anytypeio.anytype.domain.`object`.ObjectWrapper
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.`object`.ObjectIcon
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
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
                        limit = SEARCH_LIMIT,
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
                    var icon : ObjectIcon = ObjectIcon.None
                    when(targetType?.layout) {
                        ObjectType.Layout.BASIC -> {
                            val img = obj.iconImage
                            val emoji = obj.iconEmoji
                            if (!img.isNullOrBlank()) {
                                icon = ObjectIcon.Basic.Image(hash = urlBuilder.thumbnail(img))
                            } else if (!emoji.isNullOrBlank()) {
                                icon = ObjectIcon.Basic.Emoji(unicode = emoji)
                            }
                        }
                        ObjectType.Layout.PROFILE -> {
                            val img = obj.iconImage
                            icon = if (!img.isNullOrBlank()) {
                                ObjectIcon.Profile.Image(hash = urlBuilder.thumbnail(img))
                            } else {
                                ObjectIcon.Profile.Avatar(name = obj.name.orEmpty())
                            }
                        }
                        ObjectType.Layout.TODO -> {
                            icon = ObjectIcon.Task(isChecked = obj.done ?: false)
                        }
                        ObjectType.Layout.SET -> {
                            val img = obj.iconImage
                            val emoji = obj.iconEmoji
                            if (!img.isNullOrBlank()) {
                                icon = ObjectIcon.Basic.Image(hash = urlBuilder.thumbnail(img))
                            } else if (!emoji.isNullOrBlank()) {
                                icon = ObjectIcon.Basic.Emoji(unicode = emoji)
                            }
                        }
                        else -> {}
                    }
                    DefaultObjectView(
                        id = obj.id,
                        name = obj.name.orEmpty(),
                        typeName = targetType?.name.orEmpty(),
                        typeLayout = targetType?.layout,
                        icon = icon
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DASHBOARD
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    companion object {
        const val EMPTY_QUERY = ""
        const val DEBOUNCE_DURATION = 300L
        const val SEARCH_LIMIT = 200
    }
}
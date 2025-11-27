package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.widgets.GetSuggestedWidgetTypes
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendChangeWidgetSourceEvent
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchSection
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import com.anytypeio.anytype.presentation.widgets.source.SuggestWidgetObjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectWidgetSourceViewModel(
    private val vmParams: VmParams,
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<WidgetDispatchEvent>,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getSuggestedWidgetTypes: GetSuggestedWidgetTypes,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ObjectSearchViewModel(
    vmParams = vmParams,
    urlBuilder = urlBuilder,
    searchObjects = searchObjects,
    getObjectTypes = getObjectTypes,
    analytics = analytics,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
    fieldParser = fieldParser,
    storeOfObjectTypes = storeOfObjectTypes,
    spaceViews = spaceViews
) {

    private val suggested = MutableStateFlow<SuggestedWidgetsState>(SuggestedWidgetsState.Init)

    val isDismissed = MutableStateFlow(false)
    var config : Config = Config.None

    val viewState = flow {
        emitAll(
            combine(
                stateData.asFlow(),
                suggested.filterIsInstance<SuggestedWidgetsState.Default>(),
                spaceViews.observe(vmParams.space)
            ) { state, suggested, spaceView ->
                val hasChat = !spaceView.chatId.isNullOrEmpty()
                val isChatSpace = spaceView.spaceUxType == SpaceUxType.CHAT
                
                when(state) {
                    is ObjectSearchView.Success -> {
                        state.copy(
                            objects = buildList {
                                val query = userInput.value
                                addAll(
                                    resolveSuggestedResults(
                                        suggested = suggested,
                                        query = query,
                                        hasChat = hasChat,
                                        isChatSpace = isChatSpace
                                    )
                                )
                                // Widgets from existing objects
                                add(ObjectSearchSection.SelectWidgetSource.FromMyObjects)
                                addAll(state.objects)
                            }
                        )
                    }
                    is ObjectSearchView.NoResults -> {
                        val query = state.searchText
                        val result = buildList {
                            addAll(
                                resolveSuggestedResults(
                                    suggested = suggested,
                                    query = query,
                                    hasChat = hasChat,
                                    isChatSpace = isChatSpace
                                )
                            )
                        }
                        if (result.isNotEmpty()) {
                            ObjectSearchView.Success(result)
                        } else {
                            state
                        }
                    }
                    else -> state
                }
            }
        )
    }

    init {
        viewModelScope.launch {
            dispatcher.flow()
                .filterIsInstance<WidgetDispatchEvent.TypePicked>()
                .take(1)
                .collect {
                    isDismissed.value = true
                }
        }
    }

    private fun resolveSuggestedResults(
        suggested: SuggestedWidgetsState.Default,
        query: String,
        hasChat: Boolean,
        isChatSpace: Boolean
    ) = buildList {

        // Adding system widgets if matched by query

        val filteredSuggestedSystemSources = suggested.suggestedSystemSources.filter { source ->
            source.contains(query, ignoreCase = true)
        }
        if (filteredSuggestedSystemSources.isNotEmpty()) {
            add(ObjectSearchSection.SelectWidgetSource.System)
            with(filteredSuggestedSystemSources) {
                if (contains(BundledWidgetSourceIds.FAVORITE)) {
                    add(BundledWidgetSourceView.Favorites)
                }
                if (contains(BundledWidgetSourceIds.ALL_OBJECTS)) {
                    add(BundledWidgetSourceView.AllObjects)
                }
                // Chat widget can only be added if the space has chat AND is not a chat space itself
                if (contains(BundledWidgetSourceIds.CHAT) && hasChat && !isChatSpace) {
                    add(BundledWidgetSourceView.Chat)
                }
                if (contains(BundledWidgetSourceIds.RECENT)) {
                    add(BundledWidgetSourceView.Recent)
                }
                if (contains(BundledWidgetSourceIds.RECENT_LOCAL)) {
                    add(BundledWidgetSourceView.RecentLocal)
                }
                if (contains(BundledWidgetSourceIds.BIN)) {
                    add(BundledWidgetSourceView.Bin)
                }
            }
        }

        // Adding object type widgets (aka object type widgets) if matched by query

        val filteredSuggestedObjectTypes = suggested.suggestedObjectTypes.filter { type ->
            type.name.contains(query, ignoreCase = true)
        }

        if (filteredSuggestedObjectTypes.isNotEmpty()) {
            add(ObjectSearchSection.SelectWidgetSource.Suggested)
            addAll(filteredSuggestedObjectTypes)
        }
    }

    override fun resolveViews(result: Resultat<List<DefaultObjectView>>) {
        viewModelScope.launch {
            result.fold(
                onSuccess = { views ->
                    if (views.isEmpty()) {
                        stateData.postValue(ObjectSearchView.NoResults(userInput.value))
                    } else {
                        stateData.postValue(ObjectSearchView.Success(views))
                    }
                },
                onLoading = {
                    stateData.postValue(ObjectSearchView.Loading)
                },
                onFailure = {
                    Timber.e(it, "Error while selecting source for widget")
                }
            )
        }
    }

    fun onStartWithNewWidget(
        ctx: Id,
        target: Id?,
        isInEditMode: Boolean
    ) {
        Timber.d("onStart with picking source for new widget")
        config = Config.NewWidget(
            ctx = ctx,
            target = target,
            isInEditMode = isInEditMode
        )
        proceedWithSearchQuery(ctx)
    }

    fun onStartWithExistingWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: Int,
        isInEditMode: Boolean
    ) {
        Timber.d("onStart with picking source for an existing widget")
        config = Config.ExistingWidget(
            ctx = ctx,
            widget = widget,
            source = source,
            type = type,
            isInEditMode = isInEditMode
        )
        proceedWithSearchQuery(ctx)
    }

    private fun proceedWithSearchQuery(ctx: Id) {
        viewModelScope.launch {
            getSuggestedWidgetTypes.async(
                params = GetSuggestedWidgetTypes.Params(
                    space = vmParams.space,
                    objectTypeFilters = buildList {
                        add(
                            DVFilter(
                                relation = Relations.SPACE_ID,
                                condition = DVFilterCondition.EQUAL,
                                value = vmParams.space.id
                            )
                        )
                        addAll(ObjectSearchConstants.filterTypes())
                    },
                    objectTypeKeys = ObjectSearchConstants.defaultKeysObjectType,
                    ctx = ctx
                )
            ).onSuccess { result ->
                suggested.value = SuggestedWidgetsState.Default(
                    suggestedSystemSources = result.suggestedSystemSources
                        .filterNot { it == BundledWidgetSourceIds.CHAT },
                    suggestedObjectTypes = result.suggestedObjectTypes.map { type ->
                        SuggestWidgetObjectType(
                            id = type.id,
                            name = fieldParser.getObjectPluralName(type),
                            objectIcon = type.objectIcon()
                        )
                    }
                )
            }
        }
        getObjectTypes()
        startProcessingSearchQuery(null)
    }

    override suspend fun getSearchObjectsParams(ignore: Id?): SearchObjects.Params {
        val spaceUxType = spaceViews.get(vmParams.space)?.spaceUxType
        return super.getSearchObjectsParams(ignore).copy(
            filters = ObjectSearchConstants.filterSearchObjects(
                excludeTypes = true,
                spaceUxType = spaceUxType
            )
        )
    }

    fun onBundledWidgetSourceClicked(view: BundledWidgetSourceView) {
        Timber.d("onBundledWidgetSourceClicked, view:[$view]")
        when (val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked.Bundled(
                            source = view.id,
                            target = curr.target
                        )
                    ).also {
                        sendChangeWidgetSourceEvent(
                            analytics = analytics,
                            view = view,
                            isForNewWidget = true,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    if (
                        view is BundledWidgetSourceView.AllObjects
                        || view is BundledWidgetSourceView.Bin
                        || view is BundledWidgetSourceView.Chat
                    ) {
                        isDismissed.value = true
                    }
                }
            }
            is Config.ExistingWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourceChanged(
                            ctx = curr.ctx,
                            widget = curr.widget,
                            source = view.id,
                            type = curr.type
                        )
                    ).also {
                        sendChangeWidgetSourceEvent(
                            analytics = analytics,
                            view = view,
                            isForNewWidget = false,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    isDismissed.value = true
                }
            }
            Config.None -> {
                Timber.w("Missing config for widget source")
            }
        }
    }

    fun onSuggestedWidgetObjectTypeClicked(view: SuggestWidgetObjectType) {
        Timber.d("onSuggestedWidgetObjectTypeClicked, view:[$view]")
        when (val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked.Default(
                            source = view.id,
                            target = curr.target,
                            sourceLayout = ObjectType.Layout.OBJECT_TYPE.code
                        )
                    ).also {
                        // TODO send analytics
                    }
                }
            }
            is Config.ExistingWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourceChanged(
                            ctx = curr.ctx,
                            widget = curr.widget,
                            source = view.id,
                            type = curr.type
                        )
                    ).also {
                        // TODO send analytics
                    }
                    isDismissed.value = true
                }
            }
            Config.None -> {
                Timber.w("Missing config for widget source")
            }
        }
    }

    fun onCreateNewObjectClicked() {
        viewModelScope.launch {
            dispatcher.send(WidgetDispatchEvent.NewWithWidgetWithNewSource)
            isDismissed.value = true
        }
    }

    override fun onObjectClicked(view: DefaultObjectView) {
        Timber.d("onObjectClicked, view:[$view]")
        when(val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked.Default(
                            source = view.id,
                            sourceLayout = view.layout?.code ?: -1,
                            target = curr.target
                        )
                    ).also {
                        dispatchSelectCustomSourceAnalyticEvent(
                            view = view,
                            isForNewWidget = true,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    if (view.layout != null && WidgetConfig.isLinkOnlyLayout(view.layout.code)) {
                        isDismissed.value = true
                    }
                }
            }
            is Config.ExistingWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourceChanged(
                            ctx = curr.ctx,
                            widget = curr.widget,
                            source = view.id,
                            type = curr.type
                        )
                    ).also {
                        dispatchSelectCustomSourceAnalyticEvent(
                            view = view,
                            isForNewWidget = false,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    isDismissed.value = true
                }
            }
            is Config.None -> {
                // Do nothing.
            }
        }
    }

    private fun CoroutineScope.dispatchSelectCustomSourceAnalyticEvent(
        view: DefaultObjectView,
        isForNewWidget: Boolean,
        isInEditMode: Boolean
    ) {
        val sourceObjectType = types.value.getOrDefault(emptyList()).find { type ->
            type.id == view.type
        }
        if (sourceObjectType != null) {
            sendChangeWidgetSourceEvent(
                analytics = analytics,
                sourceObjectTypeId = sourceObjectType.sourceObject.orEmpty(),
                isCustomObjectType = sourceObjectType.sourceObject.isNullOrEmpty(),
                isForNewWidget = isForNewWidget,
                isInEditMode = isInEditMode
            )
        } else {
            Timber.e("Could not found type for analytics")
        }
    }

    class Factory(
        private val vmParams: VmParams,
        private val urlBuilder: UrlBuilder,
        private val searchObjects: SearchObjects,
        private val getObjectTypes: GetObjectTypes,
        private val analytics: Analytics,
        private val dispatcher: Dispatcher<WidgetDispatchEvent>,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val fieldParser: FieldParser,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val getSuggestedWidgetTypes: GetSuggestedWidgetTypes,
        private val spaceViews: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetSourceViewModel(
                vmParams = vmParams,
                urlBuilder = urlBuilder,
                searchObjects = searchObjects,
                analytics = analytics,
                getObjectTypes = getObjectTypes,
                dispatcher = dispatcher,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                getSuggestedWidgetTypes = getSuggestedWidgetTypes,
                spaceViews = spaceViews
            ) as T
        }
    }

    sealed class Config {
        data object None : Config()
        data class NewWidget(
            val ctx: Id,
            val target: Id?,
            val isInEditMode: Boolean
        ) : Config()
        data class ExistingWidget(
            val ctx: Id,
            val widget: Id,
            val source: Id,
            val type: Int,
            val isInEditMode: Boolean
        ) : Config()
    }

    sealed class SuggestedWidgetsState {
        data object Init : SuggestedWidgetsState()
        data class Default(
            val suggestedObjectTypes: List<SuggestWidgetObjectType>,
            val suggestedSystemSources: List<String>
        ) : SuggestedWidgetsState()
    }
}
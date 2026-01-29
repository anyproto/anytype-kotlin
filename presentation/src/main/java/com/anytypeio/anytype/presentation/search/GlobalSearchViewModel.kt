package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation.Format.CHECKBOX
import com.anytypeio.anytype.core_models.Relation.Format.DATE
import com.anytypeio.anytype.core_models.Relation.Format.EMAIL
import com.anytypeio.anytype.core_models.Relation.Format.EMOJI
import com.anytypeio.anytype.core_models.Relation.Format.FILE
import com.anytypeio.anytype.core_models.Relation.Format.LONG_TEXT
import com.anytypeio.anytype.core_models.Relation.Format.NUMBER
import com.anytypeio.anytype.core_models.Relation.Format.OBJECT
import com.anytypeio.anytype.core_models.Relation.Format.PHONE
import com.anytypeio.anytype.core_models.Relation.Format.RELATIONS
import com.anytypeio.anytype.core_models.Relation.Format.SHORT_TEXT
import com.anytypeio.anytype.core_models.Relation.Format.STATUS
import com.anytypeio.anytype.core_models.Relation.Format.TAG
import com.anytypeio.anytype.core_models.Relation.Format.UNDEFINED
import com.anytypeio.anytype.core_models.Relation.Format.URL
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.RestoreGlobalSearchHistory
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.domain.search.UpdateGlobalSearchHistory
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchBacklinksEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterObjectsByIds
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterSearchObjects
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class GlobalSearchViewModel @Inject constructor(
    private val vmParams: VmParams,
    private val searchWithMeta: SearchWithMeta,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storeOfRelations: StoreOfRelations,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val restoreGlobalSearchHistory: RestoreGlobalSearchHistory,
    private val updateGlobalSearchHistory: UpdateGlobalSearchHistory,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val userInput = MutableStateFlow("")
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    private val mode = MutableStateFlow<Mode>(Mode.Default)

    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Init())
    val state = _state.asStateFlow()

    init {
        Timber.d("GlobalSearchViewModel, init")
        proceedRestoreGlobalSearch(space = vmParams.space)
    }

    private fun proceedRestoreGlobalSearch(space: SpaceId) {
        Timber.d("restoreGlobalSearch, space $space")
        viewModelScope.launch {
            val params = RestoreGlobalSearchHistory.Params(spaceId = space)
            restoreGlobalSearchHistory.async(params = params).fold(
                onSuccess = { response ->
                    val globalSearchHistory = response.globalSearchHistory
                    Timber.d("restoreGlobalSearchHistory, onSuccess $globalSearchHistory")
                    userInput.value = globalSearchHistory?.query ?: EMPTY_STRING_VALUE
                    val relatedObjectId = globalSearchHistory?.relatedObject
                    if (!relatedObjectId.isNullOrEmpty()) {
                        proceedRelatedObjectSearch(
                            query = globalSearchHistory.query,
                            relatedObjectId = relatedObjectId
                        )
                    } else {
                        val initialState =
                            ViewState.Init(query = globalSearchHistory?.query ?: EMPTY_STRING_VALUE)
                        proceedWithInitialState(initialState)
                    }
                },
                onFailure = {
                    Timber.e(it, "restoreGlobalSearch, onFailure")
                    userInput.value = EMPTY_STRING_VALUE
                    proceedWithInitialState(ViewState.Init(query = EMPTY_STRING_VALUE))
                }
            )
        }
    }

    private suspend fun proceedRelatedObjectSearch(query: String, relatedObjectId: Id) {
        val params = SearchWithMeta.Params(
            relatedObjectId = relatedObjectId,
            command = Command.SearchWithMeta(
                limit = 1,
                keys = DEFAULT_KEYS,
                filters = filterObjectsByIds(
                    ids = listOf(relatedObjectId),
                    space = vmParams.space.id
                ),
                space = vmParams.space
            )
        )
        searchWithMeta.async(params).fold(
            onSuccess = { result ->
                Timber.d("proceedRelatedObjectSearch, onSuccess $result")
                val relatedGlobalSearchItemView = result.firstOrNull()?.view(
                    storeOfRelations = storeOfRelations,
                    storeOfObjectTypes = storeOfObjectTypes,
                    urlBuilder = urlBuilder,
                    fieldParser = fieldParser
                )
                if (relatedGlobalSearchItemView != null) {
                    mode.value = Mode.Related(target = relatedGlobalSearchItemView)
                    proceedWithInitialState(
                        ViewState.RelatedInit(
                            query = query,
                            target = relatedGlobalSearchItemView,
                            isLoading = false
                        )
                    )
                } else {
                    proceedWithInitialState(ViewState.Init(query = query))
                }
            },
            onFailure = {
                Timber.e(it, "proceedRelatedObjectSearch, onFailure")
                proceedWithInitialState(ViewState.Init(query = query))
            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun proceedWithInitialState(initial: ViewState) {
        combine(
            mode,
            searchQuery
        ) { mode, query ->
            mode to query
        }.flatMapLatest { (mode, query) ->
            when(mode) {
                is Mode.Default -> {
                    buildDefaultSearchFlow(query = query, space = vmParams.space)
                }
                is Mode.Related -> {
                    buildRelatedSearchFlow(query = query, mode = mode, space = vmParams.space)
                }
            }
        }.scan(
            initial = initial
        ) { curr, new ->
            when(new) {
                is ViewState.Default -> {
                    if (new.isLoading) {
                        new.copy(
                            views = curr.views
                        )
                    } else {
                        new
                    }
                }
                is ViewState.Related -> {
                    if (new.isLoading) {
                        new.copy(
                            views = curr.views
                        )
                    } else {
                        new
                    }
                }
                else -> new
            }
        }.collect { viewState ->
            _state.value = viewState
        }
    }

    private suspend fun buildRelatedSearchFlow(
        query: String,
        mode: Mode.Related,
        space: SpaceId
    ) = searchWithMeta
        .stream(
            relatedSearchFlowParams(
                query = query,
                links = mode.target.links,
                backlinks = mode.target.backlinks,
                space = space,
                relatedObjectId = mode.target.id
            )
        ).map { result ->
            when (result) {
                is Resultat.Failure -> {
                    ViewState.Related(
                        target = mode.target,
                        isLoading = false
                    )
                }

                is Resultat.Loading -> {
                    ViewState.Related(
                        target = mode.target,
                        isLoading = true
                    )
                }
                is Resultat.Success -> {
                    ViewState.Related(
                        target = mode.target,
                        views = result.value.mapNotNull {
                            it.view(
                                storeOfRelations = storeOfRelations,
                                storeOfObjectTypes = storeOfObjectTypes,
                                urlBuilder = urlBuilder,
                                fieldParser = fieldParser
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }

    private fun relatedSearchFlowParams(
        query: String,
        links: List<Id>,
        backlinks: List<Id>,
        space: SpaceId,
        relatedObjectId: Id?
    ): SearchWithMeta.Params {
        return SearchWithMeta.Params(
            saveSearch = true,
            relatedObjectId = relatedObjectId,
            command = Command.SearchWithMeta(
                space = space,
                query = query,
                limit = DEFAULT_SEARCH_LIMIT,
                offset = 0,
                keys = DEFAULT_KEYS,
                filters = buildList {
                    val spaceUxType = spaceViews.get(vmParams.space)?.spaceUxType
                    addAll(filterSearchObjects(spaceUxType = spaceUxType))
                    add(
                        DVFilter(
                            relation = Relations.ID,
                            value = buildSet {
                                addAll(links)
                                addAll(backlinks)
                            }.toList(),
                            condition = DVFilterCondition.IN
                        )
                    )
                },
                sorts = ObjectSearchConstants.sortsSearchObjects,
                withMetaRelationDetails = false,
                withMeta = false
            )
        )
    }

    private suspend fun buildDefaultSearchFlow(query: String, space: SpaceId) = searchWithMeta
        .stream(
            SearchWithMeta.Params(
                saveSearch = true,
                relatedObjectId = null,
                command = Command.SearchWithMeta(
                    query = query,
                    limit = DEFAULT_SEARCH_LIMIT,
                    offset = 0,
                    keys = DEFAULT_KEYS,
                    filters = buildList {
                        val spaceUxType = spaceViews.get(vmParams.space)?.spaceUxType
                        addAll(ObjectSearchConstants.filterSearchObjects(spaceUxType = spaceUxType))
                    },
                    sorts = ObjectSearchConstants.sortsSearchObjects,
                    withMetaRelationDetails = true,
                    withMeta = true,
                    space = space
                )
            )

        ).map { result ->
            when (result) {
                is Resultat.Failure -> {
                    ViewState.Default(
                        views = emptyList(),
                        isLoading = false
                    )
                }
                is Resultat.Loading -> {
                    ViewState.Default(
                        isLoading = true
                    )
                }
                is Resultat.Success -> {
                    ViewState.Default(
                        views = result.value.mapNotNull {
                            it.view(
                                storeOfRelations = storeOfRelations,
                                storeOfObjectTypes = storeOfObjectTypes,
                                urlBuilder = urlBuilder,
                                fieldParser = fieldParser
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }


    init {
        Timber.i("GlobalSearchViewModel, init")
    }

    fun onQueryChanged(query: String) {
        userInput.value = query
    }

    fun onObjectClicked(globalSearchItemView: GlobalSearchItemView) {
        Timber.d("onObjectClicked, globalSearchItemView $globalSearchItemView")
        viewModelScope.launch {
            navigation.emit(
                globalSearchItemView.obj.navigation()
            )
        }
        viewModelScope.launch {
            sendAnalyticsSearchResultEvent(
                analytics = analytics,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    fun onClearRelatedObjectClicked() {
        viewModelScope.launch {
            userInput.value = EMPTY_STRING_VALUE
            mode.value = Mode.Default
        }
    }

    fun onShowRelatedClicked(globalSearchItemView: GlobalSearchItemView) {
        viewModelScope.launch {
            userInput.value = EMPTY_STRING_VALUE
            mode.value = Mode.Related(globalSearchItemView)
            proceedUpdateGlobalSearch(
                query = EMPTY_STRING_VALUE,
                relatedObjectId = globalSearchItemView.id
            )
        }
        viewModelScope.launch {
            sendAnalyticsSearchBacklinksEvent(
                analytics = analytics,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    private suspend fun proceedUpdateGlobalSearch(query: String, relatedObjectId: Id?) {
        val params = UpdateGlobalSearchHistory.Params(
            spaceId = vmParams.space,
            query = query,
            relatedObjectId = relatedObjectId
        )
        updateGlobalSearchHistory.async(params).fold(
            onSuccess = {
                Timber.i("updateGlobalSearch, onSuccess")
            },
            onFailure = {
                Timber.e(it, "updateGlobalSearch, onFailure")
            }
        )
    }

    data class VmParams(val space: SpaceId)

    class Factory @Inject constructor(
        private val vmParams: VmParams,
        private val searchWithMeta: SearchWithMeta,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val storeOfRelations: StoreOfRelations,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val restoreGlobalSearchHistory: RestoreGlobalSearchHistory,
        private val updateGlobalSearchHistory: UpdateGlobalSearchHistory,
        private val fieldParser: FieldParser,
        private val spaceViews: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalSearchViewModel(
                vmParams = vmParams,
                searchWithMeta = searchWithMeta,
                storeOfObjectTypes = storeOfObjectTypes,
                storeOfRelations = storeOfRelations,
                urlBuilder = urlBuilder,
                analytics = analytics,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                restoreGlobalSearchHistory = restoreGlobalSearchHistory,
                updateGlobalSearchHistory = updateGlobalSearchHistory,
                fieldParser = fieldParser,
                spaceViews = spaceViews
            ) as T
        }
    }

    /**
     * Search interaction mode.
     */
    private sealed class Mode {
        /**
         * Default search mode. Searching for objects in a given space.
         */
        data object Default : Mode()
        /**
         * Searching for objects related to the given object, i.e. searching for (back)links.
         */
        data class Related(val target: GlobalSearchItemView) : Mode()
    }

    sealed class ViewState {

        abstract val views: List<GlobalSearchItemView>
        abstract val isLoading: Boolean

        data class Init(
            val query: String = EMPTY_STRING_VALUE
        ): ViewState() {
            override val views: List<GlobalSearchItemView> = emptyList()
            override val isLoading: Boolean = false
        }

        data class Default (
            override val views: List<GlobalSearchItemView> = emptyList(),
            override val isLoading: Boolean
        ): ViewState()

        data class Related (
            val target: GlobalSearchItemView,
            override val views: List<GlobalSearchItemView> = emptyList(),
            override val isLoading: Boolean
        ): ViewState()

        //ToDo: remove this state, and make Related sealed class
        data class RelatedInit (
            val query: String = EMPTY_STRING_VALUE,
            val target: GlobalSearchItemView,
            override val views: List<GlobalSearchItemView> = emptyList(),
            override val isLoading: Boolean
        ): ViewState()

        fun isEmptyState() : Boolean {
            return this !is Init && !this.isLoading && views.isEmpty()
        }
    }

    companion object {
        const val DEFAULT_DEBOUNCE_DURATION = 300L
        const val DEFAULT_SEARCH_LIMIT = 50
        val DEFAULT_KEYS = buildList {
            addAll(ObjectSearchConstants.defaultKeys)
            add(Relations.LINKS)
            add(Relations.BACKLINKS)
        }
    }
}

/**
 * @property [title] object title
 * @property [type] type screen name
 */
data class GlobalSearchItemView(
    val id: Id,
    val obj: ObjectWrapper.Basic,
    val icon: ObjectIcon,
    val space: SpaceId,
    val layout: ObjectType.Layout,
    val title: String,
    val type: String,
    val meta: Meta,
    val links: List<Id> = emptyList(),
    val backlinks: List<Id> = emptyList(),
    val pinned: Boolean = false,
    val nameMeta: NameMeta? = null
) {
    data class NameMeta(
        val name: String,
        val highlights: List<IntRange> = emptyList()
    )

    sealed class Meta {
        data object None : Meta()
        data class Default(
            val name: String,
            val value: String,
            val highlights: List<IntRange> = emptyList()
        ): Meta()
        data class Status(
            val name: String,
            val value: String,
            val color: ThemeColor
        ): Meta()
        data class Tag(
            val name: String,
            val value: String,
            val color: ThemeColor
        ): Meta()
        data class Block(
            val snippet: String,
            val highlights: List<IntRange> = emptyList()
        ): Meta()
    }
}

suspend fun Command.SearchWithMeta.Result.view(
    storeOfObjectTypes: StoreOfObjectTypes,
    storeOfRelations: StoreOfRelations,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser
) : GlobalSearchItemView? {
    if (wrapper.spaceId == null) return null
    if (wrapper.layout == null) return null
    val (_, typeName) = fieldParser.getObjectTypeIdAndName(
        objectWrapper = wrapper,
        types = storeOfObjectTypes.getAll()
    )
    val meta = metas.firstOrNull()
    return GlobalSearchItemView(
        id = obj,
        obj = wrapper,
        icon = wrapper.objectIcon(
            builder = urlBuilder,
            objType = storeOfObjectTypes.getTypeOfObject(wrapper)
        ),
        links = wrapper.links,
        backlinks = wrapper.backlinks,
        space = SpaceId(requireNotNull(wrapper.spaceId)),
        layout = requireNotNull(wrapper.layout),
        title = fieldParser.getObjectNameOrPluralsForTypes(wrapper),
        type =  typeName,
        meta = if (meta != null) {
            when(val source = meta.source) {
                is Command.SearchWithMeta.Result.Meta.Source.Block -> {
                    GlobalSearchItemView.Meta.Block(
                        snippet = meta.highlight.orEmpty(),
                        highlights = meta.ranges,
                    )
                }
                is Command.SearchWithMeta.Result.Meta.Source.Relation -> {
                    val relation = storeOfRelations.getByKey(source.key)
                    val dep = meta.dependencies.firstOrNull()
                    if (relation != null && relation.map.isNotEmpty() && relation.key != Relations.TYPE && dep != null && dep.map.isNotEmpty()) {
                        when(relation.format) {
                            SHORT_TEXT, LONG_TEXT, URL, EMAIL, PHONE -> {
                                GlobalSearchItemView.Meta.Default(
                                    name = relation.name.orEmpty(),
                                    value = meta.highlight.orEmpty(),
                                    highlights = meta.ranges
                                )
                            }
                            NUMBER -> {
                                GlobalSearchItemView.Meta.Default(
                                    name = relation.name.orEmpty(),
                                    value = meta.highlight.orEmpty(),
                                    highlights = emptyList()
                                )
                            }
                            STATUS -> {
                                val value  = ObjectWrapper.Option(dep.map)
                                GlobalSearchItemView.Meta.Status(
                                    name = relation.name.orEmpty(),
                                    color = ThemeColor.entries.find {
                                        it.code == value.color
                                    } ?: ThemeColor.DEFAULT,
                                    value = value.name.orEmpty()
                                )
                            }
                            TAG -> {
                                val value  = ObjectWrapper.Option(dep.map)
                                GlobalSearchItemView.Meta.Tag(
                                    name = relation.name.orEmpty(),
                                    color = ThemeColor.entries.find {
                                        it.code == value.color
                                    } ?: ThemeColor.DEFAULT,
                                    value = value.name.orEmpty()
                                )
                            }
                            FILE -> GlobalSearchItemView.Meta.Default(
                                name = relation.name.orEmpty(),
                                value = fieldParser.getObjectName(dep),
                                highlights = emptyList()
                            )
                            OBJECT -> {
                                val value  = ObjectWrapper.Basic(dep.map)
                                GlobalSearchItemView.Meta.Default(
                                    name = relation.name.orEmpty(),
                                    value = value.name.orEmpty(),
                                    highlights = emptyList()
                                )
                            }
                            DATE -> GlobalSearchItemView.Meta.None
                            EMOJI -> GlobalSearchItemView.Meta.None
                            CHECKBOX -> GlobalSearchItemView.Meta.None
                            RELATIONS -> GlobalSearchItemView.Meta.None
                            UNDEFINED -> GlobalSearchItemView.Meta.None
                        }
                    } else {
                        GlobalSearchItemView.Meta.None
                    }
                }
            }
        } else {
            GlobalSearchItemView.Meta.None
        },
        nameMeta = metas.getNameMeta()
    )
}

private fun List<Command.SearchWithMeta.Result.Meta>.getNameMeta(): GlobalSearchItemView.NameMeta? {
    val meta =
        firstOrNull { (it.source as? Command.SearchWithMeta.Result.Meta.Source.Relation)?.key == Relations.NAME }
    return if (meta != null) {
        GlobalSearchItemView.NameMeta(
            name = meta.highlight.orEmpty(),
            highlights = meta.ranges
        )
    } else {
        null
    }
}
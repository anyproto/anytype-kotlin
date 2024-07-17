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
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchBacklinksEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class GlobalSearchViewModel(
    private val searchWithMeta: SearchWithMeta,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storeOfRelations: StoreOfRelations,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val userInput = MutableStateFlow(EMPTY_STRING_VALUE)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    private val mode = MutableStateFlow<Mode>(Mode.Default)

    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    val state = combine(
        mode,
        searchQuery
    ) { mode, query ->
        mode to query
    }.flatMapLatest { (mode, query) ->
        when(mode) {
            is Mode.Default -> {
                buildDefaultSearchFlow(query)
            }
            is Mode.Related -> {
                buildRelatedSearchFlow(query, mode)
            }
        }
    }.scan<ViewState, ViewState>(initial = ViewState.Init) { curr, new ->
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ViewState.Init
    )

    private suspend fun buildRelatedSearchFlow(
        query: String,
        mode: Mode.Related
    ) = searchWithMeta
        .stream(
            Command.SearchWithMeta(
                query = query,
                limit = DEFAULT_SEARCH_LIMIT,
                offset = 0,
                keys = DEFAULT_KEYS,
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterSearchObjects(
                            spaces = listOf(spaceManager.get())
                        )
                    )
                    add(
                        DVFilter(
                            relation = Relations.ID,
                            value = buildSet {
                                addAll(mode.target.links)
                                addAll(mode.target.backlinks)
                            }.toList(),
                            condition = DVFilterCondition.IN
                        )
                    )
                },
                sorts = ObjectSearchConstants.sortsSearchObjects,
                withMetaRelationDetails = false,
                withMeta = false
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
                                urlBuilder = urlBuilder
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }

    private suspend fun buildDefaultSearchFlow(query: String) = searchWithMeta
        .stream(
            Command.SearchWithMeta(
                query = query,
                limit = DEFAULT_SEARCH_LIMIT,
                offset = 0,
                keys = DEFAULT_KEYS,
                filters = ObjectSearchConstants.filterSearchObjects(
                    // TODO add tech space?
                    spaces = listOf(spaceManager.get())
                ),
                sorts = ObjectSearchConstants.sortsSearchObjects,
                withMetaRelationDetails = true,
                withMeta = true
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
                                urlBuilder = urlBuilder
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }

    fun onQueryChanged(query: String) {
        userInput.value = query
    }

    fun onObjectClicked(globalSearchItemView: GlobalSearchItemView) {
        viewModelScope.launch {
            navigation.emit(
                globalSearchItemView.layout.navigation(
                    target = globalSearchItemView.id,
                    space = globalSearchItemView.space.id
                )
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
        }
        viewModelScope.launch {
            sendAnalyticsSearchBacklinksEvent(
                analytics = analytics,
                spaceParams = provideParams(spaceManager.get())
            )
        }
    }

    class Factory @Inject constructor(
        private val searchWithMeta: SearchWithMeta,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val storeOfRelations: StoreOfRelations,
        private val spaceManager: SpaceManager,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalSearchViewModel(
                searchWithMeta = searchWithMeta,
                storeOfObjectTypes = storeOfObjectTypes,
                storeOfRelations = storeOfRelations,
                spaceManager = spaceManager,
                urlBuilder = urlBuilder,
                analytics = analytics,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
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

        data object Init: ViewState() {
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
data class  GlobalSearchItemView(
    val id: Id,
    val icon: ObjectIcon,
    val space: SpaceId,
    val layout: ObjectType.Layout,
    val title: String,
    val type: String,
    val meta: Meta,
    val links: List<Id> = emptyList(),
    val backlinks: List<Id> = emptyList(),
    val pinned: Boolean = false
) {
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
    urlBuilder: UrlBuilder
) : GlobalSearchItemView? {
    if (wrapper.spaceId == null) return null
    if (wrapper.layout == null) return null
    val type = wrapper.type.firstOrNull()
    val meta = metas.firstOrNull()
    return GlobalSearchItemView(
        id = obj,
        icon = ObjectIcon.from(
            obj = wrapper,
            layout = wrapper.layout,
            builder = urlBuilder
        ),
        links = wrapper.links,
        backlinks = wrapper.backlinks,
        space = SpaceId(requireNotNull(wrapper.spaceId)),
        layout = requireNotNull(wrapper.layout),
        title = wrapper.getProperName(),
        type =  if (type != null) {
            storeOfObjectTypes.get(type)?.name.orEmpty()
        } else {
            EMPTY_STRING_VALUE
        },
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
                    if (relation != null && relation.map.isNotEmpty() && dep != null && dep.map.isNotEmpty()) {
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
                                value = dep.getProperName(),
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
        }
    )
}
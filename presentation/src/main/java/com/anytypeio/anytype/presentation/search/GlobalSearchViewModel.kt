package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
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
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.objects.getProperName
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class GlobalSearchViewModel(
    private val searchWithMeta: SearchWithMeta,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storeOfRelations: StoreOfRelations,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val userInput = MutableStateFlow("")
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(ObjectSearchViewModel.DEBOUNCE_DURATION).distinctUntilChanged())
        }

    val views = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())
    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    init {
        viewModelScope.launch {
            searchQuery
                .flatMapLatest { query ->
                    searchWithMeta
                        .asFlow(
                            Command.SearchWithMeta(
                                query = query,
                                limit = 50,
                                offset = 0,
                                keys = emptyList(),
                                filters = ObjectSearchConstants.filterSearchObjects(
                                    // TODO add tech space?
                                    spaces = listOf(spaceManager.get())
                                ),
                                sorts = ObjectSearchConstants.sortsSearchObjects,
                                withMetaRelationDetails = true,
                                withMeta = true
                            )
                        )
                }.collect { results ->
                    views.value = results.mapNotNull { result ->
                        result.view(
                            storeOfObjectTypes = storeOfObjectTypes,
                            storeOfRelations = storeOfRelations
                        )
                    }
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

    class Factory @Inject constructor(
        private val searchWithMeta: SearchWithMeta,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val storeOfRelations: StoreOfRelations,
        private val spaceManager: SpaceManager,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlobalSearchViewModel(
                searchWithMeta = searchWithMeta,
                storeOfObjectTypes = storeOfObjectTypes,
                storeOfRelations = storeOfRelations,
                spaceManager = spaceManager,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}

/**
 * @property [title] object title
 * @property [type] type screen name
 */
data class  GlobalSearchItemView(
    val id: Id,
    val space: SpaceId,
    val layout: ObjectType.Layout,
    val title: String,
    val type: String,
    val meta: Meta
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
    storeOfRelations: StoreOfRelations
) : GlobalSearchItemView? {
    if (wrapper.spaceId == null) return null
    if (wrapper.layout == null) return null
    val type = wrapper.type.firstOrNull()
    val meta = metas.firstOrNull()
    return GlobalSearchItemView(
        id = obj,
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
                                GlobalSearchItemView.Meta.Status(
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
package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_ID
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.relations.GetRelations
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationItemView
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.model.Section
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultObjectSearchSorts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterMarketplaceRelations
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterMyRelations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base view model for adding a relation either to an object or to a set.
 */
abstract class RelationAddViewModelBase(
    relationsProvider: ObjectRelationProvider,
    private val getRelations: GetRelations,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val addObjectToWorkspace: AddObjectToWorkspace,
    private val workspaceManager: WorkspaceManager
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)

    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val command = MutableSharedFlow<Command>()
    val isDismissed = MutableStateFlow(false)
    val results = MutableStateFlow(emptyList<RelationItemView>())

    private val objectRelationKeys = relationsProvider.observeAll().map { relations ->
        relations.map { r -> r.key }
    }

    init {
        viewModelScope.launch {
            combine(
                searchQuery,
                objectRelationKeys
            ) { query, keys ->
                val myRelations = proceedWithGettingMyRelations(
                    query = query
                )
                val excludedRelations = myRelations.mapNotNull { it.sourceObject }
                val marketplaceRelations = proceedWithGettingMarketplaceRelations(
                    query = query,
                    excluded = excludedRelations
                )
                buildViews(
                    myRelations = myRelations,
                    marketplaceRelations = marketplaceRelations,
                    objectRelationKeys = keys
                )
            }.flowOn(appCoroutineDispatchers.io).catch {
                sendToast("An error occurred. Please try again later.")
            }.collect { views ->
                results.value = views
            }
        }
    }

    private fun buildViews(
        myRelations: List<ObjectWrapper.Relation>,
        marketplaceRelations: List<ObjectWrapper.Relation>,
        objectRelationKeys: List<Key>,
    ) = buildList {
        val my = myRelations.filter { !objectRelationKeys.contains(it.key) }.map { wrapper ->
            RelationView.Existing(
                id = wrapper.id,
                key = wrapper.key,
                name = wrapper.name.orEmpty(),
                format = wrapper.format,
                workspace = wrapper.workspaceId
            )
        }
        val marketplace = marketplaceRelations.filter { !objectRelationKeys.contains(it.key) }.map { wrapper ->
            RelationView.Existing(
                id = wrapper.id,
                key = wrapper.key,
                name = wrapper.name.orEmpty(),
                format = wrapper.format,
                workspace = wrapper.workspaceId
            )
        }
        if (my.isNotEmpty()) {
            add(Section.Library)
            addAll(my)
        }
        if (marketplace.isNotEmpty()) {
            add(Section.Marketplace)
            addAll(marketplace)
        }
    }

    private suspend fun proceedWithGettingMarketplaceRelations(
        excluded: List<Id>,
        query: String
    ): List<ObjectWrapper.Relation> {
        val params = GetRelations.Params(
            sorts = defaultObjectSearchSorts(),
            filters = buildList {
                addAll(filterMarketplaceRelations())
                if (excluded.isNotEmpty()) {
                    add(
                        DVFilter(
                            relationKey = Relations.ID,
                            condition = DVFilterCondition.NOT_IN,
                            value = excluded
                        )
                    )
                }
                add(
                    DVFilter(
                        relationKey = Relations.IS_HIDDEN,
                        condition = DVFilterCondition.EQUAL,
                        value = false
                    )
                )
                add(
                    DVFilter(
                        relationKey = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = MARKETPLACE_ID
                    )
                )
                add(
                    DVFilter(
                        relationKey = Relations.RELATION_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = Relations.systemRelationKeys
                    )
                )
            },
            query = query
        )
        return getRelations.execute(
            params = params
        )
    }

    private suspend fun proceedWithGettingMyRelations(
        query: String
    ): List<ObjectWrapper.Relation> {
        val params = GetRelations.Params(
            sorts = defaultObjectSearchSorts(),
            filters = buildList {
                addAll(filterMyRelations())
                add(
                    DVFilter(
                        relationKey = Relations.IS_HIDDEN,
                        condition = DVFilterCondition.EQUAL,
                        value = false
                    )
                )
                add(
                    DVFilter(
                        relationKey = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = workspaceManager.getCurrentWorkspace()
                    )
                )
                add(
                    DVFilter(
                        relationKey = Relations.RELATION_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = Relations.systemRelationKeys
                    )
                )
            },
            query = query
        )
        return getRelations.execute(
            params = params
        )
    }

    abstract fun sendAnalyticsEvent(length: Int)

    fun onQueryChanged(input: String) {
        sendAnalyticsEvent(input.length)
        userInput.value = input
    }

    fun onRelationSelected(
        ctx: Id,
        relation: RelationView.Existing
    ) {
        viewModelScope.launch {
            if (relation.workspace == MARKETPLACE_ID) {
                addObjectToWorkspace(
                    AddObjectToWorkspace.Params(
                        objects = listOf(relation.id)
                    )
                ).proceed(
                    success = {
                        sendToast("Relation `${relation.name}` added to your library")
                        proceedWithDispatchingSelectedRelation(
                            ctx = ctx,
                            relation = relation
                        )
                    },
                    failure = {
                        Timber.e(it, "Error while adding relation to workspace.")
                        sendToast("Something went wrong. Please, try again later.")
                    }
                )
            } else {
                proceedWithDispatchingSelectedRelation(
                    ctx = ctx,
                    relation = relation
                )
            }
        }
    }

    private suspend fun proceedWithDispatchingSelectedRelation(
        ctx: Id,
        relation: RelationView.Existing
    ) {
        command.emit(
            Command.DispatchSelectedRelation(
                ctx = ctx,
                relation = relation.key,
                format = relation.format
            )
        )
    }

    sealed class Command {
        data class DispatchSelectedRelation(
            val ctx: Id,
            val relation: Key,
            val format: RelationFormat
        ) : Command()
    }

    companion object {
        const val ERROR_MESSAGE = "Error while adding relation to object"
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}
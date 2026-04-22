package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import timber.log.Timber

/**
 * Container for the "My Favorites" section — an Unread-style compact list of
 * personal favorites for the current user in [space].
 *
 * Favorites live in the per-user virtual widgets doc at
 * `_personalWidgets_<encodedSpaceId>`. We open that doc and listen to its block
 * events the same way [com.anytypeio.anytype.presentation.home.HomeScreenViewModel]
 * listens to the shared widgets doc: initial [ObjectView] from [OpenObject],
 * then [InterceptEvents] for live updates, reduced into an up-to-date tree.
 *
 * From the tree we extract the ordered list of link-target object IDs (wrapper
 * children under root), filter out built-in targets (`favorite`, `recent`, …)
 * and subscribe to the resulting object IDs so the UI has the full
 * [com.anytypeio.anytype.core_models.ObjectWrapper.Basic] for each row.
 */
class PersonalFavoritesWidgetContainer(
    private val space: SpaceId,
    private val widget: Widget.PersonalFavorites,
    private val openObject: OpenObject,
    private val interceptEvents: InterceptEvents,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    isSessionActiveFlow: Flow<Boolean>
) : WidgetContainer {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> = isSessionActiveFlow
        .flatMapLatest { isActive ->
            if (!isActive) flowOf(emptyView())
            else buildViewFlow()
        }
        .catch { e ->
            Timber.e(e, "PersonalFavoritesWidgetContainer: view flow failed")
            emit(emptyView())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> = personalWidgetsTree()
        .map { tree -> tree.orderedRealTargets() }
        .distinctUntilChanged()
        .flatMapLatest { targets ->
            if (targets.isEmpty()) flowOf(emptyView())
            else joinWithObjects(targets)
        }

    private fun personalWidgetsTree(): Flow<ObjectView> = flow {
        val docId = personalWidgetsId(space)
        val initial = openObject.run(
            OpenObject.Params(
                obj = docId,
                spaceId = space,
                saveAsLastOpened = false
            )
        )
        emitAll(
            interceptEvents
                .build(InterceptEvents.Params(context = docId))
                .scan(initial) { state, events -> reduce(state, events) }
        )
    }

    private fun joinWithObjects(targets: List<Id>): Flow<WidgetView> {
        val params = StoreSearchParams(
            space = space,
            subscription = "subscription.personal.favorites.${widget.id}",
            keys = ObjectSearchConstants.defaultKeys,
            filters = listOf(
                DVFilter(
                    relation = Relations.ID,
                    condition = DVFilterCondition.IN,
                    value = targets
                )
            ),
            limit = 0,
            source = emptyList(),
            collection = null,
            sorts = emptyList()
        )
        return storage.subscribe(params).map { objects ->
            val byId = objects.associateBy { it.id }
            val ordered = targets.mapNotNull { byId[it] }
            WidgetView.SetOfObjects(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = ordered.map { obj ->
                    WidgetView.SetOfObjects.Element.Regular(
                        obj = obj,
                        objectIcon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(obj)
                        ),
                        name = WidgetView.Name.Default(
                            prettyPrintName = fieldParser.getObjectPluralName(obj, false)
                        )
                    )
                },
                isExpanded = true,
                isCompact = true,
                name = WidgetView.Name.Empty,
                sectionType = widget.sectionType
            )
        }
    }

    private fun emptyView(): WidgetView = WidgetView.SetOfObjects(
        id = widget.id,
        source = widget.source,
        tabs = emptyList(),
        elements = emptyList(),
        isExpanded = true,
        isCompact = true,
        name = WidgetView.Name.Empty,
        sectionType = widget.sectionType
    )
}

/**
 * Reduces block-tree events relevant to personal-favorites rendering into an
 * updated [ObjectView]. Only handles events that can change the flat list of
 * `<wrapper-widget> -> <link-block(target=objectId)>` children of root:
 * add/delete block, update structure (reorder), and link-target change.
 * Other events are intentionally ignored.
 */
private fun reduce(state: ObjectView, events: List<Event>): ObjectView {
    var curr = state
    events.forEach { event ->
        when (event) {
            is Event.Command.AddBlock -> {
                curr = curr.copy(blocks = curr.blocks + event.blocks)
            }
            is Event.Command.DeleteBlock -> {
                curr = curr.copy(blocks = curr.blocks.filter { it.id !in event.targets })
            }
            is Event.Command.UpdateStructure -> {
                curr = curr.copy(blocks = curr.blocks.map { block ->
                    if (block.id == event.id) block.copy(children = event.children) else block
                })
            }
            is Event.Command.LinkGranularChange -> {
                curr = curr.copy(blocks = curr.blocks.map { block ->
                    if (block.id != event.id) return@map block
                    val content = block.content as? Block.Content.Link ?: return@map block
                    block.copy(content = content.copy(target = event.target))
                })
            }
            else -> {
                // Other events do not affect the personal-favorites target list.
            }
        }
    }
    return curr
}

/**
 * Walks root's children to collect ordered target-object IDs. Expects each
 * root child to be a wrapper block whose first child is a link block pointing
 * at a real object. Built-in targets (`favorite`, `recent`, `allObjects`, …)
 * are filtered out.
 */
private fun ObjectView.orderedRealTargets(): List<Id> {
    val byId = blocks.associateBy { it.id }
    val rootBlock = byId[root] ?: return emptyList()
    return rootBlock.children.mapNotNull { wrapperId ->
        val wrapper = byId[wrapperId] ?: return@mapNotNull null
        if (wrapper.content !is Block.Content.Widget) return@mapNotNull null
        val firstChildId = wrapper.children.firstOrNull() ?: return@mapNotNull null
        val child = byId[firstChildId] ?: return@mapNotNull null
        val link = child.content as? Block.Content.Link ?: return@mapNotNull null
        link.target.takeIf { it !in BundledWidgetSourceIds.ids }
    }
}

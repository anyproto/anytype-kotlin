package com.anytypeio.anytype.presentation.relations.value.tagstatus

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfRelationOptions
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationOptions
import com.anytypeio.anytype.domain.relations.SetRelationOptionOrder
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class TagOrStatusValueViewModel(
    private val viewModelParams: ViewModelParams,
    private val values: ObjectValueProvider,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val deleteRelationOptions: DeleteRelationOptions,
    private val setRelationOptionOrder: SetRelationOptionOrder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfRelationOptions: StoreOfRelationOptions
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val viewState = MutableStateFlow<TagStatusViewState>(TagStatusViewState.Loading)

    private val input = MutableStateFlow("")
    val queryState: StateFlow<String> = input.asStateFlow()
    @OptIn(FlowPreview::class)
    private val query = input.take(1).onCompletion {
        emitAll(
            input.drop(1).debounce(300L).distinctUntilChanged()
        )
    }
    private var isEditableRelation = false
    /**
     * One-shot commands. Backed by an unbounded [Channel] (not a `replay = 0`
     * SharedFlow) so the `Command.Expand` emitted from the `init` pipeline on first
     * load — before the bottom sheet has attached its collector — is buffered and
     * delivered to the next subscriber instead of being silently dropped, which left
     * the sheet stuck unexpanded (DROID-4523). The single collecting fragment makes
     * [receiveAsFlow]'s single-consumer semantics correct.
     */
    private val commandsChannel = Channel<Command>(Channel.UNLIMITED)
    val commands: Flow<Command> = commandsChannel.receiveAsFlow()

    override fun onCleared() {
        // viewModelScope is already cancelled by the time onCleared() runs, so no
        // send() can race this close(). Closing is explicit about intent: nothing
        // will consume the channel once the ViewModel is gone.
        commandsChannel.close()
        super.onCleared()
    }

    private var isInitialExpandDone = false

    // Lock mechanism to prevent race conditions during DnD operations
    // When a drag operation completes, we optimistically update the UI and send to middleware
    // We then lock event processing for a short period to prevent incoming events from
    // overwriting our optimistic update before middleware confirms the change
    private var optionEventLockTimestamp: Long? = null

    // Cached after init so click handlers can rebuild viewState optimistically without
    // re-reading the (possibly orphaned / never re-emitting) value store. See proceedWithOptimisticUpdate.
    private var currentRelation: ObjectWrapper.Relation? = null

    /**
     * Authoritative, *unfiltered* selection — the single source of truth for every write.
     * Must never be derived from [viewState], whose items are filtered by the search query:
     * selections hidden by an active query would then be silently dropped from the persisted
     * value.
     */
    private var currentIds: List<Id> = emptyList()

    /**
     * All options of this relation in store (drag-and-drop) order, kept unfiltered so a
     * reorder can be expressed against the full order even while a query is active and
     * while the list is displayed selected-first.
     */
    private var currentOptions: List<ObjectWrapper.Option> = emptyList()

    /**
     * Ids selected at the moment the sheet was opened. Selected-first sorting is anchored to
     * this snapshot — not to the live selection — so that toggling an option does not make
     * rows jump under the user's finger mid-session.
     */
    private val initialIds = mutableListOf<Id>()
    private var isInitialSortDone = false

    init {
        viewModelScope.launch {
            val relation = storeOfRelations.getByKey(key = viewModelParams.relationKey) ?: return@launch
            setupIsRelationNotEditable(relation)
            currentRelation = relation
            combine(
                values.subscribe(
                    ctx = viewModelParams.ctx,
                    target = viewModelParams.objectId
                ),
                query.distinctUntilChanged(),
                storeOfRelationOptions.trackChanges()
            ) { record, query, _ ->
                // Skip state updates during active drag operations to prevent UI flickering
                if (isOptionEventLockActive()) {
                    Timber.d("DROID-3916, Skipping state update due to active option event lock")
                    return@combine
                }
                val options = storeOfRelationOptions.getByRelationKey(viewModelParams.relationKey)
                    .sortedBy { it.orderId }
                val ids = getRecordValues(record)
                currentOptions = options
                currentIds = ids
                captureInitialSelection(ids)
                if (!isInitialExpandDone) {
                    isInitialExpandDone = true
                    if (ids.isEmpty() && isEditableRelation) {
                        emitCommand(Command.Expand)
                    }
                }
                initViewState(
                    relation = relation,
                    options = filterOptions(query, options, ids),
                    query = query,
                    ids = ids
                )
            }
                .catch {
                    Timber.e(it, "TagOrStatusValue, Error while combining flows")
                }
                .collect()
        }
    }

    /**
     * Snapshots the selection the sheet was opened with, once. See [initialIds].
     */
    private fun captureInitialSelection(ids: List<Id>) {
        if (isInitialSortDone) return
        isInitialSortDone = true
        initialIds.clear()
        initialIds.addAll(ids)
    }

    private fun filterOptions(
        query: String,
        options: List<ObjectWrapper.Option>,
        ids: List<Id>
    ): List<ObjectWrapper.Option> {
        return if (isEditableRelation) {
            options.filter { it.name?.contains(query, true) == true }
        } else {
            options.filter { ids.contains(it.id) }
        }
    }

    fun onQueryChanged(input: String) {
        this.input.value = input
    }

    fun proceedWithDeleteOptions(optionId: Id) {
        viewModelScope.launch {
            val params = DeleteRelationOptions.Params(listOf(optionId))
            deleteRelationOptions.execute(params).fold(
                onSuccess = {
                    Timber.d("Options deleted successfully")
                    removeTag(optionId)
                },
                onFailure = { Timber.e(it, "Error while deleting options") }
            )
        }
    }

    fun onAction(action: TagStatusAction) {
        Timber.d("TagStatusViewModel onAction, action: $action")
        if (!isEditableRelation) {
            Timber.d("TagStatusViewModel onAction, relation is not editable")
            sendToast("Relation is not editable")
            return
        }
        when (action) {
            TagStatusAction.Clear -> clearTagsOrStatus()
            is TagStatusAction.Click -> onActionClick(action.item)
            is TagStatusAction.LongClick -> {
                val currentState = viewState.value
                if (currentState is TagStatusViewState.Content) {
                    viewState.value = currentState.copy(showItemMenu = action.item)
                }
            }

            TagStatusAction.Plus -> openOptionScreen(
                Command.OpenOptionScreen(
                    color = ThemeColor.entries.drop(1).random().code,
                    relationKey = viewModelParams.relationKey,
                    ctx = viewModelParams.ctx,
                    objectId = viewModelParams.objectId
                )
            )
            is TagStatusAction.Delete -> {
                emitCommand(Command.DeleteOption(action.optionId))
            }
            is TagStatusAction.Duplicate -> {
                val item = action.item
                openOptionScreen(
                    Command.OpenOptionScreen(
                        color = item.color.code,
                        text = item.name,
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            is TagStatusAction.Edit -> {
                val item = action.item
                openOptionScreen(
                    Command.OpenOptionScreen(
                        optionId = item.optionId,
                        color = item.color.code,
                        text = item.name,
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            TagStatusAction.Create -> {
                openOptionScreen(
                    Command.OpenOptionScreen(
                        text = "",
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
            is TagStatusAction.OnMove -> {
                Timber.d("OnMove from ${action.from} to ${action.to}")
                viewModelScope.launch {
                    val currentState = viewState.value
                    if (currentState !is TagStatusViewState.Content) return@launch
                    val visible = currentState.items.map { it.optionId }
                    if (action.from !in visible.indices || action.to !in visible.indices) {
                        Timber.w("OnMove with out-of-bounds indices, ignoring")
                        return@launch
                    }
                    val reorderedVisible = visible
                        .toMutableList()
                        .apply { add(action.to, removeAt(action.from)) }
                    val reorderedIds = reorderOptions(
                        fullOrder = currentOptions.map { it.id },
                        reorderedVisible = reorderedVisible,
                        movedIndex = action.to
                    )
                    if (reorderedIds == null) {
                        Timber.w("OnMove could not be projected on the full option order")
                        return@launch
                    }
                    // Activate lock before sending to middleware to prevent race conditions
                    activateOptionEventLock()
                    setRelationOptionOrder.async(
                        SetRelationOptionOrder.Params(
                            spaceId = viewModelParams.space,
                            relationKey = RelationKey(viewModelParams.relationKey),
                            orderedIds = reorderedIds
                        )
                    ).fold(
                        onSuccess = {
                            Timber.d("Option order saved successfully")
                            // Manual sorting wins over the selected-first float: once the user
                            // has dragged a row, stop lifting the initially selected tags, so
                            // the list they rebuild is the one they see.
                            initialIds.clear()
                        },
                        onFailure = { e ->
                            Timber.e(e, "Failed to save option order")
                            sendToast("Failed to save order")
                        }
                    )
                }
            }
        }
    }

    private fun emitCommand(command: Command, delay: Long = 0L) {
        viewModelScope.launch {
            delay(delay)
            commandsChannel.send(command)
        }
    }

    private fun openOptionScreen(command: Command.OpenOptionScreen) {
        viewModelScope.launch {
            commandsChannel.send(command)
        }
    }

    private fun onActionClick(item: RelationsListItem) {
        when (item) {
            is RelationsListItem.Item.Status -> {
                if (item.isSelected) {
                    clearTagsOrStatus()
                } else {
                    addStatus(item.optionId)
                }
            }
            is RelationsListItem.Item.Tag -> {
                if (item.isSelected) {
                    removeTag(item.optionId)
                } else {
                    addTag(item.optionId)
                }
            }
            is RelationsListItem.CreateItem.Tag -> {
                input.value = ""  // Clear the query so user sees full list after creating option
                emitCommand(
                    Command.OpenOptionScreen(
                        text = item.text,
                        relationKey = viewModelParams.relationKey,
                        ctx = viewModelParams.ctx,
                        objectId = viewModelParams.objectId
                    )
                )
            }
        }
    }

    private fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        options: List<ObjectWrapper.Option>,
        query: String
    ) {
        val result = mutableListOf<RelationsListItem.Item>()
        val isTagRelation = relation.format == Relation.Format.TAG

        when (relation.format) {
            Relation.Format.STATUS -> {
                result.addAll(
                    mapStatusOptions(
                        ids = ids,
                        options = options
                    )
                )
            }
            Relation.Format.TAG -> {
                result.addAll(
                    mapTagOptions(
                        ids = ids,
                        options = options
                    )
                )
            }
            else -> {
                Timber.w("Relation format should be Tag or Status but was: ${relation.format}")
            }
        }

        // CreateItem is only shown for TAG relations when there's a search query
        val createItem = if (isTagRelation && query.isNotBlank() && isEditableRelation) {
            RelationsListItem.CreateItem.Tag(query)
        } else {
            null
        }

        viewState.value = if (result.isEmpty() && createItem == null) {
            TagStatusViewState.Empty(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
            )
        } else {
            TagStatusViewState.Content(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
                items = result,
                createItem = createItem,
                // Derived from the unfiltered selection: a query that hides every selected
                // option must not hide the "Clear" action along with it.
                hasSelection = ids.isNotEmpty()
            )
        }.also {
            Timber.d("TagStatusViewModel initViewState, viewState: $it")
        }
    }

    private fun getRecordValues(record: Map<String, Any?>): List<Id> {
        return when (val value = record[viewModelParams.relationKey]) {
            is Id -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }

    /**
     * Projects a move performed on the *visible* list onto the full option order.
     *
     * When every option is on screen the visible list *is* the new order — what the user sees
     * is what gets stored. When a search query hides part of the list, persisting it verbatim
     * would drop every hidden option from the order, so the moved option is instead re-inserted
     * next to whichever visible neighbour it was dropped against, and every other option keeps
     * its place.
     *
     * @param fullOrder every option of the relation, in store order.
     * @param reorderedVisible the visible list *after* the move.
     * @param movedIndex index of the moved option within [reorderedVisible].
     * @return the new full order, or null if the move cannot be resolved.
     */
    private fun reorderOptions(
        fullOrder: List<Id>,
        reorderedVisible: List<Id>,
        movedIndex: Int
    ): List<Id>? {
        if (reorderedVisible.size == fullOrder.size && reorderedVisible.toSet() == fullOrder.toSet()) {
            return reorderedVisible
        }
        val moved = reorderedVisible.getOrNull(movedIndex) ?: return null
        val result = fullOrder.toMutableList()
        if (!result.remove(moved)) return null
        val precedingVisible = reorderedVisible
            .take(movedIndex)
            .lastOrNull { result.contains(it) }
        val insertAt = if (precedingVisible != null) {
            result.indexOf(precedingVisible) + 1
        } else {
            val followingVisible = reorderedVisible
                .drop(movedIndex + 1)
                .firstOrNull { result.contains(it) }
            // Without a visible neighbour on either side there is nothing to position the
            // option against, and moving it blindly would shuffle the order the user cannot
            // see. Leave the stored order alone.
                ?: return null
            result.indexOf(followingVisible)
        }
        result.add(insertAt, moved)
        return result
    }

    private fun addTag(tag: Id) {
        if (currentIds.contains(tag)) return
        val newIds = currentIds + tag
        proceedWithOptimisticUpdate(
            newIds = newIds,
            persistedValue = newIds,
            analyticsEvent = EventsDictionary.relationChangeValue,
            dismissOnSuccess = false
        )
    }

    private fun removeTag(tag: Id) {
        val newIds = currentIds - tag
        proceedWithOptimisticUpdate(
            newIds = newIds,
            persistedValue = newIds,
            analyticsEvent = if (newIds.isEmpty()) EventsDictionary.relationDeleteValue
            else EventsDictionary.relationChangeValue,
            dismissOnSuccess = false
        )
    }

    private fun addStatus(status: Id) {
        val newIds = listOf(status)
        proceedWithOptimisticUpdate(
            newIds = newIds,
            persistedValue = newIds,
            analyticsEvent = EventsDictionary.relationChangeValue,
            dismissOnSuccess = true
        )
    }

    private fun clearTagsOrStatus() {
        proceedWithOptimisticUpdate(
            newIds = emptyList(),
            persistedValue = null,
            analyticsEvent = EventsDictionary.relationDeleteValue,
            dismissOnSuccess = false
        )
    }

    /**
     * Single entry point for value edits. The sheet's list is normally rebuilt only when
     * [values].subscribe re-emits, but some hosts have no live writer for the backing store,
     * so a click would never be reflected. To fix that we update [viewState] optimistically:
     * 1. lock event processing so a stale [values] re-emission can't clobber the UI,
     * 2. rebuild [viewState] from [newIds] via [initViewState] (reuses filterOptions +
     *    option mapping / number recompute + create-item logic),
     * 3. persist via [setObjectDetails],
     * 4. on failure revert to the pre-click snapshot and toast,
     * 5. on success dispatch the payload, send analytics, optionally dismiss.
     */
    private fun proceedWithOptimisticUpdate(
        newIds: List<Id>,
        persistedValue: Any?,
        analyticsEvent: String,
        dismissOnSuccess: Boolean
    ) {
        val relation = currentRelation ?: return
        val previousState = viewState.value
        val previousIds = currentIds
        viewModelScope.launch {
            // Lock BEFORE the optimistic write so any concurrent combine emission is skipped.
            activateOptionEventLock()
            val options = storeOfRelationOptions
                .getByRelationKey(viewModelParams.relationKey)
                .sortedBy { it.orderId }
            currentOptions = options
            currentIds = newIds
            val query = input.value
            initViewState(
                relation = relation,
                options = filterOptions(query, options, newIds),
                query = query,
                ids = newIds
            )
            setObjectDetails(
                UpdateDetail.Params(
                    target = viewModelParams.objectId,
                    key = viewModelParams.relationKey,
                    value = persistedValue
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while updating tag/status value")
                    // Revert optimistic update and release the lock so real events are honored again.
                    optionEventLockTimestamp = null
                    currentIds = previousIds
                    viewState.value = previousState
                    sendToast("Error while updating value")
                },
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = analyticsEvent,
                        storeOfRelations = storeOfRelations,
                        relationKey = viewModelParams.relationKey,
                        spaceParams = provideParams(viewModelParams.space.id)
                    )
                    if (dismissOnSuccess) {
                        emitCommand(command = Command.Dismiss, delay = DELAY_UNTIL_CLOSE)
                    }
                }
            )
        }
    }

    /**
     * Maps options to Tag items.
     *
     * Options come from the store already sorted by relationOptionOrder; on top of that the
     * tags that were *already selected when the sheet was opened* float to the top, in their
     * selection order, so a long option list doesn't have to be scrolled to see what is active.
     *
     * The partition is anchored to [initialIds] rather than to the live selection on purpose:
     * a tag selected or deselected while the sheet is open keeps its row, and only moves to
     * the top the next time the sheet is opened. [sortedWith] is stable, so options outside
     * [initialIds] keep the store order.
     */
    private fun mapTagOptions(
        ids: List<Id>,
        options: List<ObjectWrapper.Option>
    ) = options.map { option ->
        RelationsListItem.Item.Tag(
            optionId = option.id,
            name = option.name.orEmpty(),
            color = getOrDefault(option.color),
            isSelected = ids.contains(option.id),
            number = ids.indexOf(option.id).takeIf { it != -1 }?.plus(1) ?: Int.MAX_VALUE
        )
    }.sortedWith(
        compareBy(
            { !initialIds.contains(it.optionId) },
            { initialIds.indexOf(it.optionId) }
        )
    )

    /**
     * Maps options to Status items.
     * Options from store are already sorted by relationOptionOrder, then by name.
     */
    private fun mapStatusOptions(
        ids: List<Id>,
        options: List<ObjectWrapper.Option>
    ) = options.map { option ->
        RelationsListItem.Item.Status(
            optionId = option.id,
            name = option.name.orEmpty(),
            color = getOrDefault(option.color),
            isSelected = ids.contains(option.id)
        )
    }

    private fun getOrDefault(code: String?): ThemeColor {
        return ThemeColor.entries.find { it.code == code } ?: ThemeColor.DEFAULT
    }

    private fun setupIsRelationNotEditable(relation: ObjectWrapper.Relation) {
        isEditableRelation = !(viewModelParams.isLocked
                || relation.isReadonlyValue
                || relation.isHidden == true
                || relation.isDeleted == true
                || relation.isArchived == true
                || !relation.isValid)
    }

    //region Option Event Lock
    /**
     * Activates the event lock for options to prevent race conditions.
     * Should be called before sending a drag-and-drop order change to middleware.
     */
    private fun activateOptionEventLock() {
        optionEventLockTimestamp = System.currentTimeMillis()
        Timber.d("DROID-3916, Option event lock activated at $optionEventLockTimestamp")
    }

    /**
     * Checks if the option event lock is currently active.
     * The lock is active if it was set within the last OPTION_EVENT_LOCK_DURATION_MS milliseconds.
     */
    private fun isOptionEventLockActive(): Boolean {
        val lockTimestamp = optionEventLockTimestamp ?: return false
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lockTimestamp
        val isActive = elapsedTime < OPTION_EVENT_LOCK_DURATION_MS

        if (!isActive) {
            Timber.d("DROID-3916, Option event lock expired (elapsed: ${elapsedTime}ms)")
            optionEventLockTimestamp = null
        }

        return isActive
    }
    //endregion

    data class ViewModelParams(
        val ctx: Id,
        val space: SpaceId,
        val objectId: Id,
        val relationKey: Key,
        val isLocked: Boolean,
        val relationContext: RelationContext
    )
}

sealed class Command {
    data class OpenOptionScreen(
        val ctx: Id,
        val objectId: Id,
        val relationKey: Key,
        val optionId: Id? = null,
        val color: String? = null,
        val text: String? = null,
    ) : Command()

    data class DeleteOption(val optionId: Id) : Command()

    object Dismiss : Command()

    object Expand : Command()
}

sealed class TagStatusViewState {

    object Loading : TagStatusViewState()
    data class Empty(val title: String, val isRelationEditable: Boolean) :
        TagStatusViewState()

    data class Content(
        val title: String,
        val items: List<RelationsListItem.Item>,
        val createItem: RelationsListItem.CreateItem.Tag? = null,
        val isRelationEditable: Boolean,
        val showItemMenu: RelationsListItem.Item? = null,
        /**
         * Whether the relation currently holds any value. Unlike [items], which is filtered by
         * the search query, this reflects the whole selection.
         */
        val hasSelection: Boolean = false
    ) : TagStatusViewState()
}

sealed class TagStatusAction {
    data class Click(val item: RelationsListItem) : TagStatusAction()
    data class LongClick(val item: RelationsListItem.Item) : TagStatusAction()
    object Clear : TagStatusAction()
    object Plus : TagStatusAction()
    data class Edit(val item: RelationsListItem.Item) : TagStatusAction()
    data class Delete(val optionId: Id) : TagStatusAction()
    data class Duplicate(val item: RelationsListItem.Item) : TagStatusAction()
    object Create : TagStatusAction()
    data class OnMove(val from: Int, val to: Int) : TagStatusAction()
}

enum class RelationContext { OBJECT, OBJECT_SET, DATA_VIEW }

sealed class RelationsListItem {

    sealed class Item : RelationsListItem() {

        abstract val optionId: Id
        abstract val name: String
        abstract val color: ThemeColor
        abstract val isSelected: Boolean

        data class Tag(
            override val optionId: Id,
            override val name: String,
            override val color: ThemeColor,
            override val isSelected: Boolean,
            val number: Int = Int.MAX_VALUE,
            val showMenu: Boolean = false
        ) : Item()

        data class Status(
            override val optionId: Id,
            override val name: String,
            override val color: ThemeColor,
            override val isSelected: Boolean
        ) : Item()
    }

    sealed class CreateItem(
        val text: String
    ) : RelationsListItem() {
        class Tag(text: String) : CreateItem(text)
    }
}

const val DELAY_UNTIL_CLOSE = 300L

// Duration in milliseconds to lock option event processing after a drag operation
// This prevents incoming middleware events from overwriting optimistic UI updates
private const val OPTION_EVENT_LOCK_DURATION_MS = 1500L

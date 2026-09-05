package com.anytypeio.anytype.presentation.quickcapture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.domain.ai.TypeSuggestionEngine
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.quickcapture.FetchQuickCaptureDraft
import com.anytypeio.anytype.domain.quickcapture.MoveQuickCaptureDraft
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Host view model for the Quick Capture sheet (spec: docs/quick-capture-android-spec.md).
 *
 * Owns the draft lifecycle (one hidden draft per space, device-local pointer), the target
 * space selection (chip + picker), the emptiness signal driving the Send/trash controls,
 * and the publish flow. The embedded editor renders the draft; this VM never touches blocks
 * except when retargeting the draft to another space.
 *
 * Lifecycle contract: this VM outlives the sheet's view (config changes recreate the view,
 * not the VM), so global-state cleanup — `spaceManager.clear()` and the middleware
 * subscription — happens in [onCleared] / on flow completion, never in the dialog's
 * dismiss callback, which also fires on plain view destruction.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuickCaptureViewModel(
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceManager: SpaceManager,
    private val settings: UserSettingsRepository,
    private val createObject: CreateObject,
    private val setObjectDetails: SetObjectDetails,
    private val deleteObjects: DeleteObjects,
    private val moveQuickCaptureDraft: MoveQuickCaptureDraft,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val typeSuggestionEngine: TypeSuggestionEngine,
    private val fetchQuickCaptureDraft: FetchQuickCaptureDraft
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    sealed class ScreenState {
        data object Loading : ScreenState()
        data class Ready(val space: SpaceId, val draft: Id) : ScreenState()
    }

    sealed class Command {
        data class Dismiss(val result: PublishedResult? = null) : Command()
    }

    /** Payload handed back to the vault for the success banner. */
    data class PublishedResult(
        val objectId: Id,
        val spaceId: Id,
        val typeName: String,
        val spaceName: String
    )

    data class SpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val isSelected: Boolean,
        /** This space is holding an unsent draft — surfaced as a pencil in the picker. */
        val hasDraft: Boolean = false
    ) {
        val targetSpaceId: Id? get() = space.targetSpaceId
        val name: String get() = space.name.orEmpty()
    }

    val screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)

    // Buffered channel, not a replay=0 shared flow: a Dismiss emitted while the sheet is
    // stopped (e.g. Send tapped right before backgrounding) must be delivered on restart,
    // not silently dropped.
    private val _commands = Channel<Command>(Channel.BUFFERED)
    val commands = _commands.receiveAsFlow()

    val showSpacePicker = MutableStateFlow(false)
    val showClearDraftConfirmation = MutableStateFlow(false)

    /**
     * Raised when both the current draft and the target space's draft hold content. The
     * target's draft is never destroyed — the user cannot see it, so overwriting it would be
     * a blind delete. The choice is only ever about the draft in front of them: discard it,
     * or leave it where it is. Both then open the target's own draft.
     */
    val draftConflict = MutableStateFlow<DraftConflict?>(null)

    data class DraftConflict(val space: Id, val spaceName: String)

    /**
     * Raised when the draft on screen has content the user did not type this session — they
     * reopened an earlier note and switched spaces without touching it. "Text follows the
     * chip" reads as intent only when the text was just written; silently relocating an older
     * draft because its space was left is not what the gesture meant. So ask.
     */
    val moveOrNewPrompt = MutableStateFlow<MoveOrNewRequest?>(null)

    data class MoveOrNewRequest(val space: Id, val spaceName: String)

    /** What to do with the current draft when the selection actually proceeds. */
    private enum class SwitchMode {
        /** No conflict: "text follows the chip" — carry the draft into the target space. */
        MOVE_CURRENT,

        /** Conflict, user chose to drop the visible draft and pick up the target's. */
        DISCARD_CURRENT,

        /** Conflict, user chose to leave the visible draft in its own space. */
        KEEP_CURRENT
    }

    private val selectedSpaceId = MutableStateFlow<Id?>(null)

    /** Device-local `[spaceId → lastInteractionDate]`; an input of [spaces] so ordering reacts to it. */
    private val recency = MutableStateFlow<Map<Id, Long>>(emptyMap())

    /**
     * Spaces holding an unsent draft, from the device-local pointers. Deliberately not
     * validated against the backend: that would be one fetch per space on every picker open,
     * and the cost of a rare stale pencil is far lower than the latency. Refreshed whenever
     * this VM creates, moves, discards or publishes a draft.
     */
    private val draftSpaces = MutableStateFlow<Set<Id>>(emptySet())

    private var startJob: Job? = null

    /** Editable candidate spaces, ordered by [spaceComparator]. */
    val spaces: StateFlow<List<SpaceView>> = combine(
        spaceViews.observe(),
        userPermissionProvider.all(),
        selectedSpaceId,
        recency,
        draftSpaces
    ) { all, permissions, selected, recencyMap, drafts ->
        all.filter { view -> view.isEditable(permissions) }
            .sortedWith(spaceComparator(recencyMap))
            .map { view ->
                SpaceView(
                    space = view,
                    icon = view.spaceIcon(urlBuilder),
                    isSelected = view.targetSpaceId == selected,
                    hasDraft = view.targetSpaceId in drafts
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedSpace: StateFlow<SpaceView?> = combine(
        spaces,
        selectedSpaceId
    ) { list, selected ->
        list.firstOrNull { it.targetSpaceId == selected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The draft object as observed via a filter-less ids subscription. */
    private val draftDetails: StateFlow<ObjectWrapper.Basic?> = screenState
        .flatMapLatest { state ->
            when (state) {
                is ScreenState.Ready -> storelessSubscriptionContainer.subscribe(
                    StoreSearchByIdsParams(
                        space = state.space,
                        subscription = QUICK_CAPTURE_DRAFT_SUBSCRIPTION,
                        keys = listOf(
                            Relations.ID,
                            Relations.NAME,
                            Relations.SNIPPET,
                            Relations.TYPE,
                            Relations.LAYOUT,
                            Relations.INTERNAL_FLAGS,
                            Relations.IS_DELETED,
                            Relations.IS_ARCHIVED
                        ),
                        targets = listOf(state.draft)
                    )
                ).onCompletion {
                    // Runs when the sheet switches drafts AND when viewModelScope dies —
                    // the middleware-side subscription must not outlive its consumer.
                    withContext(NonCancellable) {
                        runCatching {
                            storelessSubscriptionContainer.unsubscribe(
                                listOf(QUICK_CAPTURE_DRAFT_SUBSCRIPTION)
                            )
                        }
                    }
                }
                else -> flowOf(emptyList())
            }
        }.combine(screenState) { objects, state ->
            when (state) {
                is ScreenState.Ready -> objects.firstOrNull { it.id == state.draft }
                else -> null
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Emptiness signal for the UI (Send disabled / trash hidden).
     *
     * Derived from actual content — the title and the middleware-maintained text snippet —
     * not from the ShouldEmptyDelete internal flag. That flag is one-way: heart clears it on
     * the first content and never restores it, so deleting your text back out would leave
     * Send enabled and let you publish an empty object. Until the first subscription
     * emission the draft counts as empty, which is the safe side.
     */
    val isDraftEmpty: StateFlow<Boolean> = draftDetails
        .combine(screenState) { details, state ->
            if (state !is ScreenState.Ready || details == null) return@combine true
            details.name.isNullOrBlank() && details.snippet.isNullOrBlank()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * Latches on the first send. Guards far more than a double tap: between the publish
     * succeeding and the sheet actually going away there is bookkeeping, a lastUsedDate
     * write and an analytics event — real round trips — during which screenState still names
     * the object, it is no longer hidden, and the whole sheet is tappable. Every path that
     * could destroy or relocate that object has to stand down once a send is in flight.
     */
    private val isSending = AtomicBoolean(false)

    /**
     * Emptiness for *destructive-ish* decisions (space switch): here the safe side is the
     * opposite — only a subscription-confirmed empty draft may skip the content move.
     */
    private var pendingSourceHasContent: Boolean? = null

    /**
     * Emptiness of the draft we are switching *away from*. Prefers the editor's live answer
     * over the lagging details subscription; without a live answer, only a
     * subscription-confirmed empty draft may skip the content move.
     */
    private fun isSourceDraftEmpty(): Boolean {
        pendingSourceHasContent?.let { return !it }
        val details = draftDetails.value ?: return false
        return details.name.isNullOrBlank() && details.snippet.isNullOrBlank()
    }

    val syncStatus: StateFlow<SpaceSyncAndP2PStatusState> = spaceSyncAndP2PStatusProvider
        .observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpaceSyncAndP2PStatusState.Init)

    val syncStatusWidget = MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)

    fun onStart() {
        if (screenState.value is ScreenState.Ready || startJob?.isActive == true) return
        // Prewarm at sheet open (handoff §7) — fire-and-forget, never blocks capture.
        viewModelScope.launch { runCatching { typeSuggestionEngine.prewarm() } }
        startJob = viewModelScope.launch {
            val recencyMap = runCatching { settings.getSpaceLastInteractions() }
                .getOrDefault(emptyMap())
            recency.value = recencyMap
            refreshDraftSpaces()
            // Auto-selection is computed from the raw inputs, not the UI flow, so it cannot
            // race the recency load against the Compose subscription of [spaces].
            val candidates = withTimeoutOrNull(SPACES_AWAIT_TIMEOUT_MS) {
                combine(
                    spaceViews.observe(),
                    userPermissionProvider.all()
                ) { all, permissions ->
                    all.filter { view -> view.isEditable(permissions) }
                }.first { it.isNotEmpty() }
            }.orEmpty().sortedWith(spaceComparator(recencyMap))
            val resolved = resolveInitialSpace(candidates)
            if (resolved == null) {
                Timber.w("Quick capture: no editable non-1:1 space available")
                _commands.send(Command.Dismiss())
            } else {
                val (space, pendingDraft) = resolved
                proceedWithSpace(space = space, preValidatedDraft = pendingDraft)
            }
        }
    }

    /**
     * Picks the space the sheet opens in, and (when known) its already-validated draft.
     *
     * Priority is the unfinished thought: if the last space captured into still holds a
     * pending draft that exists on disk, reopen there — wherever that space sits in the
     * list, 1:1 included, since it was the user's own explicit choice. Otherwise fall back
     * to the ordered candidate list, where 1:1 spaces are never auto-selected (handoff §3).
     */
    private suspend fun resolveInitialSpace(
        candidates: List<ObjectWrapper.SpaceView>
    ): Pair<SpaceId, Id?>? {
        val last = runCatching { settings.getQuickCaptureLastSpace() }.getOrNull()
        if (last != null && candidates.any { it.targetSpaceId == last }) {
            val lastSpace = SpaceId(last)
            val draft = runCatching { settings.getQuickCaptureDraft(lastSpace) }.getOrNull()
            if (draft != null && isDraftAlive(space = lastSpace, draft = draft)) {
                Timber.d("Quick capture: restoring pending draft in last space $last")
                return lastSpace to draft
            }
        }
        val fallback = candidates.firstOrNull { !it.isOneToOneSpace }?.targetSpaceId
        return fallback?.let { SpaceId(it) to null }
    }

    /**
     * Object.Search by id only — no default filters, which would exclude the hidden draft.
     * `isHidden == true` is the "still an unpublished draft" check: without it an
     * interrupted send could promote an already-published note back into the draft slot,
     * where the trash button would permanently delete it.
     */
    private suspend fun isDraftAlive(space: SpaceId, draft: Id): Boolean {
        val obj = fetchOwnDraft(space, draft) ?: return false
        return obj.isDeleted != true &&
            obj.isArchived != true &&
            obj.isHidden == true
    }

    /**
     * Reads the space's draft, scoped by the query to the one this participant created —
     * another member's draft is never returned, so it cannot be adopted, counted or deleted.
     */
    private suspend fun fetchOwnDraft(space: SpaceId, draft: Id): ObjectWrapper.Basic? =
        fetchQuickCaptureDraft.async(
            FetchQuickCaptureDraft.Params(
                space = space,
                draft = draft,
                keys = DRAFT_VALIDATION_KEYS
            )
        ).getOrNull()

    /** Re-reads the device-local draft pointers that drive the picker's pencils. */
    private suspend fun refreshDraftSpaces() {
        draftSpaces.value = runCatching { settings.getQuickCaptureDrafts().keys }
            .onFailure { Timber.w(it, "Quick capture: could not read draft pointers") }
            .getOrDefault(emptySet())
    }

    private suspend fun markSelectedSpace(space: SpaceId) {
        selectedSpaceId.value = space.id
        runCatching { settings.setQuickCaptureLastSpace(space.id) }
            .onFailure { Timber.w(it, "Quick capture: could not persist last space") }
    }

    private fun ObjectWrapper.SpaceView.isEditable(
        permissions: Map<Id, SpaceMemberPermissions>
    ): Boolean {
        val target = targetSpaceId
        return isActive && target != null && permissions[target]?.isOwnerOrEditor() == true
    }

    /**
     * Ordering mirrors the vault (see VaultViewModel.transformToVaultSpaceViews): pinned
     * spaces first in their manual order, then the rest. Quick capture adds one tier on
     * top — 1:1 conversations always sort last — and uses device-local capture recency
     * where the vault uses chat-message recency.
     */
    private fun spaceComparator(
        recencyMap: Map<Id, Long>
    ): Comparator<ObjectWrapper.SpaceView> {
        return compareBy<ObjectWrapper.SpaceView> { it.isOneToOneSpace }
            // Pinned before unpinned, then the user's manual pin order (lexid asc).
            .thenBy { view -> view.spaceOrder.isNullOrEmpty() }
            .thenBy { view -> view.spaceOrder ?: "" }
            // Unpinned spaces all tie above, so recency decides among them.
            .thenByDescending { view -> recencyMap[view.targetSpaceId] ?: 0L }
            .thenByDescending { view ->
                view.spaceJoinDate ?: view.getSingleValue<Double>(Relations.CREATED_DATE) ?: 0.0
            }
    }

    /**
     * Activates the target space (workspaceOpen + store swap; no navigation, no
     * SaveCurrentSpace) and ensures its draft. [preValidatedDraft], when given, was already
     * checked by [resolveInitialSpace] — skip the duplicate lookup.
     */
    private suspend fun proceedWithSpace(space: SpaceId, preValidatedDraft: Id? = null) {
        screenState.value = ScreenState.Loading
        spaceManager.set(space.id).fold(
            onFailure = {
                Timber.e(it, "Quick capture: could not open space ${space.id}")
                sendToast(SOMETHING_WENT_WRONG)
                _commands.send(Command.Dismiss())
            },
            onSuccess = {
                markSelectedSpace(space)
                if (preValidatedDraft != null) {
                    screenState.value = ScreenState.Ready(space = space, draft = preValidatedDraft)
                } else {
                    ensureDraft(space)
                }
            }
        )
    }

    private suspend fun ensureDraft(space: SpaceId) {
        val existing = settings.getQuickCaptureDraft(space)
        if (existing != null) {
            if (isDraftAlive(space = space, draft = existing)) {
                screenState.value = ScreenState.Ready(space = space, draft = existing)
                return
            } else {
                Timber.d("Quick capture: stale draft pointer for ${space.id} — creating fresh")
                settings.clearQuickCaptureDraft(space)
            }
        }
        createDraft(space)
    }

    private suspend fun createDraft(space: SpaceId) {
        createObject.async(
            CreateObject.Param(
                space = space,
                type = null,
                internalFlags = listOf(
                    InternalFlags.ShouldEmptyDelete,
                    InternalFlags.ShouldSelectType
                ),
                prefilled = mapOf(Relations.IS_HIDDEN to true)
            )
        ).fold(
            onSuccess = { result ->
                // Belt-and-braces: some create paths may ignore prefilled details (spec V5).
                proceedWithHidingDraftIfNeeded(result)
                settings.setQuickCaptureDraft(space = space, obj = result.objectId)
                screenState.value = ScreenState.Ready(space = space, draft = result.objectId)
                refreshDraftSpaces()
            },
            onFailure = {
                Timber.e(it, "Quick capture: could not create draft in ${space.id}")
                sendToast(SOMETHING_WENT_WRONG)
                _commands.send(Command.Dismiss())
            }
        )
    }

    private suspend fun proceedWithHidingDraftIfNeeded(result: CreateObject.Result) {
        if (result.obj.getSingleValue<Boolean>(Relations.IS_HIDDEN) != true) {
            setObjectDetails.async(
                SetObjectDetails.Params(
                    ctx = result.objectId,
                    details = mapOf(Relations.IS_HIDDEN to true)
                )
            ).fold(
                onSuccess = { /* hidden */ },
                onFailure = { Timber.e(it, "Quick capture: could not hide draft") }
            )
        }
    }

    //region Space picker

    fun onSpaceChipClicked() {
        showSpacePicker.value = true
    }

    fun onSpacePickerDismissed() {
        showSpacePicker.value = false
    }

    /**
     * @param sourceHasContent live emptiness reported by the embedded editor. The details
     * subscription lags the editor's text debounce, so trusting it alone would move (and
     * resurrect the title of) a draft the user has just emptied. Null = unknown, fall back
     * to the subscription.
     */
    fun onSpaceSelected(
        target: Id,
        sourceHasContent: Boolean? = null,
        sourceEditedThisSession: Boolean = true
    ) {
        showSpacePicker.value = false
        val current = screenState.value
        if (current is ScreenState.Ready && current.space.id == target) return
        if (current is ScreenState.Loading) return // a transition is already in flight
        // Publish in flight: the object screenState names is already public, and moving it
        // would delete the user's real note as the move's source.
        if (isSending.get()) return
        pendingSourceHasContent = sourceHasContent
        viewModelScope.launch {
            // Two drafts with content and one sheet: the user has to say what happens to the
            // one they can see. The target's draft is never touched — it is off-screen, so
            // any automatic decision about it would be a blind delete.
            if (current is ScreenState.Ready && !isSourceDraftEmpty()) {
                val existing = runCatching { settings.getQuickCaptureDraft(SpaceId(target)) }
                    .getOrNull()
                if (existing != null && draftHasContent(space = SpaceId(target), draft = existing)) {
                    draftConflict.value = DraftConflict(
                        space = target,
                        spaceName = spaceNameOf(target)
                    )
                    return@launch
                }
                // Nothing to lose in the target space, but the text here is not this
                // session's — moving it would relocate a note the user only reopened.
                if (!sourceEditedThisSession) {
                    moveOrNewPrompt.value = MoveOrNewRequest(
                        space = target,
                        spaceName = spaceNameOf(target)
                    )
                    return@launch
                }
            }
            proceedWithSpaceSelection(target, SwitchMode.MOVE_CURRENT)
        }
    }

    /** Conflict resolved: throw away the visible draft, then open the target's own. */
    fun onDiscardCurrentDraftChosen() {
        val request = draftConflict.value ?: return
        draftConflict.value = null
        viewModelScope.launch {
            proceedWithSpaceSelection(request.space, SwitchMode.DISCARD_CURRENT)
        }
    }

    /** Conflict resolved: leave the visible draft in its space, then open the target's own. */
    fun onKeepCurrentDraftChosen() {
        val request = draftConflict.value ?: return
        draftConflict.value = null
        viewModelScope.launch {
            proceedWithSpaceSelection(request.space, SwitchMode.KEEP_CURRENT)
        }
    }

    fun onDraftConflictCancelled() {
        draftConflict.value = null
    }

    /** Untouched draft, empty target: carry the note across, as the chip normally implies. */
    fun onMoveDraftChosen() {
        val request = moveOrNewPrompt.value ?: return
        moveOrNewPrompt.value = null
        viewModelScope.launch {
            proceedWithSpaceSelection(request.space, SwitchMode.MOVE_CURRENT)
        }
    }

    /** Untouched draft, empty target: leave the note where it is and start a fresh one. */
    fun onStartNewDraftChosen() {
        val request = moveOrNewPrompt.value ?: return
        moveOrNewPrompt.value = null
        viewModelScope.launch {
            proceedWithSpaceSelection(request.space, SwitchMode.KEEP_CURRENT)
        }
    }

    fun onMoveOrNewCancelled() {
        moveOrNewPrompt.value = null
    }

    private fun spaceNameOf(target: Id): String =
        spaces.value.firstOrNull { it.targetSpaceId == target }?.name.orEmpty()

    /**
     * True only when this object is safe to delete without asking: it fetched cleanly, is
     * still an unpublished draft, and holds nothing.
     *
     * Every uncertainty fails CLOSED. That is the difference from [draftHasContent], which
     * answers "should we prompt?" and fails open — a fetch error there costs a needless
     * dialog, whereas the same error here would cost the user their note. The delete site
     * must establish its own precondition rather than trust a decision taken earlier, before
     * the target space was even opened.
     */
    private suspend fun isDisposableDraft(space: SpaceId, draft: Id): Boolean {
        val obj = fetchOwnDraft(space, draft) ?: return false
        // Not a draft any more (published, archived, deleted) — never ours to delete.
        if (obj.isDeleted == true || obj.isArchived == true || obj.isHidden != true) return false
        return obj.name.isNullOrBlank() && obj.snippet.isNullOrBlank()
    }

    /** True when the target space's stored draft still exists and holds real content. */
    private suspend fun draftHasContent(space: SpaceId, draft: Id): Boolean {
        val obj = fetchOwnDraft(space, draft) ?: return false
        if (obj.isDeleted == true || obj.isArchived == true || obj.isHidden != true) return false
        return !obj.name.isNullOrBlank() || !obj.snippet.isNullOrBlank()
    }

    private suspend fun proceedWithSpaceSelection(target: Id, mode: SwitchMode) {
        val current = screenState.value
        if (current !is ScreenState.Ready) {
            proceedWithSpace(SpaceId(target))
            return
        }
        spaceManager.set(target).fold(
            onFailure = {
                Timber.e(it, "Quick capture: could not open space $target")
                sendToast(SOMETHING_WENT_WRONG)
            },
            onSuccess = {
                markSelectedSpace(SpaceId(target))
                when {
                    mode == SwitchMode.DISCARD_CURRENT -> {
                        // Loading detaches the editor first. Without it the editor stays bound
                        // to the object about to be deleted, still firing text writes at it,
                        // and the re-entrancy guard above stays disarmed for the whole round
                        // trip. Same reason the other branches take it.
                        screenState.value = ScreenState.Loading
                        discardDraft(current)
                        ensureDraft(SpaceId(target))
                    }
                    // The current draft stays put; the target opens its own. Still detach:
                    // the type/relation stores have already switched space beneath it.
                    mode == SwitchMode.KEEP_CURRENT -> {
                        screenState.value = ScreenState.Loading
                        ensureDraft(SpaceId(target))
                    }
                    isSourceDraftEmpty() -> {
                        // Nothing typed — just open/create the target space's own draft.
                        screenState.value = ScreenState.Loading
                        ensureDraft(SpaceId(target))
                    }
                    else -> {
                        // Includes the "subscription hasn't confirmed emptiness yet" case:
                        // moving a possibly-empty draft is harmless, dropping typed text is not.
                        retargetDraft(from = current, to = SpaceId(target))
                    }
                }
            }
        )
    }

    /**
     * Deletes the draft the user is looking at, on their explicit instruction. Delete first,
     * clear the pointer second: if the delete fails the pointer still names a live draft and
     * the text is recoverable, whereas the reverse order would strand it.
     */
    private suspend fun discardDraft(current: ScreenState.Ready) {
        deleteObjects.async(DeleteObjects.Params(listOf(current.draft))).fold(
            onSuccess = {
                Timber.d("Quick capture: discarded draft in ${current.space.id}")
                // Only now: clearing on a failed delete would strand a hidden object that no
                // pointer names, which is unreachable by construction. Keeping the pointer
                // leaves the draft where the user can still get back to it.
                runCatching { settings.clearQuickCaptureDraft(current.space) }
                    .onFailure { Timber.w(it, "Quick capture: could not clear discarded pointer") }
            },
            onFailure = {
                Timber.w(it, "Quick capture: could not discard draft — keeping its pointer")
                sendToast(SOMETHING_WENT_WRONG)
            }
        )
        refreshDraftSpaces()
    }

    /** "Text follows the chip": the typed content moves to the newly selected space. */
    private suspend fun retargetDraft(from: ScreenState.Ready, to: SpaceId) {
        screenState.value = ScreenState.Loading
        val replaced = settings.getQuickCaptureDraft(to)
        moveQuickCaptureDraft.async(
            MoveQuickCaptureDraft.Params(
                from = from.draft,
                fromSpace = from.space,
                toSpace = to
            )
        ).fold(
            onSuccess = { newDraft ->
                // The draft pointers were already rewritten inside the use case, on the
                // uninterruptible side of the commit: this lambda is NOT reached when the
                // sheet is dismissed mid-move, because ResultInteractor.async wraps doWork in
                // withContext, which rethrows CancellationException on resume if the caller's
                // scope died. Only the discretionary cleanup below may live here.
                withContext(NonCancellable) {
                    // Only reachable when the target's draft was EMPTY: a target draft with
                    // content raises the conflict dialog instead and never moves. So this
                    // disposes an empty placeholder, not anything the user typed.
                    //
                    // Still after the move, not before: deleting first would destroy it for
                    // nothing on failure (Object.ListDelete is permanent, not move-to-bin).
                    //
                    // Re-validate immediately before deleting rather than trusting the
                    // pointer. A pointer can outlive the draft it named — a send publishes
                    // the object and only then clears it — and deleting on a stale pointer
                    // would destroy a real, published note.
                    //
                    // isDisposableDraft re-establishes the FULL precondition here, now that
                    // the target space is actually open: still an unpublished draft, and
                    // empty. Checking only "does it still exist" would trust the emptiness
                    // decision taken back in onSpaceSelected — which ran before
                    // spaceManager.set(), against a possibly stale index, and which treats a
                    // failed fetch as "no content". A single transient error there would
                    // otherwise land here as a permanent delete of someone's note.
                    if (replaced != null && replaced != newDraft) {
                        if (isDisposableDraft(space = to, draft = replaced)) {
                            deleteObjects.async(DeleteObjects.Params(listOf(replaced))).fold(
                                onSuccess = { /* replaced */ },
                                onFailure = {
                                    // Orphaned hidden draft; empties are GC'd by heart on close.
                                    Timber.w(it, "Quick capture: could not delete replaced draft")
                                }
                            )
                        } else {
                            Timber.d("Quick capture: ${to.id} draft not disposable, keeping it")
                        }
                    }
                }
                screenState.value = ScreenState.Ready(space = to, draft = newDraft)
                refreshDraftSpaces()
            },
            onFailure = {
                Timber.e(it, "Quick capture: could not move draft to ${to.id}")
                sendToast(SOMETHING_WENT_WRONG)
                // Fall back to the original draft — the thought is never lost.
                markSelectedSpace(from.space)
                spaceManager.set(from.space.id).fold(
                    onSuccess = { screenState.value = from },
                    onFailure = { error ->
                        // Cannot restore a coherent editor (stores belong to another space) —
                        // close the sheet; the draft and its pointer are intact.
                        Timber.e(error, "Quick capture: could not re-open source space")
                        _commands.send(Command.Dismiss())
                    }
                )
            }
        )
    }

    //endregion

    //region Clear draft

    fun onClearDraftClicked() {
        if (isSending.get()) return // see isSending: the object is published, not a draft
        if (!isDraftEmpty.value) {
            showClearDraftConfirmation.value = true
        }
    }

    fun onClearDraftCancelled() {
        showClearDraftConfirmation.value = false
    }

    fun onClearDraftConfirmed() {
        showClearDraftConfirmation.value = false
        val current = screenState.value
        if (current !is ScreenState.Ready) return
        // A send that started while this dialog was up: the target is a published note now.
        if (isSending.get()) return
        viewModelScope.launch {
            screenState.value = ScreenState.Loading
            deleteObjects.async(DeleteObjects.Params(listOf(current.draft))).fold(
                onSuccess = {
                    settings.clearQuickCaptureDraft(current.space)
                    createDraft(current.space)
                },
                onFailure = {
                    Timber.e(it, "Quick capture: could not clear draft")
                    sendToast(SOMETHING_WENT_WRONG)
                    screenState.value = current
                }
            )
        }
    }

    //endregion

    //region Send

    fun onSendClicked() {
        val current = screenState.value
        if (current !is ScreenState.Ready || isDraftEmpty.value) return
        // Nothing observable changes fast enough to re-disable the button: screenState stays
        // Ready and the draft still has content, while the sheet remains tappable through its
        // dismiss animation. The UI throttle cannot cover this — noRippleThrottledClickable
        // is not remembered, so any recomposition (the sync status ticks constantly) resets
        // its window. Without this latch a double tap logs objectCreate twice, which is the
        // exact inflation the "log at commit, not at draft creation" rule exists to prevent.
        if (!isSending.compareAndSet(false, true)) return
        viewModelScope.launch {
            setObjectDetails.async(
                SetObjectDetails.Params(
                    ctx = current.draft,
                    details = mapOf(Relations.IS_HIDDEN to false)
                )
            ).fold(
                onFailure = {
                    Timber.e(it, "Quick capture: publish failed")
                    sendToast(SOMETHING_WENT_WRONG)
                    // Nothing was published — let the user try again.
                    isSending.set(false)
                },
                onSuccess = {
                    // The object is published — the bookkeeping and the Dismiss command must
                    // complete even if the user backgrounds the app right now. The buffered
                    // command channel delivers Dismiss on the next sheet start if needed.
                    withContext(NonCancellable) {
                        // Guarded like its neighbours: an escaping exception here would both
                        // crash (this is a bare viewModelScope.launch with no handler) and
                        // skip the Dismiss below, leaving the pointer aimed at an object that
                        // is now published — the exact state that used to make a later space
                        // switch delete a real note.
                        runCatching { settings.clearQuickCaptureDraft(current.space) }
                            .onFailure { Timber.w(it, "Quick capture: could not clear draft pointer") }
                        runCatching {
                            settings.setSpaceLastInteraction(
                                space = current.space,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        val details = draftDetails.value
                        val typeObj = details?.type?.firstOrNull()
                            ?.let { storeOfObjectTypes.get(it) }
                        typeObj?.let { type -> markTypeAsRecentlyUsed(type.id) }
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            route = EventsDictionary.Routes.quickCapture,
                            objType = typeObj,
                            spaceParams = provideParams(current.space.id)
                        )
                        _commands.send(
                            Command.Dismiss(
                                result = PublishedResult(
                                    objectId = current.draft,
                                    spaceId = current.space.id,
                                    typeName = typeObj?.name.orEmpty(),
                                    spaceName = selectedSpace.value?.name.orEmpty()
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    //endregion

    /**
     * Type recency for the type bar's "recently used first" ordering.
     *
     * Heart bumps `lastUsedDate` on Object.Create, but quick capture creates with the
     * space's default type and the user then *changes* it via SetObjectType — which does
     * not bump it. Without this write the bar could never learn from captures at all
     * (spec V6b). Best-effort: a rejected write must never fail a send.
     */
    private suspend fun markTypeAsRecentlyUsed(typeId: Id) {
        setObjectDetails.async(
            SetObjectDetails.Params(
                ctx = typeId,
                details = mapOf(
                    Relations.LAST_USED_DATE to (System.currentTimeMillis() / 1000).toDouble()
                )
            )
        ).fold(
            onSuccess = { Timber.d("Quick capture: bumped lastUsedDate for type $typeId") },
            onFailure = { Timber.w(it, "Quick capture: could not bump lastUsedDate for $typeId") }
        )
    }

    //region Sync status

    fun onSyncStatusBadgeClicked() {
        syncStatusWidget.value = syncStatus.value.toSyncStatusWidgetState()
    }

    fun onSyncWidgetDismiss() {
        syncStatusWidget.value = SyncStatusWidgetState.Hidden
    }

    //endregion

    /**
     * Real teardown only (dismiss, back, navigation away) — NOT config changes, which
     * recreate the view but retain this VM. viewModelScope is already cancelled here, so
     * no in-flight `spaceManager.set()` can resurrect the space after this clear; the
     * draft ids-subscription is closed by [draftDetails]' onCompletion. The draft itself
     * (with its pointer) is kept — it restores on the next pencil tap.
     */
    override fun onCleared() {
        super.onCleared()
        spaceManager.clear()
    }

    class Factory @Inject constructor(
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val userPermissionProvider: UserPermissionProvider,
        private val spaceManager: SpaceManager,
        private val settings: UserSettingsRepository,
        private val createObject: CreateObject,
        private val setObjectDetails: SetObjectDetails,
        private val deleteObjects: DeleteObjects,
        private val moveQuickCaptureDraft: MoveQuickCaptureDraft,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
        private val typeSuggestionEngine: TypeSuggestionEngine,
        private val fetchQuickCaptureDraft: FetchQuickCaptureDraft
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuickCaptureViewModel(
                spaceViews = spaceViews,
                userPermissionProvider = userPermissionProvider,
                spaceManager = spaceManager,
                settings = settings,
                createObject = createObject,
                setObjectDetails = setObjectDetails,
                deleteObjects = deleteObjects,
                moveQuickCaptureDraft = moveQuickCaptureDraft,
                storelessSubscriptionContainer = storelessSubscriptionContainer,
                storeOfObjectTypes = storeOfObjectTypes,
                urlBuilder = urlBuilder,
                analytics = analytics,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
                typeSuggestionEngine = typeSuggestionEngine,
                fetchQuickCaptureDraft = fetchQuickCaptureDraft
            ) as T
        }
    }

    companion object {
        const val QUICK_CAPTURE_DRAFT_SUBSCRIPTION = "quick-capture-draft-subscription"
        private const val SOMETHING_WENT_WRONG = "Something went wrong. Please, try again."
        private const val SPACES_AWAIT_TIMEOUT_MS = 3_000L
        private val DRAFT_VALIDATION_KEYS = listOf(
            Relations.ID,
            Relations.IS_DELETED,
            Relations.IS_ARCHIVED,
            Relations.IS_HIDDEN,
            Relations.INTERNAL_FLAGS,
            Relations.NAME,
            // Scopes every draft decision to objects this account created: in a shared space
            // a pointer must never let us adopt or delete another member's hidden object.
            Relations.CREATOR,
            // Emptiness is content-based (spec §15), and body text lives in the snippet.
            // Without this key `snippet` reads null for every fetched draft and
            // draftHasContent() silently degenerates to a title-only check — which would
            // skip the replace confirmation and permanently delete a body-only note.
            Relations.SNIPPET,
            Relations.TYPE,
            Relations.LAYOUT
        )
    }
}

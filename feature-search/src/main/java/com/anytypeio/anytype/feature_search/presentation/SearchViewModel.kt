package com.anytypeio.anytype.feature_search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.ChatMessageSort
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.chats.SearchChatMessages
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CrossSpaceObjectTypesContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.CrossSpaceSearchObjects
import com.anytypeio.anytype.domain.search.SearchWithMeta
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.DeepLinkToObjectDelegate
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.core_ui.R as UiR
import com.anytypeio.anytype.localization.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * The unified search surface: one ViewModel, three scopes (current space /
 * another space / global), scope and filters as removable tokens, an adaptive
 * suggestion-chip row and three long-tail picker sheets — the token model the
 * desktop and iOS clients converged on.
 *
 * Loader selection (in order): focus tokens -> local listings; Messages kind
 * or chat filter -> scoped chat search; Channels kind -> in-memory vault
 * list; non-current scope -> one-shot CrossSpaceSearch; else in-space
 * SearchWithMeta (the only path with fulltext highlights). All result
 * attribution (space names/icons, authors, type captions, chat containers)
 * is joined against app-lifetime cross-space stores — never fetched per row
 * or per space. The only sanctioned extra RPC is a once-per-page batch fetch
 * for unresolved message containers, with a negative cache.
 */
class SearchViewModel(
    private val vmParams: VmParams,
    private val searchWithMeta: SearchWithMeta,
    private val crossSpaceSearch: CrossSpaceSearchObjects,
    private val searchChatMessages: SearchChatMessages,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val participants: ParticipantSubscriptionContainer,
    private val chatsDetails: ChatsDetailsSubscriptionContainer,
    private val crossSpaceTypes: CrossSpaceObjectTypesContainer,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder,
    private val fieldParser: FieldParser,
    private val dispatchers: AppCoroutineDispatchers,
    private val deepLinkToObject: DeepLinkToObjectDelegate,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace
) : ViewModel() {

    /**
     * [entrySpace] null = opened from the vault (global mode). [chat] seeds a
     * chat filter token (in-chat entry). [excludedIds] hides already-attached
     * objects in the ATTACH_TO_CHAT purpose.
     */
    data class VmParams(
        val entrySpace: SpaceId?,
        val chat: Id? = null,
        val purpose: SearchPurpose = SearchPurpose.NAVIGATION,
        val excludedIds: List<Id> = emptyList()
    )

    private val isPicker get() = vmParams.purpose == SearchPurpose.ATTACH_TO_CHAT

    private val tokens = MutableStateFlow(initialTokens())
    private val rawInput = MutableStateFlow("")
    private val browseSort = MutableStateFlow(BrowseSort.EDITED)

    /**
     * Load-more is a signal, not shared state: the page counter lives inside
     * each base request's collection (see the pipeline), so a stale signal
     * can never inflate a fresh query's page count.
     */
    private val loadMoreRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val input: StateFlow<String> = rawInput

    /** Tap-selected pill (accent state); second backspace removes it. */
    val selectedTokenId = MutableStateFlow<String?>(null)

    /** Which long-tail picker sheet is open, if any. */
    val activePicker = MutableStateFlow<PickerType?>(null)

    /**
     * A Channel, not a SharedFlow: a navigation command emitted while the view
     * is being recreated (rotation mid-RPC) must be buffered and delivered to
     * the next collector, never dropped — by the time it is emitted the space
     * switch may already have happened.
     */
    private val commandsChannel = Channel<SearchNavigation>(Channel.BUFFERED)
    val commands: Flow<SearchNavigation> = commandsChannel.receiveAsFlow()

    /** Message containers that neither the store nor a batch fetch resolved. */
    private val unresolvableContainerIds = mutableSetOf<Id>()
    private val resolvedContainers = mutableMapOf<Id, ObjectWrapper.Basic>()

    private fun initialTokens(): List<SearchToken> = buildList {
        val space = vmParams.entrySpace
        if (space != null) {
            add(SearchToken.SpaceScope(space.id))
            val chat = vmParams.chat
            if (chat != null) add(SearchToken.ChatFilter(chat = chat, space = space.id))
        }
    }

    // region pipeline

    private data class Request(
        val tokens: List<SearchToken>,
        val query: String,
        val pages: Int,
        val sort: BrowseSort
    )

    // Typing pays the debounce; a clear (drill, backspace-to-empty) applies at
    // once so a token mutation never searches with the stale query. One
    // operator, one subscription — a take(1)/drop(1) split leaves a gap
    // between two subscriptions where a keystroke is lost.
    private val queries = rawInput
        .debounce { value -> if (value.isEmpty()) 0L else DEBOUNCE_MS }
        .distinctUntilChanged()

    private var lastLoaded = SearchUiState(isLoading = true)
    private var loadEpochCounter = 0L

    /**
     * Rows accumulated across the appended pages of ONE base request. Only
     * ever touched inside [load], which flatMapLatest serializes — page 1
     * replaces it, later pages append after cross-page id dedup.
     */
    private class PageAccumulator {
        val ids = mutableSetOf<Id>()
        val objectRows = mutableListOf<SearchResultView.ObjectRow>()
        val records = mutableListOf<ObjectWrapper.Basic>()
        val messageRows = mutableListOf<SearchResultView.MessageRow>()
    }

    private var pageAcc = PageAccumulator()

    private data class BaseRequest(
        val tokens: List<SearchToken>,
        val query: String,
        val sort: BrowseSort
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = combine(
        tokens, queries, browseSort
    ) { tokens, query, sort ->
        BaseRequest(tokens, query, sort)
    }
        .distinctUntilChanged()
        .flatMapLatest { base ->
            // The page counter is scan-local to this base's collection: a new
            // base always starts at page 1 with no shared counter to reset,
            // and a load-more signal for a cancelled base dies with its flow.
            loadMoreRequests
                .scan(1) { page, _ -> page + 1 }
                .map { page -> Request(base.tokens, base.query, page, base.sort) }
        }
        .distinctUntilChanged()
        .flatMapLatest { request ->
            flow {
                // Quiet reload: keep the on-screen list (stamped with the mode
                // it was loaded for) while the new one is in flight.
                emit(lastLoaded.copy(isLoading = true))
                // A fresh (non-append) load resets the list scroll position.
                val epoch = if (request.pages == 1) ++loadEpochCounter else loadEpochCounter
                val loaded = try {
                    load(request)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // A cancelled load must cancel — swallowing it corrupts
                    // lastLoaded and leaks a bogus empty state.
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Search load failed")
                    // Degrade to the empty state — never stale rows.
                    SearchUiState(isEmpty = true, loadedScope = request.scope())
                }.copy(loadEpoch = epoch)
                lastLoaded = loaded
                emit(loaded)
            }
        }
        .flowOn(dispatchers.io)
        // Lazily, not WhileSubscribed: navigating into a result must preserve
        // the loaded list, scroll position and appended pages — a sharing
        // restart would re-run the RPC and collapse back to page 1. The VM is
        // screen-scoped, so the pipeline dies with the screen anyway.
        .stateIn(viewModelScope, SharingStarted.Lazily, SearchUiState(isLoading = true))

    private fun Request.scope(): SearchScope {
        val scope = tokens.filterIsInstance<SearchToken.SpaceScope>().firstOrNull()
        return if (scope == null) {
            SearchScope.Global
        } else {
            SearchScope.Space(
                space = scope.space,
                isCurrent = scope.space == vmParams.entrySpace?.id
            )
        }
    }

    private fun Request.what(): SearchToken? =
        tokens.firstOrNull { it.group == TokenGroup.WHAT }

    private fun Request.creator(): SearchToken.Creator? =
        tokens.filterIsInstance<SearchToken.Creator>().firstOrNull()

    private fun Request.bucket(): KindBucket? =
        (what() as? SearchToken.Kind)?.bucket

    private fun Request.chatFilter(): SearchToken.ChatFilter? =
        tokens.filterIsInstance<SearchToken.ChatFilter>().firstOrNull()

    private fun Request.isBrowse(): Boolean = query.isBlank()

    /**
     * Lead groups (Channels / People / Types) show only in global mode with a
     * non-blank query and no non-scope token — any filter token means the
     * query is already about something narrower.
     */
    private fun Request.showsLeadRows(): Boolean =
        scope() is SearchScope.Global &&
            query.isNotBlank() &&
            tokens.none { it.group != TokenGroup.SCOPE }

    private suspend fun load(request: Request): SearchUiState {
        val scope = request.scope()
        val typeFocus = request.tokens.filterIsInstance<SearchToken.TypeFocus>().firstOrNull()
        val personFocus = request.tokens.filterIsInstance<SearchToken.PersonFocus>().firstOrNull()
        return when {
            // Focused listings are local and instant — no RPC, no debounce.
            typeFocus != null -> loadTypeFocus(request, typeFocus)
            personFocus != null -> loadPersonFocus(request, personFocus)
            request.chatFilter() != null || request.bucket() == KindBucket.MESSAGE ->
                loadMessages(request, scope)
            request.bucket() == KindBucket.CHANNEL -> loadChannelBrowse(request)
            scope is SearchScope.Space && scope.isCurrent -> loadInSpace(request, scope)
            else -> loadCrossSpace(request, scope)
        }
    }

    // endregion

    // region loaders

    private suspend fun loadInSpace(
        request: Request,
        scope: SearchScope.Space
    ): SearchUiState {
        val isBrowse = request.isBrowse()
        val limit = pageLimit(isBrowse, request.pages)
        val filters = buildList {
            addAll(SearchFilters.base())
            request.bucket()?.let { addAll(SearchFilters.kindBucket(it)) }
            (request.what() as? SearchToken.TypeFilter)?.let { addAll(SearchFilters.typeToken(it)) }
            request.creator()?.let { token ->
                addAll(
                    SearchFilters.creatorToken(
                        identity = token.identity,
                        participantIds = participantIdsOf(token.identity),
                        whatIsChatsBucket = request.bucket() == KindBucket.CHAT
                    )
                )
            }
            if (isBrowse && request.what() == null) add(SearchFilters.excludeTypeObjects())
            if (vmParams.excludedIds.isNotEmpty()) add(SearchFilters.excludeIds(vmParams.excludedIds))
        }
        val sorts: List<DVSort> =
            if (isBrowse) SearchFilters.browseSorts(request.bucket(), request.sort) else emptyList()
        val raw = searchWithMeta.run(
            SearchWithMeta.Params(
                command = Command.SearchWithMeta(
                    query = request.query,
                    limit = limit,
                    offset = pageOffset(isBrowse, request.pages),
                    keys = OBJECT_KEYS,
                    sorts = sorts,
                    filters = filters,
                    withMeta = !isBrowse,
                    withMetaRelationDetails = false,
                    space = SpaceId(scope.space)
                ),
                relatedObjectId = null,
                saveSearch = false
            )
        )
        // Page on the RAW count — deduped counts under-fill pages (iOS B20).
        val hasMore = raw.size >= limit
        if (request.pages == 1) pageAcc = PageAccumulator()
        // Set.add doubles as in-page AND cross-page dedup — duplicate keys
        // crash LazyColumn.
        val pageResults = raw.filter { pageAcc.ids.add(it.obj) }
        // One store snapshot per load — never a (locked) store read per row.
        val typeById = storeOfObjectTypes.getAll().associateBy { it.id }
        pageResults.forEach { result ->
            pageAcc.objectRows.add(result.toObjectRow(typeById))
            pageAcc.records.add(result.wrapper)
        }
        val objectRows = pageAcc.objectRows.toList()
        val rows =
            if (isBrowse) groupBrowseRows(objectRows, pageAcc.records, request)
            else objectRows
        val empty = objectRows.isEmpty()
        // An empty text search teaches the Messages filter — but only where
        // the scope actually has chats to search (JS-9865 rev 5).
        if (empty && !isBrowse && request.what() == null && request.creator() == null &&
            !isPicker && spaceHasChats(scope.space)
        ) {
            return SearchUiState(
                results = listOf(SearchResultView.MessagesTutorialRow),
                isEmpty = false,
                loadedScope = SearchScope.Space(scope.space, isCurrent = true),
                hasMore = false
            )
        }
        return SearchUiState(
            results = rows,
            isEmpty = empty,
            loadedScope = SearchScope.Space(scope.space, isCurrent = true),
            hasMore = hasMore
        )
    }

    private suspend fun loadCrossSpace(
        request: Request,
        scope: SearchScope
    ): SearchUiState {
        val isBrowse = request.isBrowse()
        val isChatsBucket = request.bucket() == KindBucket.CHAT
        // Browse pages on a deterministic sort, so it can append per-page.
        // Fulltext relevance order is a merge across per-space stores with no
        // offset-stability guarantee — it stays on a growing limit instead.
        val limit = if (isBrowse) pageLimit(isBrowse, request.pages) else PAGE_SIZE * request.pages
        var fullText = request.query
        val filters = buildList {
            addAll(SearchFilters.base())
            if (scope is SearchScope.Space) add(SearchFilters.spaceScope(scope.space))
            request.bucket()?.let { addAll(SearchFilters.kindBucket(it)) }
            (request.what() as? SearchToken.TypeFilter)?.let { addAll(SearchFilters.typeToken(it)) }
            request.creator()?.let { token ->
                addAll(
                    SearchFilters.creatorToken(
                        identity = token.identity,
                        participantIds = participantIdsOf(token.identity),
                        whatIsChatsBucket = isChatsBucket
                    )
                )
            }
            // Type objects are noise in the generic browse; the Types lead
            // group also replaces per-space type rows in plain text queries.
            if (request.what() == null && (isBrowse || request.showsLeadRows())) {
                add(SearchFilters.excludeTypeObjects())
            }
            // Chat objects are not in the fulltext index — filter by name instead.
            if (isChatsBucket && fullText.isNotBlank()) {
                add(SearchFilters.chatNameQuery(fullText))
                fullText = ""
            }
            if (vmParams.excludedIds.isNotEmpty()) add(SearchFilters.excludeIds(vmParams.excludedIds))
        }
        // The Chats bucket always reads in last-message order, text query or
        // not — its name filter is a plain filter, so sorts still apply.
        val sorts: List<DVSort> =
            if (isBrowse || isChatsBucket) SearchFilters.browseSorts(request.bucket(), request.sort)
            else emptyList()
        val result = crossSpaceSearch.run(
            Command.CrossSpaceSearch(
                query = fullText,
                filters = filters,
                sorts = sorts,
                offset = if (isBrowse) pageOffset(true, request.pages) else 0,
                limit = limit,
                keys = OBJECT_KEYS
            )
        )
        // Page on the RAW count — deduped counts under-fill pages (iOS B20).
        val hasMore = result.records.size >= limit
        // One store snapshot per load — never a full-store scan per row.
        val typeById = crossSpaceTypes.get().associateBy { it.id }
        val spaceNames = spaceNameIndex()
        val rows: List<SearchResultView>
        val empty: Boolean
        if (isBrowse) {
            if (request.pages == 1) pageAcc = PageAccumulator()
            // Set.add doubles as in-page AND cross-page dedup — duplicate
            // keys crash LazyColumn.
            val pageRecords = result.records.filter { pageAcc.ids.add(it.id) }
            pageRecords.forEach { record ->
                pageAcc.objectRows.add(record.toCrossSpaceObjectRow(typeById, spaceNames))
                pageAcc.records.add(record)
            }
            rows = groupBrowseRows(pageAcc.objectRows.toList(), pageAcc.records, request)
            empty = pageAcc.objectRows.isEmpty()
        } else {
            // Growing-limit fulltext: every page is a full refetch, dedup in place.
            val records = result.records.distinctBy { it.id }
            val objectRows = records.map { it.toCrossSpaceObjectRow(typeById, spaceNames) }
            rows = buildList {
                var hasLeadRows = false
                if (request.showsLeadRows()) {
                    hasLeadRows = addLeadRows(request.query)
                }
                if (hasLeadRows && objectRows.isNotEmpty()) {
                    add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_section_objects))
                }
                addAll(objectRows)
            }
            empty = rows.none {
                it is SearchResultView.ObjectRow || it is SearchResultView.ChannelRow ||
                    it is SearchResultView.PersonRow || it is SearchResultView.TypeAggRow
            }
        }
        // An empty text search teaches the Messages filter — but only where
        // the scope actually has chats to search (JS-9865 rev 5).
        if (empty && !isBrowse && request.what() == null && request.creator() == null &&
            !isPicker && spaceHasChats((scope as? SearchScope.Space)?.space)
        ) {
            return SearchUiState(
                results = listOf(SearchResultView.MessagesTutorialRow),
                isEmpty = false,
                loadedScope = scope,
                hasMore = false
            )
        }
        return SearchUiState(
            results = rows,
            isEmpty = empty,
            loadedScope = scope,
            hasMore = hasMore
        )
    }

    private suspend fun loadMessages(
        request: Request,
        scope: SearchScope
    ): SearchUiState {
        val isBrowse = request.isBrowse()
        val limit = pageLimit(isBrowse, request.pages)
        // A chat filter pins its own chat AND space — it outlives the scope token.
        val chatFilter = request.chatFilter()
        val response = searchChatMessages.run(
            Command.ChatCommand.SearchMessages(
                space = SpaceId(
                    chatFilter?.space ?: (scope as? SearchScope.Space)?.space.orEmpty()
                ),
                chat = chatFilter?.chat,
                query = request.query,
                offset = pageOffset(isBrowse, request.pages),
                limit = limit,
                // Always newest-first, for text queries too: relevance order
                // groups hits per chat and reads as arbitrary. The fixed sort
                // also makes offset paging deterministic.
                sorts = listOf(
                    ChatMessageSort(key = ChatMessageSort.Key.CREATED_AT, descending = true)
                ),
                creators = request.creator()?.let { listOf(it.identity) } ?: emptyList()
            )
        )
        // Page on the RAW count — deduped counts under-fill pages (iOS B20).
        val hasMore = response.results.size >= limit
        if (request.pages == 1) pageAcc = PageAccumulator()
        // Set.add doubles as in-page AND cross-page dedup — new arrivals shift
        // offsets, and duplicate keys crash LazyColumn.
        val results = response.results.filter { pageAcc.ids.add(it.messageId) }
        resolveContainers(results.map { it.chatId })
        val isCrossSpace = scope !is SearchScope.Space
        // One store snapshot per load — never a participant or chat-container
        // scan per row.
        val memberBySpaceIdentity = participants.get()
            .associateBy { member -> member.spaceId.orEmpty() to member.identity }
        val containerById = chatsDetails.get().associateBy { it.id }
        val spaceNames = spaceNameIndex()
        results.forEach { result ->
            val spaceId = result.spaceId.ifEmpty { (scope as? SearchScope.Space)?.space.orEmpty() }
            val author = memberBySpaceIdentity[spaceId to result.message.creator]
            val container = containerById[result.chatId] ?: resolvedContainers[result.chatId]
            // A discussion-parent container carries its own id under the
            // discussion's key — that difference marks a discussion row.
            val discussionParent = container?.id?.takeIf { it != result.chatId }
            pageAcc.messageRows.add(
                SearchResultView.MessageRow(
                    messageId = result.messageId,
                    chatId = result.chatId,
                    space = spaceId,
                    authorName = author?.name.orEmpty(),
                    authorIcon = author?.memberIcon(),
                    authorIdentity = result.message.creator,
                    createdAt = result.message.createdAt,
                    text = result.highlight.ifEmpty { result.message.plainText() },
                    highlights = if (result.highlight.isNotEmpty()) result.highlightRanges else emptyList(),
                    containerName = container?.name,
                    discussionParentId = discussionParent,
                    spaceName = if (isCrossSpace) spaceNames[spaceId] else null
                )
            )
        }
        val rows = buildList {
            if (isBrowse && pageAcc.messageRows.isNotEmpty()) {
                add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_section_recent_messages))
            }
            addAll(pageAcc.messageRows)
        }
        return SearchUiState(
            results = rows,
            isEmpty = pageAcc.messageRows.isEmpty(),
            loadedScope = scope,
            hasMore = hasMore
        )
    }

    /**
     * Three stages, one batch per page, never per row (iOS §7.6): the
     * vault-wide chat-container store; then parents whose `discussionId`
     * points at one of the unresolved ids — the parent's name captions the
     * row and is the object a discussion message opens; then a plain
     * `id In [...]` fetch accepting chat layouts only. Ids no stage resolves
     * are negative-cached so later pages never re-query them.
     */
    private suspend fun resolveContainers(chatIds: List<Id>) {
        // One store snapshot, not a store scan per id.
        val known = chatsDetails.get().mapTo(mutableSetOf()) { it.id }
        val unresolved = chatIds.toSet()
            .filter { id ->
                id !in known &&
                    resolvedContainers[id] == null &&
                    id !in unresolvableContainerIds
            }
            .toMutableSet()
        if (unresolved.isEmpty()) return

        // Stage 2: discussion parents — keyed by the DISCUSSION's id, while
        // the record carries its own (that asymmetry is what routes opening
        // to the parent editor).
        runCatching {
            crossSpaceSearch.run(
                Command.CrossSpaceSearch(
                    query = "",
                    filters = listOf(
                        DVFilter(
                            relation = Relations.DISCUSSION_ID,
                            condition = DVFilterCondition.IN,
                            value = unresolved.toList()
                        )
                    ),
                    offset = 0,
                    limit = unresolved.size,
                    keys = CONTAINER_KEYS
                )
            )
        }.onSuccess { result ->
            result.records.forEach { record ->
                val discussionId = record.getValue<Id>(Relations.DISCUSSION_ID)
                if (!discussionId.isNullOrEmpty() && discussionId in unresolved) {
                    resolvedContainers[discussionId] = record
                    unresolved.remove(discussionId)
                }
            }
        }
        if (unresolved.isEmpty()) return

        // Stage 3: by id, chat layouts only — a bare Discussion object
        // deliberately renders no caption.
        runCatching {
            crossSpaceSearch.run(
                Command.CrossSpaceSearch(
                    query = "",
                    filters = listOf(
                        DVFilter(
                            relation = Relations.ID,
                            condition = DVFilterCondition.IN,
                            value = unresolved.toList()
                        )
                    ),
                    offset = 0,
                    limit = unresolved.size,
                    keys = CONTAINER_KEYS
                )
            )
        }.onSuccess { result ->
            val found = mutableSetOf<Id>()
            result.records.forEach { record ->
                if (record.layout == ObjectType.Layout.CHAT_DERIVED || record.layout == ObjectType.Layout.CHAT) {
                    resolvedContainers[record.id] = record
                    found.add(record.id)
                }
            }
            unresolvableContainerIds.addAll(unresolved - found)
        }
    }

    private fun loadChannelBrowse(request: Request): SearchUiState {
        val query = request.query.trim()
        val views = spaceViews.get()
            .filter { view -> !view.targetSpaceId.isNullOrEmpty() }
            // Duplicate keys crash LazyColumn — never trust the store.
            .distinctBy { view -> view.targetSpaceId }
            .filter { view ->
                query.isEmpty() || view.name.orEmpty().contains(query, ignoreCase = true)
            }
        val rows = buildList {
            add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_chip_channels))
            views.forEach { view ->
                add(
                    SearchResultView.ChannelRow(
                        space = view.targetSpaceId.orEmpty(),
                        name = view.name.orEmpty(),
                        icon = view.spaceIcon(urlBuilder)
                    )
                )
            }
            // The bucket's one action — shown with a query too: a name that
            // matches no Channel is exactly when "create it" is the answer,
            // and it replaces the empty state (desktop #2356 / iOS §8.7).
            if (!isPicker) {
                add(SearchResultView.CreateChannelRow)
            }
        }
        return SearchUiState(
            results = rows,
            // The create row stands in for the empty state in this bucket.
            isEmpty = views.isEmpty() && query.isNotEmpty() && isPicker,
            loadedScope = SearchScope.Global,
            hasMore = false
        )
    }

    /**
     * Focused type: every space's instance of the uniqueKey, vault order,
     * from the in-memory store. Typing filters by type OR Channel name —
     * "which space's <Type>" is the question being answered. The top
     * suggestion is the way back out wide (desktop JS-9865 §3).
     */
    private fun loadTypeFocus(
        request: Request,
        focus: SearchToken.TypeFocus
    ): SearchUiState {
        val query = request.query.trim()
        val spaceOrder = spaceViews.get()
            .mapIndexedNotNull { index, view -> view.targetSpaceId?.let { it to index } }
            .toMap()
        val spaceNames = spaceNameIndex()
        val instances = crossSpaceTypes.get()
            .filter { it.uniqueKeyOrNull == focus.uniqueKey }
            .filter { it.getValue<Boolean>(Relations.IS_HIDDEN) != true }
            .sortedBy { spaceOrder[it.getValue<Id>(Relations.SPACE_ID)] ?: Int.MAX_VALUE }
        val focusName = instances.firstOrNull()?.let { fieldParser.getObjectName(it) }.orEmpty()
        val filtered = instances.filter { instance ->
            if (query.isEmpty()) return@filter true
            val spaceName = spaceNames[instance.getValue<Id>(Relations.SPACE_ID)].orEmpty()
            fieldParser.getObjectName(instance).contains(query, ignoreCase = true) ||
                spaceName.contains(query, ignoreCase = true)
        }
        val rows = buildList {
            add(
                SearchResultView.SuggestionRow(
                    id = "type-everywhere:${focus.uniqueKey}",
                    labelRes = R.string.search_v2_focus_search_type_everywhere,
                    formatArg = focusName,
                    token = SearchToken.TypeFilter(uniqueKey = focus.uniqueKey, typeId = focus.typeId)
                )
            )
            if (filtered.isNotEmpty()) {
                add(SearchResultView.SectionHeader(title = focusName))
            }
            filtered.forEach { instance ->
                val spaceId = instance.getValue<Id>(Relations.SPACE_ID).orEmpty()
                add(
                    SearchResultView.ObjectRow(
                        id = instance.id,
                        space = spaceId,
                        icon = instance.objectIcon(),
                        name = fieldParser.getObjectName(instance),
                        typeName = null,
                        spaceName = spaceNames[spaceId],
                        layout = ObjectType.Layout.OBJECT_TYPE,
                        navigation = OpenObjectNavigation.OpenType(target = instance.id, space = spaceId),
                        drill = SearchToken.TypeFilter(uniqueKey = focus.uniqueKey, typeId = instance.id)
                    )
                )
            }
        }
        return SearchUiState(
            results = rows,
            // Never empty: the suggestion row — the way back out wide — must
            // survive a query that matches no instance.
            isEmpty = false,
            loadedScope = SearchScope.Global,
            hasMore = false
        )
    }

    /**
     * Focused person: their membership in every shared space, 1:1 Channel
     * hoisted first. Picking a row lands creator + that Channel's scope in
     * ONE mutation; the suggestion covers all Channels (desktop JS-9865 §4).
     */
    private fun loadPersonFocus(
        request: Request,
        focus: SearchToken.PersonFocus
    ): SearchUiState {
        val query = request.query.trim()
        val spaceOrder = spaceViews.get()
            .mapIndexedNotNull { index, view -> view.targetSpaceId?.let { it to index } }
            .toMap()
        val spaceNames = spaceNameIndex()
        val oneToOneSpace = spaceViews.get()
            .firstOrNull { it.oneToOneIdentity == focus.identity }
            ?.targetSpaceId
        val memberships = participants.get()
            .filter { it.identity == focus.identity && it.status == ParticipantStatus.ACTIVE }
            .distinctBy { it.spaceId }
            .sortedWith(
                compareByDescending<ObjectWrapper.SpaceMember> { it.spaceId == oneToOneSpace }
                    .thenBy { spaceOrder[it.spaceId] ?: Int.MAX_VALUE }
            )
        val personName = memberships.firstOrNull()?.name.orEmpty()
        val filtered = memberships.filter { member ->
            // The person is constant — typing narrows by Channel.
            query.isEmpty() ||
                spaceNames[member.spaceId].orEmpty().contains(query, ignoreCase = true)
        }
        val rows = buildList {
            add(
                SearchResultView.SuggestionRow(
                    id = "by-everywhere:${focus.identity}",
                    labelRes = R.string.search_v2_focus_search_by_everywhere,
                    formatArg = personName,
                    token = SearchToken.Creator(focus.identity)
                )
            )
            if (filtered.isNotEmpty()) {
                add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_focus_person_header))
            }
            filtered.forEach { member ->
                val spaceId = member.spaceId.orEmpty()
                add(
                    SearchResultView.FocusPersonSpaceRow(
                        identity = focus.identity,
                        space = spaceId,
                        spaceName = spaceNames[spaceId].orEmpty(),
                        icon = member.memberIcon(),
                        personName = member.name.orEmpty(),
                        isOneToOne = spaceId == oneToOneSpace
                    )
                )
            }
        }
        return SearchUiState(
            results = rows,
            // Never empty: the suggestion row — the way back out wide — must
            // survive a query that matches no membership (or a cold store).
            isEmpty = false,
            loadedScope = SearchScope.Global,
            hasMore = false
        )
    }

    /** Chat presence for the Messages gate — from the vault-wide chat store. */
    private fun spaceHasChats(space: Id?): Boolean {
        val chats = chatsDetails.get()
        return if (space == null) chats.isNotEmpty() else chats.any { it.spaceId == space }
    }

    // endregion

    // region lead rows

    /** Appends Channels / People / Types lead groups; true if any was added. */
    private fun MutableList<SearchResultView>.addLeadRows(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        // A short query (up to three letters) matches half the vault — keep
        // the injected groups to a taste; from four letters show the full hand.
        val cap = if (trimmed.length >= LEAD_FULL_QUERY_LENGTH) LEAD_CAP_FULL else LEAD_CAP_SHORT

        var added = false

        val channels = spaceViews.get()
            .filter { view -> !view.targetSpaceId.isNullOrEmpty() }
            .distinctBy { view -> view.targetSpaceId }
            .filter { view -> view.name.orEmpty().contains(trimmed, ignoreCase = true) }
            .take(CHANNEL_MATCH_LIMIT)
        if (channels.isNotEmpty()) {
            added = true
            add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_chip_channels))
            channels.forEach { view ->
                add(
                    SearchResultView.ChannelRow(
                        space = view.targetSpaceId.orEmpty(),
                        name = view.name.orEmpty(),
                        icon = view.spaceIcon(urlBuilder)
                    )
                )
            }
        }

        val accountId = configStorage.getAccountId()
        // One pass over the space views for the 1:1 lookup — not one per person.
        val oneToOneByIdentity = spaceViews.get()
            .mapNotNull { view -> view.oneToOneIdentity?.let { it to view.targetSpaceId } }
            .toMap()
        val people = participants.get()
            .filter { it.status == ParticipantStatus.ACTIVE }
            .filter { it.identity != accountId }
            // Local OR global name — a person is findable by either (iOS §8.4).
            .filter { member ->
                member.name.orEmpty().contains(trimmed, ignoreCase = true) ||
                    member.globalName.orEmpty().contains(trimmed, ignoreCase = true)
            }
            .groupBy { it.identity }
            .values
            .take(cap)
        if (people.isNotEmpty()) {
            added = true
            add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_chip_people))
            people.forEach { memberships ->
                val member = memberships.first()
                val ownOneToOne = oneToOneByIdentity[member.identity]
                add(
                    SearchResultView.PersonRow(
                        identity = member.identity,
                        name = member.name.orEmpty(),
                        icon = member.memberIcon(),
                        sharedChannelCount = memberships
                            .mapNotNull { it.spaceId }
                            .distinct()
                            .count { it != ownOneToOne },
                        participantId = member.id,
                        participantSpace = member.spaceId.orEmpty(),
                        globalName = member.globalName
                    )
                )
            }
        }

        val types = typeAggRows(query = trimmed, cap = cap)
        if (types.isNotEmpty()) {
            added = true
            add(SearchResultView.SectionHeader(titleRes = R.string.search_v2_chip_types))
            addAll(types)
        }
        return added
    }

    /** A type instance decorated once — key, name, space and recency read a single time. */
    private data class TypeCandidate(
        val type: ObjectWrapper.Type,
        val uniqueKey: String,
        val name: String,
        val spaceId: Id,
        val lastUsed: Double = 0.0
    )

    private fun typeAggRows(query: String, cap: Int): List<SearchResultView.TypeAggRow> {
        val currentSpace = vmParams.entrySpace?.id
        val spaceNames = spaceNameIndex()
        // One decoration pass: every relation and name is read exactly once,
        // and the sort below compares cached fields, not re-derived ones.
        return crossSpaceTypes.get()
            .asSequence()
            .mapNotNull { type ->
                if (type.getValue<Boolean>(Relations.IS_HIDDEN) == true) return@mapNotNull null
                val uniqueKey = type.uniqueKeyOrNull ?: return@mapNotNull null
                if (uniqueKey in EXCLUDED_TYPE_KEYS) return@mapNotNull null
                val name = fieldParser.getObjectName(type)
                if (query.isNotEmpty() && !name.contains(query, ignoreCase = true)) return@mapNotNull null
                TypeCandidate(
                    type = type,
                    uniqueKey = uniqueKey,
                    name = name,
                    spaceId = type.getValue<Id>(Relations.SPACE_ID).orEmpty()
                )
            }
            .groupBy { it.uniqueKey }
            .map { (uniqueKey, instances) ->
                val representative = instances.firstOrNull { it.spaceId == currentSpace }
                    ?: instances.minBy { it.spaceId }
                SearchResultView.TypeAggRow(
                    uniqueKey = uniqueKey,
                    name = representative.name,
                    icon = representative.type.objectIcon(),
                    spaceCount = instances.asSequence().map { it.spaceId }.distinct().count(),
                    typeId = representative.type.id,
                    typeSpace = representative.spaceId,
                    spaceName = spaceNames[representative.spaceId]
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            .take(cap)
    }

    // endregion

    // region browse grouping

    /**
     * The empty-query browse is date-grouped (Today / Yesterday / Previous 7
     * and 14 days / month) or first-letter-grouped (Name order); the first
     * header carries the sort menu. The Chats bucket stays flat on
     * last-message order — a title or a foreign sort would interleave its
     * buckets (iOS §8.5).
     */
    private fun groupBrowseRows(
        rows: List<SearchResultView.ObjectRow>,
        records: List<ObjectWrapper.Basic>,
        request: Request
    ): List<SearchResultView> {
        if (request.bucket() == KindBucket.CHAT) return rows
        return if (request.sort == BrowseSort.NAME) {
            groupByLetter(rows, request)
        } else {
            groupByDay(rows, records, request)
        }
    }

    private fun groupByLetter(
        rows: List<SearchResultView.ObjectRow>,
        request: Request
    ): List<SearchResultView> {
        // The server sorted the raw name relation, but rows display the
        // parsed name (a Note shows its snippet, an empty name "Untitled") —
        // re-sort by what the user actually sees so headers stay ordered.
        val sorted = rows.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        val grouped = LinkedHashMap<String, MutableList<SearchResultView.ObjectRow>>()
        sorted.forEach { row ->
            val first = row.name.trim().firstOrNull()?.uppercaseChar()
            val key = if (first != null && first.isLetter()) first.toString() else "#"
            grouped.getOrPut(key) { mutableListOf() }.add(row)
        }
        return buildList {
            var first = true
            grouped.forEach { (letter, groupRows) ->
                add(
                    SearchResultView.SectionHeader(
                        title = letter,
                        sortMenu = if (first) request.sort else null
                    )
                )
                first = false
                addAll(groupRows)
            }
        }
    }

    private class DayGroup(val titleRes: Int? = null, val title: String? = null) {
        val rows = mutableListOf<SearchResultView.ObjectRow>()
    }

    private fun groupByDay(
        rows: List<SearchResultView.ObjectRow>,
        records: List<ObjectWrapper.Basic>,
        request: Request
    ): List<SearchResultView> {
        val groupKey = SearchFilters.browseGroupKey(request.bucket(), request.sort)
        val zone = TimeZone.getDefault()
        val nowMs = System.currentTimeMillis()
        val todayDay = Math.floorDiv(nowMs + zone.getOffset(nowMs), DAY_MS)
        val currentYearTitle = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(nowMs))
        // Two cached formatters per grouping pass — never one per row.
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val grouped = LinkedHashMap<String, DayGroup>()
        rows.forEachIndexed { index, row ->
            val seconds = records.getOrNull(index)?.getValue<Double>(groupKey)?.toLong() ?: 0L
            val group = if (seconds <= 0L) {
                grouped.getOrPut("unknown") { DayGroup(title = "—") }
            } else {
                val ms = seconds * 1000L
                val diff = todayDay - Math.floorDiv(ms + zone.getOffset(ms), DAY_MS)
                when {
                    diff <= 0L -> grouped.getOrPut("d0") { DayGroup(titleRes = R.string.search_v2_today) }
                    diff == 1L -> grouped.getOrPut("d1") { DayGroup(titleRes = R.string.search_v2_yesterday) }
                    diff <= 6L -> grouped.getOrPut("w7") { DayGroup(titleRes = R.string.allContent_group_prev_7) }
                    diff <= 13L -> grouped.getOrPut("w14") { DayGroup(titleRes = R.string.allContent_group_prev_14) }
                    else -> {
                        val date = Date(ms)
                        val sameYear = yearFormat.format(date) == currentYearTitle
                        val title =
                            if (sameYear) monthFormat.format(date) else monthYearFormat.format(date)
                        grouped.getOrPut("m:$title") { DayGroup(title = title) }
                    }
                }
            }
            group.rows.add(row)
        }
        return buildList {
            var first = true
            grouped.forEach { (_, group) ->
                add(
                    SearchResultView.SectionHeader(
                        titleRes = group.titleRes,
                        title = group.title,
                        sortMenu = if (first) request.sort else null
                    )
                )
                first = false
                addAll(group.rows)
            }
        }
    }

    // endregion

    // region result mapping

    private fun Command.SearchWithMeta.Result.toObjectRow(
        typeById: Map<Id, ObjectWrapper.Type>
    ): SearchResultView.ObjectRow {
        val obj = wrapper
        val type = obj.type.firstOrNull()?.let { typeById[it] }
        val nameMeta = metas.firstOrNull { meta ->
            val source = meta.source
            source is Command.SearchWithMeta.Result.Meta.Source.Relation && source.key == Relations.NAME
        }
        val blockMeta = metas.firstOrNull { meta ->
            meta.source is Command.SearchWithMeta.Result.Meta.Source.Block
        }
        // Highlight ranges index into the meta's own highlight text, not into
        // whatever display name we would otherwise derive (Notes especially).
        val highlightedName = nameMeta?.highlight?.takeIf { it.isNotEmpty() }
        return SearchResultView.ObjectRow(
            id = obj.id,
            space = obj.spaceId.orEmpty(),
            icon = obj.objectIcon(urlBuilder, type),
            name = highlightedName ?: fieldParser.getObjectName(obj),
            nameHighlights = if (highlightedName != null) nameMeta.ranges else emptyList(),
            snippet = blockMeta?.highlight ?: obj.getValue<String>(Relations.SNIPPET),
            snippetHighlights = blockMeta?.ranges.orEmpty(),
            typeName = type?.let { fieldParser.getObjectName(it) },
            spaceName = null,
            layout = obj.layout,
            navigation = obj.navigation(),
            drill = obj.drillToken()
        )
    }

    private fun ObjectWrapper.Basic.toCrossSpaceObjectRow(
        typeById: Map<Id, ObjectWrapper.Type>,
        spaceNames: Map<Id, String?>
    ): SearchResultView.ObjectRow {
        val obj = this
        val spaceId = obj.spaceId.orEmpty()
        val type = obj.type.firstOrNull()?.let { typeById[it] }
        return SearchResultView.ObjectRow(
            id = obj.id,
            space = spaceId,
            icon = obj.objectIcon(urlBuilder, type),
            name = fieldParser.getObjectName(obj),
            // Cross-space records carry no fulltext meta — names render plainly.
            snippet = obj.getValue<String>(Relations.SNIPPET),
            typeName = type?.let { fieldParser.getObjectName(it) },
            spaceName = spaceNames[spaceId],
            layout = obj.layout,
            navigation = obj.navigation(),
            drill = obj.drillToken()
        )
    }

    private fun spaceNameIndex(): Map<Id, String?> =
        spaceViews.get().associate { it.targetSpaceId.orEmpty() to it.name }

    /**
     * Plain chat messages carry their text in [Chat.Message.Content];
     * discussion messages are block-based and keep it in text blocks.
     */
    private fun Chat.Message.plainText(): String {
        val plain = content?.text.orEmpty()
        if (plain.isNotBlank()) return plain
        return blocks
            .filterIsInstance<Chat.Message.MessageBlock.Text>()
            .joinToString(separator = " ") { block -> block.text }
            .trim()
    }

    /** Drill resolution: type rows only (the backlink drill is cut, iOS B5). */
    private fun ObjectWrapper.Basic.drillToken(): SearchToken? {
        if (layout == ObjectType.Layout.OBJECT_TYPE) {
            val uniqueKey = getValue<String>(Relations.UNIQUE_KEY)?.takeIf { it.isNotEmpty() }
            if (uniqueKey != null) {
                return SearchToken.TypeFilter(uniqueKey = uniqueKey, typeId = id)
            }
        }
        return null
    }

    /**
     * The nullable read — [ObjectWrapper.Type.uniqueKey] throws on a malformed
     * record, and exception construction costs orders of magnitude more than
     * the lookup it guards.
     */
    private val ObjectWrapper.Type.uniqueKeyOrNull: String?
        get() = getValue<String>(Relations.UNIQUE_KEY)?.takeIf { it.isNotEmpty() }

    private fun participantIdsOf(identity: Id): List<Id> =
        participants.get().filter { it.identity == identity }.map { it.id }

    private fun ObjectWrapper.SpaceMember.memberIcon(): ObjectIcon {
        val image = iconImage
        return if (!image.isNullOrEmpty()) {
            ObjectIcon.Profile.Image(hash = urlBuilder.thumbnail(image), name = name.orEmpty())
        } else {
            ObjectIcon.Profile.Avatar(name = name.orEmpty())
        }
    }

    // endregion

    // region token views & chips

    val tokenViews: StateFlow<List<TokenView>> = combine(
        tokens,
        spaceViews.observe(),
        crossSpaceTypes.observe(),
        participants.observe(),
        chatsDetails.observe()
    ) { tokens, views, types, members, chats ->
        // One index per tick — never a full type-store scan per token.
        val typeByUniqueKey = types.associateBy { it.uniqueKeyOrNull }
        tokens
            // The attach picker's fixed scope never renders as a pill.
            .filterNot { isPicker && it is SearchToken.SpaceScope }
            // The scope pill renders first regardless of when it was added
            // (stable sort keeps the rest in insertion order).
            .sortedBy { if (it is SearchToken.SpaceScope) 0 else 1 }
            .map { token -> token.toView(views, typeByUniqueKey, members, chats) }
    }
        .flowOn(dispatchers.computation)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private fun SearchToken.toView(
        views: List<ObjectWrapper.SpaceView>,
        typeByUniqueKey: Map<String?, ObjectWrapper.Type>,
        members: List<ObjectWrapper.SpaceMember>,
        chats: List<ObjectWrapper.Basic>
    ): TokenView = when (this) {
        is SearchToken.ChatFilter -> {
            // The chat's own name when it has one; unnamed space-level and 1:1
            // chats read as the space itself, so those pills wear the space's
            // name (iOS token-model rule).
            val chat = chats.firstOrNull { it.id == this.chat }
            val view = views.firstOrNull { it.targetSpaceId == space }
            val chatName = chat?.name?.trim().orEmpty()
            TokenView(
                token = this,
                label = chatName.ifEmpty { view?.name ?: PLACEHOLDER_LABEL },
                objectIcon = if (chatName.isNotEmpty()) chat?.objectIcon(urlBuilder) else null,
                spaceIcon = if (chatName.isEmpty()) view?.spaceIcon(urlBuilder) else null,
                iconRes = if (chat == null && view == null) UiR.drawable.ic_chat_32 else null
            )
        }
        is SearchToken.SpaceScope -> {
            val view = views.firstOrNull { it.targetSpaceId == space }
            TokenView(
                token = this,
                // A cold store must not blank the pill — placeholder instead.
                label = view?.name ?: PLACEHOLDER_LABEL,
                spaceIcon = view?.spaceIcon(urlBuilder)
            )
        }
        is SearchToken.Kind -> TokenView(
            token = this,
            labelRes = bucket.labelRes(),
            iconRes = bucket.iconRes()
        )
        is SearchToken.TypeFilter -> {
            val type = typeByUniqueKey[uniqueKey]
            TokenView(
                token = this,
                label = type?.let { fieldParser.getObjectName(it) } ?: PLACEHOLDER_LABEL,
                objectIcon = type?.objectIcon()
            )
        }
        is SearchToken.TypeFocus -> {
            val type = typeByUniqueKey[uniqueKey]
            TokenView(
                token = this,
                label = type?.let { fieldParser.getObjectName(it) } ?: PLACEHOLDER_LABEL,
                objectIcon = type?.objectIcon()
            )
        }
        is SearchToken.PersonFocus -> {
            val member = members.firstOrNull { it.identity == identity }
            TokenView(
                token = this,
                label = member?.name ?: PLACEHOLDER_LABEL,
                objectIcon = member?.memberIcon()
            )
        }
        is SearchToken.Creator -> {
            if (identity == configStorage.getAccountId()) {
                val self = members.firstOrNull { it.identity == identity }
                TokenView(
                    token = this,
                    labelRes = R.string.search_v2_chip_by_me,
                    objectIcon = self?.memberIcon()
                )
            } else {
                val member = members.firstOrNull { it.identity == identity }
                TokenView(
                    token = this,
                    labelRes = R.string.search_v2_chip_by_name,
                    formatArg = member?.name ?: PLACEHOLDER_LABEL,
                    objectIcon = member?.memberIcon()
                )
            }
        }
    }

    /** The store trio the chip row depends on, pre-combined to stay within combine's arity. */
    private data class ChipStores(
        val globalTypes: List<ObjectWrapper.Type>,
        val spaceTypes: List<ObjectWrapper.Type>,
        val chats: List<ObjectWrapper.Basic>
    )

    val chips: StateFlow<List<ChipView>> = combine(
        tokens,
        spaceViews.observe(),
        participants.observe(),
        combine(
            crossSpaceTypes.observe(),
            storeOfObjectTypes.observe(),
            // The Messages gate reads chat presence — it must tick when the
            // first chat lands, not wait for an unrelated store to move.
            chatsDetails.observe()
        ) { globalTypes, spaceTypes, chats -> ChipStores(globalTypes, spaceTypes, chats) }
    ) { tokens, views, members, stores ->
        deriveChips(tokens, views, members, stores.globalTypes, stores.spaceTypes, stores.chats)
    }
        .flowOn(dispatchers.computation)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    /**
     * Two mutually exclusive chip modes (iOS §4):
     *
     * A. Cross-space refinement package — global mode with a kind (≠Channels)
     *    or type token: once the user has said WHAT they want, the useful next
     *    question is WHERE and WHO. `[Channels picker][People picker]
     *    [≤5 By-person][≤5 channel]`, current channel hoisted before the cap.
     *
     * B. Default row — only addable tokens; a filled group's chips disappear.
     */
    private fun deriveChips(
        tokens: List<SearchToken>,
        views: List<ObjectWrapper.SpaceView>,
        members: List<ObjectWrapper.SpaceMember>,
        globalTypes: List<ObjectWrapper.Type>,
        spaceTypes: List<ObjectWrapper.Type>,
        chats: List<ObjectWrapper.Basic>
    ): List<ChipView> {
        val scope = tokens.filterIsInstance<SearchToken.SpaceScope>().firstOrNull()
        val what = tokens.firstOrNull { it.group == TokenGroup.WHAT }
        val creator = tokens.filterIsInstance<SearchToken.Creator>().firstOrNull()
        val isGlobal = scope == null
        val accountId = configStorage.getAccountId()
        val entrySpace = vmParams.entrySpace?.id

        val channelViews = views
            .filter { !it.targetSpaceId.isNullOrEmpty() }
            // Duplicate keys crash LazyColumn — never trust the store.
            .distinctBy { it.targetSpaceId }
        val activeMembers = members.filter { it.status == ParticipantStatus.ACTIVE }
        val scopedMembers = if (isGlobal) {
            activeMembers.distinctBy { it.identity }
        } else {
            activeMembers.filter { it.spaceId == scope?.space }
        }
        // "Is there anyone besides me" — answered without materializing a
        // full distinct copy of the member list.
        val hasMembers = scopedMembers.asSequence().map { it.identity }.distinct().take(2).count() > 1

        val refinement = isGlobal && (
            (what is SearchToken.Kind && what.bucket != KindBucket.CHANNEL) ||
                what is SearchToken.TypeFilter
            )
        if (refinement) {
            return buildList {
                if (channelViews.isNotEmpty()) {
                    add(pickerChip(PickerType.CHANNELS, R.string.search_v2_chip_channels))
                }
                if (hasMembers) {
                    add(pickerChip(PickerType.PEOPLE, R.string.search_v2_chip_people))
                }
                if (creator == null && hasMembers && accountId != null) {
                    add(
                        ChipView(
                            id = "by-me",
                            token = SearchToken.Creator(accountId),
                            labelRes = R.string.search_v2_chip_by_me,
                            objectIcon = activeMembers.firstOrNull { it.identity == accountId }?.memberIcon()
                        )
                    )
                    scopedMembers
                        .filter { it.identity != accountId }
                        .take(REFINEMENT_PERSON_LIMIT - 1)
                        .forEach { member -> add(memberChip(member)) }
                }
                // Current channel moved to the front of the channel group
                // BEFORE the cap, so it can never be silently evicted.
                val ordered = channelViews.sortedByDescending { it.targetSpaceId == entrySpace }
                ordered.take(REFINEMENT_CHANNEL_LIMIT).forEach { view ->
                    val space = view.targetSpaceId.orEmpty()
                    add(
                        ChipView(
                            id = "scope:$space",
                            token = SearchToken.SpaceScope(space),
                            labelRes = if (space == entrySpace) R.string.search_v2_chip_in_space else null,
                            formatArg = if (space == entrySpace) view.name.orEmpty() else null,
                            label = if (space == entrySpace) null else view.name.orEmpty(),
                            spaceIcon = view.spaceIcon(urlBuilder)
                        )
                    )
                }
            }
        }

        val whatIsScopable = what !is SearchToken.Kind || what.bucket != KindBucket.CHANNEL
        // Channels are not authored; everything else is (iOS §4.2's
        // whoFilterApplies — Types and Chats keep their person chips).
        val whatIsAuthored = when (what) {
            null -> true
            is SearchToken.Kind -> what.bucket != KindBucket.CHANNEL
            // A creator filter has no effect on a focused listing — offering
            // the chips there would add a dead token.
            is SearchToken.TypeFocus, is SearchToken.PersonFocus -> false
            else -> true
        }
        // From the chat-container store, not SpaceView.chatId — a space whose
        // messages live in chat objects may carry no space-level chat id.
        val scopeHasChats =
            if (scope == null) chats.isNotEmpty() else chats.any { it.spaceId == scope.space }
        val personGates = creator == null && hasMembers && whatIsAuthored

        return buildList {
            if (isGlobal && entrySpace != null && whatIsScopable) {
                val view = views.firstOrNull { it.targetSpaceId == entrySpace }
                if (view != null) {
                    add(
                        ChipView(
                            id = "scope:$entrySpace",
                            token = SearchToken.SpaceScope(entrySpace),
                            labelRes = R.string.search_v2_chip_in_space,
                            formatArg = view.name.orEmpty(),
                            spaceIcon = view.spaceIcon(urlBuilder)
                        )
                    )
                }
            }
            if (isGlobal && what == null) {
                add(kindChip(KindBucket.CHANNEL, R.string.search_v2_chip_channels))
            }
            // The attach picker searches objects only — no message mode.
            if (what == null && scopeHasChats && !isPicker) {
                add(kindChip(KindBucket.MESSAGE, R.string.search_v2_chip_messages))
            }
            if (personGates && accountId != null) {
                add(
                    ChipView(
                        id = "by-me",
                        token = SearchToken.Creator(accountId),
                        labelRes = R.string.search_v2_chip_by_me,
                        objectIcon = activeMembers.firstOrNull { it.identity == accountId }?.memberIcon()
                    )
                )
                add(pickerChip(PickerType.PEOPLE, R.string.search_v2_chip_people))
            }
            if (what == null) {
                add(kindChip(KindBucket.MEDIA, R.string.search_v2_chip_media))
                add(pickerChip(PickerType.TYPES, R.string.search_v2_chip_types))
                if (isGlobal) {
                    add(kindChip(KindBucket.PAGE, R.string.search_v2_chip_pages))
                    add(kindChip(KindBucket.BOOKMARK, R.string.search_v2_chip_bookmarks))
                    add(kindChip(KindBucket.COLLECTION, R.string.search_v2_chip_collections))
                    add(kindChip(KindBucket.QUERY, R.string.search_v2_chip_queries))
                    add(kindChip(KindBucket.CHAT, R.string.search_v2_chip_chats))
                } else {
                    // In a concrete space the actual type list replaces the
                    // global buckets — name order, capped (iOS §4.4).
                    scopedTypeList(scope?.space, entrySpace, globalTypes, spaceTypes)
                        .take(TYPE_CHIPS_LIMIT)
                        .forEach { type ->
                            add(
                                ChipView(
                                    id = "type:${type.uniqueKey}",
                                    token = SearchToken.TypeFilter(
                                        uniqueKey = type.uniqueKey,
                                        typeId = type.id
                                    ),
                                    label = fieldParser.getObjectName(type),
                                    objectIcon = type.objectIcon()
                                )
                            )
                        }
                }
            }
            if (personGates) {
                scopedMembers
                    .filter { it.identity != accountId }
                    .take(PERSON_CHIP_LIMIT)
                    .forEach { member -> add(memberChip(member)) }
            }
        }
    }

    private fun scopedTypeList(
        scopeSpace: Id?,
        entrySpace: Id?,
        globalTypes: List<ObjectWrapper.Type>,
        spaceTypes: List<ObjectWrapper.Type>
    ): List<ObjectWrapper.Type> {
        val source = if (scopeSpace == entrySpace) {
            spaceTypes
        } else {
            globalTypes.filter { it.getValue<Id>(Relations.SPACE_ID) == scopeSpace }
        }
        // One pass, one name read per type; the sort compares cached names.
        return source
            .mapNotNull { type ->
                if (type.getValue<Boolean>(Relations.IS_HIDDEN) == true) return@mapNotNull null
                val key = type.uniqueKeyOrNull
                if (key == null || key in EXCLUDED_TYPE_KEYS) return@mapNotNull null
                if (type.recommendedLayout in SCOPED_TYPE_EXCLUDED_LAYOUTS) return@mapNotNull null
                type to fieldParser.getObjectName(type)
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.second })
            .map { it.first }
    }

    private fun memberChip(member: ObjectWrapper.SpaceMember) = ChipView(
        id = "by:${member.identity}",
        token = SearchToken.Creator(member.identity),
        labelRes = R.string.search_v2_chip_by_name,
        formatArg = member.name.orEmpty(),
        objectIcon = member.memberIcon()
    )

    private fun kindChip(bucket: KindBucket, labelRes: Int) = ChipView(
        id = "kind:${bucket.name}",
        token = SearchToken.Kind(bucket),
        labelRes = labelRes,
        iconRes = bucket.iconRes()
    )

    private fun pickerChip(type: PickerType, labelRes: Int) = ChipView(
        id = "picker:${type.name}",
        picker = type,
        labelRes = labelRes,
        iconRes = when (type) {
            PickerType.CHANNELS -> UiR.drawable.ic_space_type_24
            PickerType.PEOPLE -> UiR.drawable.ic_members_24
            PickerType.TYPES -> UiR.drawable.ic_object_types_24
        }
    )

    private fun KindBucket.iconRes(): Int? = when (this) {
        KindBucket.MESSAGE -> UiR.drawable.ic_chat_32
        KindBucket.TYPE -> UiR.drawable.ic_object_types_24
        KindBucket.CHANNEL -> UiR.drawable.ic_space_type_24
        else -> null
    }

    private fun KindBucket.labelRes(): Int = when (this) {
        KindBucket.CHANNEL -> R.string.search_v2_chip_channels
        KindBucket.MESSAGE -> R.string.search_v2_chip_messages
        KindBucket.MEDIA -> R.string.search_v2_chip_media
        KindBucket.PAGE -> R.string.search_v2_chip_pages
        KindBucket.BOOKMARK -> R.string.search_v2_chip_bookmarks
        KindBucket.COLLECTION -> R.string.search_v2_chip_collections
        KindBucket.QUERY -> R.string.search_v2_chip_queries
        KindBucket.CHAT -> R.string.search_v2_chip_chats
        KindBucket.TYPE -> R.string.search_v2_chip_types
    }

    // endregion

    // region pickers

    val pickerRows: StateFlow<List<PickerRowView>> = combine(
        activePicker,
        tokens,
        spaceViews.observe(),
        participants.observe(),
        crossSpaceTypes.observe()
    ) { picker, tokens, views, members, types ->
        when (picker) {
            null -> emptyList()
            PickerType.CHANNELS -> views
                .filter { !it.targetSpaceId.isNullOrEmpty() }
                // Duplicate keys crash LazyColumn — never trust the store.
                .distinctBy { it.targetSpaceId }
                .map { view ->
                    PickerRowView(
                        id = "space:${view.targetSpaceId}",
                        token = SearchToken.SpaceScope(view.targetSpaceId.orEmpty()),
                        label = view.name.orEmpty(),
                        spaceIcon = view.spaceIcon(urlBuilder)
                    )
                }
            PickerType.PEOPLE -> {
                val accountId = configStorage.getAccountId()
                val scope = tokens.filterIsInstance<SearchToken.SpaceScope>().firstOrNull()
                // 1:1-chat partners first, then alphabetical (iOS §6).
                val oneToOneIdentities = views
                    .mapNotNull { it.oneToOneIdentity }
                    .toSet()
                val memberships = members
                    .filter { it.status == ParticipantStatus.ACTIVE }
                    .filter { it.identity != accountId }
                    .filter { scope == null || it.spaceId == scope.space }
                val spaceCountByIdentity = members
                    .filter { it.status == ParticipantStatus.ACTIVE }
                    .groupBy { it.identity }
                    .mapValues { (_, list) -> list.mapNotNull { it.spaceId }.distinct().size }
                memberships
                    .distinctBy { it.identity }
                    .sortedWith(
                        compareByDescending<ObjectWrapper.SpaceMember> { it.identity in oneToOneIdentities }
                            .thenBy { it.name.orEmpty().lowercase() }
                    )
                    .map { member ->
                        val count = spaceCountByIdentity[member.identity] ?: 1
                        PickerRowView(
                            id = "creator:${member.identity}",
                            token = SearchToken.Creator(member.identity),
                            label = member.name.orEmpty(),
                            captionRes = if (count == 1) {
                                R.string.search_v2_person_member_in_one
                            } else {
                                R.string.search_v2_person_member_in
                            },
                            captionCountArg = if (count == 1) null else count,
                            objectIcon = member.memberIcon()
                        )
                    }
            }
            PickerType.TYPES -> {
                val scope = tokens.filterIsInstance<SearchToken.SpaceScope>().firstOrNull()
                val spaceNames = spaceNameIndex()
                // Decorate once: key, name, space and recency are read a
                // single time per type; every sort below compares cached
                // fields instead of re-deriving them per comparison.
                val candidates = types.mapNotNull { type ->
                    if (type.getValue<Boolean>(Relations.IS_HIDDEN) == true) return@mapNotNull null
                    val key = type.uniqueKeyOrNull ?: return@mapNotNull null
                    if (key in EXCLUDED_TYPE_KEYS) return@mapNotNull null
                    val spaceId = type.getValue<Id>(Relations.SPACE_ID).orEmpty()
                    if (scope != null && spaceId != scope.space) return@mapNotNull null
                    TypeCandidate(
                        type = type,
                        uniqueKey = key,
                        name = fieldParser.getObjectName(type),
                        spaceId = spaceId,
                        lastUsed = type.getValue<Double>(Relations.LAST_USED_DATE) ?: 0.0
                    )
                }
                // One counting pass over the decorated list — never a second
                // full-store scan.
                val countByKey = if (scope == null) {
                    candidates
                        .groupBy { it.uniqueKey }
                        .mapValues { (_, instances) ->
                            instances.asSequence().map { it.spaceId }.distinct().count()
                        }
                } else emptyMap()
                val deduped = if (scope == null) {
                    candidates.groupBy { it.uniqueKey }.map { (_, instances) ->
                        instances.maxBy { it.lastUsed }
                    }
                } else candidates
                deduped
                    // Recency-first: the picker is where "the type I always
                    // use" should be at the top (iOS §4.4).
                    .sortedWith(
                        compareByDescending<TypeCandidate> { it.lastUsed }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                    )
                    .map { candidate ->
                        val count = countByKey[candidate.uniqueKey] ?: 1
                        val typeSpaceName = spaceNames[candidate.spaceId]
                        PickerRowView(
                            id = "type:${candidate.uniqueKey}",
                            token = SearchToken.TypeFilter(
                                uniqueKey = candidate.uniqueKey,
                                typeId = candidate.type.id
                            ),
                            label = candidate.name,
                            captionRes = when {
                                scope != null -> null
                                count > 1 -> R.string.search_v2_type_agg_caption
                                typeSpaceName != null -> R.string.search_v2_in_space_caption
                                else -> null
                            },
                            captionCountArg = if (scope == null && count > 1) count else null,
                            captionStringArg = if (scope == null && count <= 1) typeSpaceName else null,
                            objectIcon = candidate.type.objectIcon()
                        )
                    }
            }
        }
    }
        .flowOn(dispatchers.computation)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun onPickerChipClicked(type: PickerType) {
        activePicker.value = type
    }

    fun onPickerDismissed() {
        activePicker.value = null
    }

    /** Picker picks are chip semantics — the query is kept. */
    fun onPickerRowClicked(row: PickerRowView) {
        activePicker.value = null
        applyToken(row.token, clearQuery = false)
    }

    // endregion

    // region user actions

    fun onQueryChanged(query: String) {
        rawInput.value = query
        // Typing clears the pill selection.
        selectedTokenId.value = null
    }

    /** Chip adds keep the typed query — chips narrow the same search. */
    fun onChipClicked(chip: ChipView) {
        val picker = chip.picker
        if (picker != null) {
            onPickerChipClicked(picker)
            return
        }
        chip.token?.let { applyToken(it, clearQuery = false) }
    }

    /** Tap selects a pill; tap again deselects. Backspace removes a selected pill. */
    fun onTokenClicked(token: SearchToken) {
        selectedTokenId.value = if (selectedTokenId.value == token.id) null else token.id
    }

    fun onTokenRemoved(token: SearchToken) {
        // The picker's scope is locked — it must never return cross-space objects.
        if (token is SearchToken.SpaceScope && isPicker) return
        val before = tokens.value
        var after = if (token is SearchToken.ChatFilter) {
            // Removing the chat filter widens ONE step: chat -> its scope's
            // messages (iOS §3.4's removal chain), not straight to objects.
            before.map { if (it == token) SearchToken.Kind(KindBucket.MESSAGE) else it }
        } else {
            before.filterNot { it == token }
        }
        if (token is SearchToken.SpaceScope) {
            // Removing the scope switches to global in place — same query
            // re-runs vault-wide; global-only buckets are already valid.
            after = after.mapAcrossBoundary(newScope = null)
        }
        tokens.value = after
        selectedTokenId.value = null
    }

    private fun removableTokens(): List<SearchToken> =
        tokens.value.filterNot { isPicker && it is SearchToken.SpaceScope }

    /**
     * Backspace on an empty field: first press selects the last token,
     * second press removes the selected one (iOS §3.4 — the safer two-press
     * reading of desktop's "Backspace at 0 pops the rightmost token").
     */
    fun onBackspaceToken() {
        val selected = selectedTokenId.value
        if (selected != null) {
            tokens.value.firstOrNull { it.id == selected }?.let { onTokenRemoved(it) }
        } else {
            selectedTokenId.value = removableTokens().lastOrNull()?.id
        }
    }

    fun onChannelDrill(row: SearchResultView.ChannelRow) {
        applyToken(SearchToken.SpaceScope(row.space), clearQuery = false)
    }

    fun onPersonDrill(row: SearchResultView.PersonRow) {
        applyToken(SearchToken.Creator(row.identity), clearQuery = true)
    }

    fun onTypeAggDrill(row: SearchResultView.TypeAggRow) {
        applyToken(
            SearchToken.TypeFilter(uniqueKey = row.uniqueKey, typeId = row.typeId),
            clearQuery = true
        )
    }

    fun onObjectDrill(row: SearchResultView.ObjectRow) {
        val drill = row.drill ?: return
        applyToken(drill, clearQuery = true)
    }

    private fun applyToken(token: SearchToken, clearQuery: Boolean) {
        val before = tokens.value
        var after = before
        // Participants are not authored — a creator filter ANDed with a
        // person focus is an empty set, so the focus yields.
        if (token is SearchToken.Creator) {
            after = after.filterNot { it is SearchToken.PersonFocus }
        }
        // Channels are not authored, and a focused type listing ignores the
        // creator — either way the token would linger as a dead filter.
        if ((token is SearchToken.Kind && token.bucket == KindBucket.CHANNEL) ||
            token is SearchToken.TypeFocus
        ) {
            after = after.filterNot { it is SearchToken.Creator }
        }
        after = after.plusToken(token)
        if (token is SearchToken.SpaceScope) {
            after = after.mapAcrossBoundary(newScope = token.space)
        }
        tokens.value = after
        selectedTokenId.value = null
        if (clearQuery) rawInput.value = ""
    }

    fun onLoadMore() {
        if (uiState.value.hasMore && !uiState.value.isLoading) {
            loadMoreRequests.tryEmit(Unit)
        }
    }

    fun onBrowseSortSelected(sort: BrowseSort) {
        browseSort.value = sort
    }

    fun onResultClicked(view: SearchResultView) {
        when (view) {
            is SearchResultView.ChannelRow -> {
                viewModelScope.launch {
                    spaceManager.set(view.space).fold(
                        onSuccess = {
                            saveCurrentSpace.async(SaveCurrentSpace.Params(SpaceId(view.space)))
                            commandsChannel.send(SearchNavigation.OpenSpace(view.space))
                        },
                        onFailure = {
                            Timber.e(it, "Could not open space from search")
                            commandsChannel.send(SearchNavigation.Toast(R.string.search_v2_could_not_open_object))
                        }
                    )
                }
            }
            is SearchResultView.SuggestionRow -> {
                applyToken(view.token, clearQuery = true)
            }
            is SearchResultView.FocusPersonSpaceRow -> {
                // One mutation: creator + that Channel's scope — one RPC,
                // and the focus yields before the boundary mapping runs.
                val after = tokens.value
                    .filterNot { it is SearchToken.PersonFocus }
                    .plusToken(SearchToken.Creator(view.identity))
                    .plusToken(SearchToken.SpaceScope(view.space))
                    .mapAcrossBoundary(newScope = view.space)
                tokens.value = after
                selectedTokenId.value = null
                rawInput.value = ""
            }
            SearchResultView.CreateChannelRow -> {
                viewModelScope.launch {
                    commandsChannel.send(SearchNavigation.OpenCreateChannel)
                }
            }
            SearchResultView.MessagesTutorialRow -> {
                // The button applies the Messages filter and keeps the query.
                applyToken(SearchToken.Kind(KindBucket.MESSAGE), clearQuery = false)
            }
            is SearchResultView.ObjectRow -> {
                if (isPicker) {
                    // A picker returns the object; it never navigates.
                    viewModelScope.launch {
                        commandsChannel.send(SearchNavigation.ObjectSelected(id = view.id, space = view.space))
                    }
                } else {
                    openObject(view.navigation, view.space, view.id)
                }
            }
            is SearchResultView.PersonRow -> {
                // Click focuses the person (the participant view stays one
                // level deeper); the drill icon keeps the creator filter.
                applyToken(SearchToken.PersonFocus(view.identity), clearQuery = true)
            }
            is SearchResultView.MessageRow -> {
                viewModelScope.launch {
                    val switchSpace = view.space != vmParams.entrySpace?.id
                    if (switchSpace) {
                        val switched = spaceManager.set(view.space)
                        if (switched.isFailure) {
                            commandsChannel.send(SearchNavigation.Toast(R.string.search_v2_could_not_open_object))
                            return@launch
                        }
                        saveCurrentSpace.async(SaveCurrentSpace.Params(SpaceId(view.space)))
                    }
                    val discussionParent = view.discussionParentId
                    if (discussionParent != null) {
                        commandsChannel.send(
                            SearchNavigation.OpenDiscussionAtMessage(
                                space = view.space,
                                parentObject = discussionParent,
                                discussion = view.chatId,
                                message = view.messageId
                            )
                        )
                    } else {
                        commandsChannel.send(
                            SearchNavigation.OpenChatAtMessage(
                                space = view.space,
                                chat = view.chatId,
                                message = view.messageId,
                                switchSpace = switchSpace
                            )
                        )
                    }
                }
            }
            is SearchResultView.TypeAggRow -> {
                // Click focuses the group — "which Channel's <Type>?"; the
                // drill icon keeps the cross-space type filter.
                applyToken(
                    SearchToken.TypeFocus(uniqueKey = view.uniqueKey, typeId = view.typeId),
                    clearQuery = true
                )
            }
            is SearchResultView.SectionHeader -> Unit
        }
    }

    private fun openObject(navigation: OpenObjectNavigation, space: Id, obj: Id) {
        viewModelScope.launch {
            if (space == vmParams.entrySpace?.id) {
                commandsChannel.send(SearchNavigation.OpenObject(navigation))
            } else {
                // Cross-space: reuse the standard deep-link path (permission
                // check + space switch), no bespoke routing. The fragment
                // pushes the destination directly — no space-home back-stack
                // entry, so back returns to the search/vault.
                val result = deepLinkToObject.onDeepLinkToObject(
                    obj = obj,
                    space = SpaceId(space),
                    switchSpaceIfObjectFound = true
                )
                when (result) {
                    is DeepLinkToObjectDelegate.Result.Success ->
                        commandsChannel.send(SearchNavigation.OpenObject(navigation))
                    else -> commandsChannel.send(
                        SearchNavigation.Toast(R.string.search_v2_could_not_open_object)
                    )
                }
            }
        }
    }

    // endregion

    class Factory @Inject constructor(
        private val params: VmParams,
        private val searchWithMeta: SearchWithMeta,
        private val crossSpaceSearch: CrossSpaceSearchObjects,
        private val searchChatMessages: SearchChatMessages,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val participants: ParticipantSubscriptionContainer,
        private val chatsDetails: ChatsDetailsSubscriptionContainer,
        private val crossSpaceTypes: CrossSpaceObjectTypesContainer,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder,
        private val fieldParser: FieldParser,
        private val dispatchers: AppCoroutineDispatchers,
        private val deepLinkToObject: DeepLinkToObjectDelegate,
        private val spaceManager: SpaceManager,
        private val saveCurrentSpace: SaveCurrentSpace
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(
            vmParams = params,
            searchWithMeta = searchWithMeta,
            crossSpaceSearch = crossSpaceSearch,
            searchChatMessages = searchChatMessages,
            spaceViews = spaceViews,
            participants = participants,
            chatsDetails = chatsDetails,
            crossSpaceTypes = crossSpaceTypes,
            storeOfObjectTypes = storeOfObjectTypes,
            configStorage = configStorage,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            dispatchers = dispatchers,
            deepLinkToObject = deepLinkToObject,
            spaceManager = spaceManager,
            saveCurrentSpace = saveCurrentSpace
        ) as T
    }

    companion object {

        val OBJECT_KEYS: List<String> = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.NAME,
            Relations.SNIPPET,
            Relations.ICON_EMOJI,
            Relations.ICON_IMAGE,
            Relations.ICON_OPTION,
            Relations.ICON_NAME,
            Relations.TYPE,
            Relations.LAYOUT,
            Relations.IS_DELETED,
            Relations.IS_ARCHIVED,
            Relations.SOURCE,
            Relations.IDENTITY_PROFILE_LINK,
            Relations.UNIQUE_KEY,
            Relations.CREATED_DATE,
            Relations.LAST_MODIFIED_DATE,
            // File rows display their extension when the name is empty.
            Relations.FILE_EXT,
            // Media browse groups on this key — without it every row lands
            // in one dateless group.
            SearchFilters.ADDED_DATE
        )

        val CONTAINER_KEYS: List<String> = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.NAME,
            Relations.LAYOUT,
            Relations.ICON_EMOJI,
            Relations.ICON_IMAGE,
            Relations.ICON_OPTION,
            Relations.DISCUSSION_ID
        )

        private const val DEBOUNCE_MS = 300L
        private const val RECENT_LIMIT = 20
        private const val PAGE_SIZE = 100
        private const val CHANNEL_MATCH_LIMIT = 3
        private const val LEAD_CAP_SHORT = 3
        private const val LEAD_CAP_FULL = 10
        private const val LEAD_FULL_QUERY_LENGTH = 4
        private const val PERSON_CHIP_LIMIT = 3
        private const val REFINEMENT_PERSON_LIMIT = 5
        private const val REFINEMENT_CHANNEL_LIMIT = 5
        private const val TYPE_CHIPS_LIMIT = 8
        private const val PLACEHOLDER_LABEL = "…"
        private const val DAY_MS = 86_400_000L

        private val EXCLUDED_TYPE_KEYS = setOf(
            ObjectTypeUniqueKeys.TEMPLATE,
            ObjectTypeIds.PARTICIPANT,
            ObjectTypeIds.OBJECT_TYPE,
            ObjectTypeIds.FILE,
            ObjectTypeIds.IMAGE,
            ObjectTypeIds.VIDEO,
            ObjectTypeIds.AUDIO,
            ObjectTypeIds.CHAT_DERIVED,
            // No ObjectTypeIds constant exists for the discussion type yet.
            "ot-discussion",
            ObjectTypeIds.DATE
        )

        private val SCOPED_TYPE_EXCLUDED_LAYOUTS: Set<ObjectType.Layout> = buildSet {
            add(ObjectType.Layout.PARTICIPANT)
            add(ObjectType.Layout.CHAT)
            add(ObjectType.Layout.CHAT_DERIVED)
            addAll(KindBucket.MEDIA.layouts())
        }

        /**
         * First browse page is small (~20 rows — enough to fill the screen,
         * cheap on a cold vault); every later page and all text pages are
         * 100. Limits are per page: paging appends at an offset instead of
         * re-fetching the whole prefix with a growing limit.
         */
        private fun pageLimit(isBrowse: Boolean, pages: Int): Int =
            if (isBrowse && pages == 1) RECENT_LIMIT else PAGE_SIZE

        private fun pageOffset(isBrowse: Boolean, pages: Int): Int = when {
            pages == 1 -> 0
            isBrowse -> RECENT_LIMIT + PAGE_SIZE * (pages - 2)
            else -> PAGE_SIZE * (pages - 1)
        }
    }
}

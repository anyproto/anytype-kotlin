package com.anytypeio.anytype.presentation.quickcapture

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.ai.TypeSuggestionEngine
import com.anytypeio.anytype.domain.base.Resultat
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
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class QuickCaptureViewModelTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var userPermissionProvider: UserPermissionProvider
    @Mock lateinit var spaceManager: SpaceManager
    @Mock lateinit var settings: UserSettingsRepository
    @Mock lateinit var createObject: CreateObject
    @Mock lateinit var fetchQuickCaptureDraft: FetchQuickCaptureDraft

    @Mock lateinit var setObjectDetails: SetObjectDetails
    @Mock lateinit var deleteObjects: DeleteObjects
    @Mock lateinit var moveQuickCaptureDraft: MoveQuickCaptureDraft
    @Mock lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer
    @Mock lateinit var storeOfObjectTypes: StoreOfObjectTypes
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    @Mock lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider

    private val targetSpace = MockDataFactory.randomUuid()
    private val spaceView = StubSpaceView(targetSpaceId = targetSpace)
    private val spaceViews: SpaceViewSubscriptionContainer = mock {
        on { observe() } doReturn MutableStateFlow(listOf(spaceView))
    }
    private val draftId = MockDataFactory.randomUuid()

    private fun vm() = QuickCaptureViewModel(
        spaceViews = spaceViews,
        userPermissionProvider = userPermissionProvider,
        spaceManager = spaceManager,
        settings = settings,
        createObject = createObject,
        fetchQuickCaptureDraft = fetchQuickCaptureDraft,
        setObjectDetails = setObjectDetails,
        deleteObjects = deleteObjects,
        moveQuickCaptureDraft = moveQuickCaptureDraft,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        storeOfObjectTypes = storeOfObjectTypes,
        urlBuilder = mock(),
        analytics = analytics,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
        typeSuggestionEngine = TypeSuggestionEngine.NoOp,
    )

    @Before
    fun setup() {
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(mapOf(targetSpace to SpaceMemberPermissions.OWNER))
        }
        spaceSyncAndP2PStatusProvider.stub {
            on { observe() } doReturn emptyFlow()
        }
        storelessSubscriptionContainer.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn flowOf(emptyList())
        }
        settings.stub {
            onBlocking { getSpaceLastInteractions() } doReturn emptyMap()
            onBlocking { getQuickCaptureDraft(SpaceId(targetSpace)) } doReturn null
        }
        spaceManager.stub {
            onBlocking { set(targetSpace, false) } doReturn Result.success(mock<Config>())
        }
        createObject.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                CreateObject.Result(
                    objectId = draftId,
                    event = Payload(context = draftId, events = emptyList()),
                    appliedTemplate = null,
                    typeKey = TypeKey("ot-page"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.IS_HIDDEN to true))
                )
            )
        }
    }

    /**
     * Regression guard for "the sheet immediately closes after open": with healthy inputs
     * (editable space present, space opens, draft creates), onStart must land on Ready and
     * must NOT emit a Dismiss command.
     */
    @Test
    fun `open with healthy inputs reaches Ready and never dismisses`() = runTest {
        val vm = vm()
        vm.commands.test {
            vm.onStart()
            coroutineTestRule.advanceUntilIdle()
            expectNoEvents()
        }
        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready, "expected Ready, was $state")
        assertEquals(targetSpace, state.space.id)
        assertEquals(draftId, state.draft)
    }

    @Test
    fun `space open failure dismisses the sheet`() = runTest {
        spaceManager.stub {
            onBlocking { set(targetSpace, false) } doReturn Result.failure(
                IllegalStateException("space is not ready")
            )
        }
        val vm = vm()
        vm.commands.test {
            vm.onStart()
            coroutineTestRule.advanceUntilIdle()
            val command = awaitItem()
            assertTrue(command is QuickCaptureViewModel.Command.Dismiss && command.result == null)
        }
    }

    @Test
    fun `restores an existing hidden draft instead of creating a new one`() = runTest {
        val existing = MockDataFactory.randomUuid()
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(targetSpace)) } doReturn existing
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                ObjectWrapper.Basic(
                    mapOf(
                        Relations.ID to existing,
                        Relations.IS_HIDDEN to true
                    )
                )
            )
        }
        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()
        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(existing, state.draft)
    }

    /**
     * Reopening quick capture must return the user to their unfinished thought: the last
     * space captured into wins over list order when its draft is still pending.
     */
    @Test
    fun `restores the last capture space when its draft is still pending`() = runTest {
        val otherSpace = MockDataFactory.randomUuid()
        val pendingDraft = MockDataFactory.randomUuid()
        // targetSpace is pinned, so it sorts first — the last-space pointer must still win.
        spaceViews.stub {
            on { observe() } doReturn MutableStateFlow(
                listOf(
                    StubSpaceView(targetSpaceId = targetSpace, spaceOrder = "a"),
                    StubSpaceView(targetSpaceId = otherSpace)
                )
            )
        }
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    otherSpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        spaceManager.stub {
            onBlocking { set(otherSpace, false) } doReturn Result.success(mock<Config>())
        }
        settings.stub {
            onBlocking { getQuickCaptureLastSpace() } doReturn otherSpace
            onBlocking { getQuickCaptureDraft(SpaceId(otherSpace)) } doReturn pendingDraft
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                ObjectWrapper.Basic(
                    mapOf(Relations.ID to pendingDraft, Relations.IS_HIDDEN to true)
                )
            )
        }

        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()

        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(otherSpace, state.space.id)
        assertEquals(pendingDraft, state.draft)
        // Pre-validated: no second create in the restored space.
        verifyBlocking(createObject, never()) { async(any()) }
    }

    @Test
    fun `falls back to list order when the last capture space has no live draft`() = runTest {
        val otherSpace = MockDataFactory.randomUuid()
        spaceViews.stub {
            on { observe() } doReturn MutableStateFlow(
                listOf(
                    StubSpaceView(targetSpaceId = targetSpace, spaceOrder = "a"),
                    StubSpaceView(targetSpaceId = otherSpace)
                )
            )
        }
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    otherSpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        settings.stub {
            onBlocking { getQuickCaptureLastSpace() } doReturn otherSpace
            // Pointer is stale — the draft was deleted elsewhere.
            onBlocking { getQuickCaptureDraft(SpaceId(otherSpace)) } doReturn MockDataFactory.randomUuid()
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(null)
        }

        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()

        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(targetSpace, state.space.id, "pinned space should win the fallback")
        assertEquals(draftId, state.draft)
    }

    @Test
    fun `rejects a pointer to a published (non-hidden) object and creates fresh`() = runTest {
        val published = MockDataFactory.randomUuid()
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(targetSpace)) } doReturn published
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                ObjectWrapper.Basic(mapOf(Relations.ID to published))
            )
        }
        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()
        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(draftId, state.draft)
    }

    /**
     * The store query is scoped to drafts this participant created, so another member's
     * hidden object simply is not returned. The view model must then start its own draft
     * rather than treat the empty result as a reason to act on the pointed-at object.
     */
    @Test
    fun `starts a fresh draft when the scoped query returns nothing`() = runTest {
        val foreignDraft = MockDataFactory.randomUuid()
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(targetSpace)) } doReturn foreignDraft
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(null)
        }

        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()

        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(draftId, state.draft, "must create its own draft, not adopt theirs")
        verifyBlocking(deleteObjects, never()) { async(any()) }
    }

    private val conflictSpace = MockDataFactory.randomUuid()
    private val conflictDraft = MockDataFactory.randomUuid()

    /**
     * Both spaces hold a draft with content: [targetSpace] is on screen with [draftId], and
     * [conflictSpace] already holds [conflictDraft].
     */
    private fun vmWithConflict(): QuickCaptureViewModel {
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    conflictSpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(conflictSpace)) } doReturn conflictDraft
        }
        spaceManager.stub {
            onBlocking { set(conflictSpace, false) } doReturn Result.success(mock<Config>())
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                ObjectWrapper.Basic(
                    mapOf(
                        Relations.ID to conflictDraft,
                        Relations.IS_HIDDEN to true,
                        Relations.NAME to "already here"
                    )
                )
            )
        }
        deleteObjects.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()
        return vm
    }

    /**
     * The target space's draft carries body text but no title. Emptiness is content-based, so
     * this must raise the replace confirmation — the delete behind it is Object.ListDelete,
     * which is permanent. Regression guard for the fetch that omitted SNIPPET and therefore
     * read every body-only draft as empty.
     */
    @Test
    fun `treats a body-only draft in the target space as content worth confirming`() = runTest {
        val otherSpace = MockDataFactory.randomUuid()
        val otherDraft = MockDataFactory.randomUuid()
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    otherSpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(otherSpace)) } doReturn otherDraft
        }
        spaceManager.stub {
            onBlocking { set(otherSpace, false) } doReturn Result.success(mock<Config>())
        }
        fetchQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                ObjectWrapper.Basic(
                    mapOf(
                        Relations.ID to otherDraft,
                        Relations.IS_HIDDEN to true,
                        // No NAME: the only evidence of content is the snippet.
                        Relations.SNIPPET to "call the dentist"
                    )
                )
            )
        }

        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()
        vm.onSpaceSelected(target = otherSpace, sourceHasContent = true)
        coroutineTestRule.advanceUntilIdle()

        val conflict = vm.draftConflict.value
        assertTrue(
            conflict != null,
            "a body-only draft in the target space must raise the conflict, not be replaced"
        )
        verifyBlocking(deleteObjects, never()) { async(any()) }

        // The mock returns the snippet whatever is asked for, so the behavioural assertion
        // above cannot catch the actual defect — the middleware returns only requested keys,
        // and omitting SNIPPET made every body-only draft read as empty. Assert the request.
        val params = argumentCaptor<FetchQuickCaptureDraft.Params>()
        verifyBlocking(fetchQuickCaptureDraft, atLeastOnce()) { async(params.capture()) }
        assertTrue(
            params.allValues.all { it.keys.contains(Relations.SNIPPET) },
            "draft validation must request SNIPPET, else body-only drafts read as empty"
        )
    }

    /**
     * Keeping both must leave the current draft exactly where it is and open the target's own
     * draft — no move, and above all no delete: the target's draft is off-screen, so the user
     * cannot judge what replacing it would cost.
     */
    @Test
    fun `keeping both drafts opens the target draft and destroys nothing`() = runTest {
        val vm = vmWithConflict()
        vm.onSpaceSelected(target = conflictSpace, sourceHasContent = true)
        coroutineTestRule.advanceUntilIdle()

        vm.onKeepCurrentDraftChosen()
        coroutineTestRule.advanceUntilIdle()

        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(conflictSpace, state.space.id)
        assertEquals(conflictDraft, state.draft, "must open the target space's existing draft")
        verifyBlocking(deleteObjects, never()) { async(any()) }
        verifyBlocking(moveQuickCaptureDraft, never()) { async(any()) }
        verifyBlocking(settings, never()) { clearQuickCaptureDraft(SpaceId(targetSpace)) }
    }

    /**
     * A reopened draft the user never touched must not follow the chip on its own. The target
     * space has no draft, so nothing is at stake there — the question is purely whether this
     * older note should be relocated.
     */
    @Test
    fun `asks before moving a draft the user has not edited this session`() = runTest {
        val emptySpace = MockDataFactory.randomUuid()
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    emptySpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(emptySpace)) } doReturn null
        }
        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()

        vm.onSpaceSelected(
            target = emptySpace,
            sourceHasContent = true,
            sourceEditedThisSession = false
        )
        coroutineTestRule.advanceUntilIdle()

        assertTrue(vm.moveOrNewPrompt.value != null, "an untouched draft must not move silently")
        verifyBlocking(moveQuickCaptureDraft, never()) { async(any()) }
    }

    /** Typing this session restores the plain "text follows the chip" behaviour. */
    @Test
    fun `moves without asking when the user typed this session`() = runTest {
        val emptySpace = MockDataFactory.randomUuid()
        userPermissionProvider.stub {
            on { all() } doReturn flowOf(
                mapOf(
                    targetSpace to SpaceMemberPermissions.OWNER,
                    emptySpace to SpaceMemberPermissions.OWNER
                )
            )
        }
        settings.stub {
            onBlocking { getQuickCaptureDraft(SpaceId(emptySpace)) } doReturn null
        }
        spaceManager.stub {
            onBlocking { set(emptySpace, false) } doReturn Result.success(mock<Config>())
        }
        moveQuickCaptureDraft.stub {
            onBlocking { async(any()) } doReturn Resultat.success(MockDataFactory.randomUuid())
        }
        val vm = vm()
        vm.onStart()
        coroutineTestRule.advanceUntilIdle()

        vm.onSpaceSelected(
            target = emptySpace,
            sourceHasContent = true,
            sourceEditedThisSession = true
        )
        coroutineTestRule.advanceUntilIdle()

        assertTrue(vm.moveOrNewPrompt.value == null)
        verifyBlocking(moveQuickCaptureDraft) { async(any()) }
    }

    /**
     * Discarding deletes only the draft the user can see, and still never touches the
     * target's.
     */
    @Test
    fun `discarding the current draft deletes it and leaves the target draft intact`() = runTest {
        val vm = vmWithConflict()
        vm.onSpaceSelected(target = conflictSpace, sourceHasContent = true)
        coroutineTestRule.advanceUntilIdle()

        vm.onDiscardCurrentDraftChosen()
        coroutineTestRule.advanceUntilIdle()

        val state = vm.screenState.value
        assertTrue(state is QuickCaptureViewModel.ScreenState.Ready)
        assertEquals(conflictDraft, state.draft)
        // Exactly the visible draft, never the target's.
        val deleted = argumentCaptor<DeleteObjects.Params>()
        verifyBlocking(deleteObjects) { async(deleted.capture()) }
        assertEquals(listOf(draftId), deleted.lastValue.targets)
        verifyBlocking(settings) { clearQuickCaptureDraft(SpaceId(targetSpace)) }
    }
}

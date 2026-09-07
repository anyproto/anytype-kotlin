package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Response
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking

class MoveQuickCaptureDraftTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var getDefaultObjectType: GetDefaultObjectType

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var settings: UserSettingsRepository

    lateinit var usecase: MoveQuickCaptureDraft

    private val fromSpace = SpaceId(MockDataFactory.randomUuid())
    private val toSpace = SpaceId(MockDataFactory.randomUuid())
    private val sourceDraft = MockDataFactory.randomUuid()
    private val createdDraft = MockDataFactory.randomUuid()
    private val typeId = MockDataFactory.randomUuid()
    private val typeKey = "ot-task"
    private val rootId = sourceDraft
    private val headerId = MockDataFactory.randomUuid()
    private val paragraphId = MockDataFactory.randomUuid()

    private val paragraph = Block(
        id = paragraphId,
        children = emptyList(),
        content = Block.Content.Text(
            text = "Buy milk",
            style = Block.Content.Text.Style.P,
            marks = emptyList()
        ),
        fields = Block.Fields.empty()
    )

    /**
     * The target draft as it looks after a successful paste. [withContent] false models the
     * failure the move must catch: a copy/paste that reports success but carries nothing.
     */
    private fun targetView(withContent: Boolean = true) = ObjectView(
        root = createdDraft,
        blocks = buildList {
            add(
                Block(
                    id = createdDraft,
                    children = if (withContent) listOf(paragraphId) else emptyList(),
                    content = Block.Content.Smart,
                    fields = Block.Fields.empty()
                )
            )
            if (withContent) add(paragraph)
        },
        details = emptyMap(),
        objectRestrictions = emptyList(),
        dataViewRestrictions = emptyList()
    )

    private fun sourceView(withContent: Boolean = true) = ObjectView(
        root = rootId,
        blocks = buildList {
            add(
                Block(
                    id = rootId,
                    children = buildList {
                        add(headerId)
                        if (withContent) add(paragraphId)
                    },
                    content = Block.Content.Smart,
                    fields = Block.Fields.empty()
                )
            )
            add(
                Block(
                    id = headerId,
                    children = emptyList(),
                    content = Block.Content.Layout(Block.Content.Layout.Type.HEADER),
                    fields = Block.Fields.empty()
                )
            )
            if (withContent) add(paragraph)
        },
        details = mapOf(
            rootId to mapOf(
                Relations.NAME to "Groceries",
                Relations.TYPE to listOf(typeId)
            ),
            typeId to mapOf(
                Relations.UNIQUE_KEY to typeKey
            )
        ),
        objectRestrictions = emptyList(),
        dataViewRestrictions = emptyList()
    )

    private val emptyPayload = Payload(context = MockDataFactory.randomUuid(), events = emptyList())

    @Before
    fun setup() {
        usecase = MoveQuickCaptureDraft(
            repo = repo,
            getDefaultObjectType = getDefaultObjectType,
            settings = settings,
            logger = logger,
            dispatchers = dispatchers
        )
    }

    private fun stubHappyPath(
        typeInTargetSpace: Boolean,
        hiddenApplied: Boolean = true,
        targetCarriesContent: Boolean = true
    ) {
        repo.stub {
            onBlocking { getObject(sourceDraft, fromSpace) } doReturn sourceView()
            // Read back after the paste: the move verifies the text actually landed before
            // it permanently deletes the source.
            onBlocking { getObject(createdDraft, toSpace) } doReturn targetView(targetCarriesContent)
            onBlocking {
                searchObjects(
                    space = toSpace,
                    filters = listOf(
                        DVFilter(
                            relation = Relations.UNIQUE_KEY,
                            value = typeKey,
                            condition = DVFilterCondition.EQUAL
                        ),
                        DVFilter(
                            relation = Relations.IS_ARCHIVED,
                            value = true,
                            condition = DVFilterCondition.NOT_EQUAL
                        ),
                        DVFilter(
                            relation = Relations.IS_DELETED,
                            value = true,
                            condition = DVFilterCondition.NOT_EQUAL
                        )
                    ),
                    limit = 1,
                    keys = listOf(Relations.ID, Relations.UNIQUE_KEY)
                )
            } doReturn if (typeInTargetSpace) listOf(mapOf(Relations.ID to typeId)) else emptyList()
            onBlocking { createObject(any()) } doReturn CreateObjectResult(
                id = createdDraft,
                event = emptyPayload,
                details = if (hiddenApplied) mapOf(Relations.IS_HIDDEN to true) else emptyMap()
            )
            onBlocking { copy(any()) } doReturn Response.Clipboard.Copy(
                text = "Buy milk",
                html = null,
                blocks = listOf(paragraph)
            )
            onBlocking { create(any()) } doReturn Pair(MockDataFactory.randomUuid(), emptyPayload)
            onBlocking { paste(any()) } doReturn Response.Clipboard.Paste(
                cursor = 0,
                isSameBlockCursor = false,
                blocks = emptyList(),
                payload = emptyPayload
            )
            onBlocking { deleteObjects(any()) } doAnswer { }
            onBlocking { setObjectDetails(any(), any()) } doReturn emptyPayload
        }
    }

    @Test
    fun `moves content and carries the source type when it exists in the target space`() = runBlocking {
        stubHappyPath(typeInTargetSpace = true)

        val result = usecase.run(
            MoveQuickCaptureDraft.Params(
                from = sourceDraft,
                fromSpace = fromSpace,
                toSpace = toSpace
            )
        )

        assertEquals(createdDraft, result)

        val createCaptor = argumentCaptor<Command.CreateObject>()
        verifyBlocking(repo) { createObject(createCaptor.capture()) }
        val command = createCaptor.firstValue
        assertEquals(TypeKey(typeKey), command.typeKey)
        assertEquals(toSpace, command.space)
        assertEquals(true, command.prefilled[Relations.IS_HIDDEN])
        assertEquals("Groceries", command.prefilled[Relations.NAME])
        assertTrue(command.internalFlags.contains(InternalFlags.ShouldEmptyDelete))
        // Type already carried over — the type bar must not pop open again.
        assertTrue(!command.internalFlags.contains(InternalFlags.ShouldSelectType))

        verifyBlocking(repo) { copy(any()) }
        verifyBlocking(repo) { create(any()) }
        verifyBlocking(repo) { paste(any()) }
        verifyBlocking(repo) { deleteObjects(listOf(sourceDraft)) }
        // No fallback needed.
        verifyBlocking(getDefaultObjectType, never()) { run(any()) }
    }

    @Test
    fun `falls back to the target space default type and reopens the type bar`() = runBlocking {
        stubHappyPath(typeInTargetSpace = false)
        val defaultType = TypeKey("ot-page")
        getDefaultObjectType.stub {
            onBlocking { run(toSpace) } doReturn GetDefaultObjectType.Response(
                id = TypeId(MockDataFactory.randomUuid()),
                type = defaultType,
                name = null,
                defaultTemplate = null
            )
        }

        usecase.run(
            MoveQuickCaptureDraft.Params(
                from = sourceDraft,
                fromSpace = fromSpace,
                toSpace = toSpace
            )
        )

        val createCaptor = argumentCaptor<Command.CreateObject>()
        verifyBlocking(repo) { createObject(createCaptor.capture()) }
        assertEquals(defaultType, createCaptor.firstValue.typeKey)
        assertTrue(createCaptor.firstValue.internalFlags.contains(InternalFlags.ShouldSelectType))
    }

    @Test
    fun `rolls back the created target draft and keeps the source when paste fails`() = runBlocking {
        stubHappyPath(typeInTargetSpace = true)
        repo.stub {
            onBlocking { paste(any()) } doAnswer { throw IllegalStateException("MW error") }
        }

        assertFailsWith<IllegalStateException> {
            usecase.run(
                MoveQuickCaptureDraft.Params(
                    from = sourceDraft,
                    fromSpace = fromSpace,
                    toSpace = toSpace
                )
            )
        }

        // The half-created hidden copy is rolled back; the source draft is never deleted.
        verifyBlocking(repo) { deleteObjects(listOf(createdDraft)) }
        verifyBlocking(repo, never()) { deleteObjects(listOf(sourceDraft)) }
    }

    @Test
    fun `applies the isHidden fallback when create ignores prefilled details`() = runBlocking {
        stubHappyPath(typeInTargetSpace = true, hiddenApplied = false)

        usecase.run(
            MoveQuickCaptureDraft.Params(
                from = sourceDraft,
                fromSpace = fromSpace,
                toSpace = toSpace
            )
        )

        // Both markers are restored: isHidden keeps it out of the UI, isDraft keeps it
        // findable by the cross-device discovery query.
        verifyBlocking(repo) {
            setObjectDetails(
                createdDraft,
                mapOf(
                    Relations.IS_HIDDEN to true,
                    Relations.IS_DRAFT to true
                )
            )
        }
    }

    @Test
    fun `moves nothing but still recreates and deletes for an empty draft`() = runBlocking {
        stubHappyPath(typeInTargetSpace = true)
        repo.stub {
            onBlocking { getObject(sourceDraft, fromSpace) } doReturn sourceView(withContent = false)
        }

        val result = usecase.run(
            MoveQuickCaptureDraft.Params(
                from = sourceDraft,
                fromSpace = fromSpace,
                toSpace = toSpace
            )
        )

        assertEquals(createdDraft, result)
        verifyBlocking(repo, never()) { copy(any()) }
        verifyBlocking(repo, never()) { paste(any()) }
        verifyBlocking(repo) { deleteObjects(listOf(sourceDraft)) }
    }

    @Test
    fun `keeps the source when the paste reports success but carries no text`() = runBlocking {
        // Object.ListDelete is permanent, so a silently empty copy must not be committed.
        stubHappyPath(typeInTargetSpace = true, targetCarriesContent = false)

        assertFailsWith<IllegalStateException> {
            usecase.run(
                MoveQuickCaptureDraft.Params(
                    from = sourceDraft,
                    fromSpace = fromSpace,
                    toSpace = toSpace
                )
            )
        }

        // The half-created target is rolled back and the source survives intact.
        verifyBlocking(repo) { deleteObjects(listOf(createdDraft)) }
        verifyBlocking(repo, never()) { deleteObjects(listOf(sourceDraft)) }
    }

    @Test
    fun `points the target space at the moved draft before deleting the source`() = runBlocking {
        stubHappyPath(typeInTargetSpace = true)

        usecase.run(
            MoveQuickCaptureDraft.Params(
                from = sourceDraft,
                fromSpace = fromSpace,
                toSpace = toSpace
            )
        )

        // Ordering is the whole point: a hidden draft no pointer names is unreachable, so the
        // pointer must exist before the only other copy is destroyed.
        val order = inOrder(settings, repo)
        order.verifyBlocking(settings) { setQuickCaptureDraft(toSpace, createdDraft) }
        order.verifyBlocking(repo) { deleteObjects(listOf(sourceDraft)) }
        verifyBlocking(settings) { clearQuickCaptureDraft(fromSpace) }
    }
}

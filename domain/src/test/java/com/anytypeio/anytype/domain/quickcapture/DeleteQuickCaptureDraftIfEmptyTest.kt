package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking

/**
 * This use case issues a permanent delete, so the tests are weighted toward everything that
 * must NOT be deleted. The image case is the one that motivated the block-level check: an
 * image-only draft has no name and no snippet, so a text-based emptiness test would have
 * called it empty and destroyed the image.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeleteQuickCaptureDraftIfEmptyTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var settings: UserSettingsRepository

    @Mock
    lateinit var logger: Logger

    private lateinit var usecase: DeleteQuickCaptureDraftIfEmpty

    private val space = SpaceId(MockDataFactory.randomUuid())
    private val draft = MockDataFactory.randomUuid()
    private val headerId = MockDataFactory.randomUuid()
    private val bodyId = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        usecase = DeleteQuickCaptureDraftIfEmpty(
            repo = repo,
            settings = settings,
            logger = logger,
            dispatchers = dispatchers
        )
    }

    private fun view(
        body: Block.Content? = null,
        name: String? = null,
        isDraft: Any? = true,
        isHidden: Any? = true
    ) = ObjectView(
        root = draft,
        blocks = buildList {
            add(
                Block(
                    id = draft,
                    children = buildList {
                        add(headerId)
                        if (body != null) add(bodyId)
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
            if (body != null) {
                add(
                    Block(
                        id = bodyId,
                        children = emptyList(),
                        content = body,
                        fields = Block.Fields.empty()
                    )
                )
            }
        },
        details = mapOf(
            draft to buildMap {
                if (name != null) put(Relations.NAME, name)
                if (isDraft != null) put(Relations.IS_DRAFT, isDraft)
                if (isHidden != null) put(Relations.IS_HIDDEN, isHidden)
            }
        ),
        objectRestrictions = emptyList(),
        dataViewRestrictions = emptyList()
    )

    private fun stub(v: ObjectView) {
        repo.stub { onBlocking { getObject(draft, space) } doReturn v }
    }

    private fun run() = runBlocking {
        usecase.run(DeleteQuickCaptureDraftIfEmpty.Params(space = space, draft = draft))
    }

    private val emptyParagraph = Block.Content.Text(
        text = "",
        style = Block.Content.Text.Style.P,
        marks = emptyList()
    )

    @Test
    fun `deletes a draft with no title and an empty paragraph`() {
        stub(view(body = emptyParagraph))

        assertTrue(run())

        verifyBlocking(repo) { deleteObjects(listOf(draft)) }
        verifyBlocking(settings) { clearQuickCaptureDraft(space) }
    }

    @Test
    fun `keeps a draft that holds only an image`() {
        // No name, no snippet — a text-only emptiness test would delete this.
        stub(
            view(
                body = Block.Content.File(
                    targetObjectId = MockDataFactory.randomUuid(),
                    name = "photo.jpg",
                    mime = "image/jpeg",
                    size = 1024,
                    type = Block.Content.File.Type.IMAGE,
                    state = Block.Content.File.State.DONE,
                    addedAt = 0L
                )
            )
        )

        assertFalse(run())

        verifyBlocking(repo, never()) { deleteObjects(any()) }
    }

    @Test
    fun `keeps a draft with a title`() {
        stub(view(body = emptyParagraph, name = "Buy milk"))

        assertFalse(run())

        verifyBlocking(repo, never()) { deleteObjects(any()) }
    }

    @Test
    fun `keeps a draft with body text`() {
        stub(
            view(
                body = Block.Content.Text(
                    text = "call the dentist",
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                )
            )
        )

        assertFalse(run())

        verifyBlocking(repo, never()) { deleteObjects(any()) }
    }

    @Test
    fun `keeps an object that has been published`() {
        // isDraft explicitly false: no longer a draft, whatever else it looks like.
        stub(view(body = emptyParagraph, isDraft = false, isHidden = false))

        assertFalse(run())

        verifyBlocking(repo, never()) { deleteObjects(any()) }
    }

    @Test
    fun `keeps the draft when the object cannot be read`() {
        // A failed read must never be mistaken for "there is nothing here".
        repo.stub {
            onBlocking { getObject(draft, space) } doThrow IllegalStateException("offline")
        }

        assertFalse(run())

        verifyBlocking(repo, never()) { deleteObjects(any()) }
    }
}

package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class ObjectAppearanceChoosePreviewLayoutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var dispatcher: Dispatcher<Payload>

    @Mock
    lateinit var setLinkAppearance: SetLinkAppearance

    private lateinit var viewModel: ObjectAppearanceChoosePreviewLayoutViewModel

    private var storage = Editor.Storage()

    @Before
    fun init() {
        MockitoAnnotations.openMocks(this)
        viewModel = ObjectAppearanceChoosePreviewLayoutViewModel(
            storage = storage,
            setLinkAppearance = setLinkAppearance,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `should fallback to icon size small when changing from card preview to text`() {

        val root = MockDataFactory.randomUuid()
        val target = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Link(
                target = target,
                type = Block.Content.Link.Type.PAGE,
                iconSize = Block.Content.Link.IconSize.MEDIUM,
                cardStyle = Block.Content.Link.CardStyle.CARD,
                description = Block.Content.Link.Description.ADDED,
                relations = setOf()
            ),
            fields = Block.Fields.empty()
        )

        setLinkAppearance.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(context = root, events = listOf())
            )
        }

        storage.document.update(document = listOf(block))

        viewModel.onItemClicked(
            item = ObjectAppearanceChooseSettingsView.PreviewLayout.Text(isSelected = false),
            ctx = root,
            blockId = block.id
        )

        val params = SetLinkAppearance.Params(
            contextId = root,
            content = Block.Content.Link(
                target = target,
                type = Block.Content.Link.Type.PAGE,
                iconSize = Block.Content.Link.IconSize.SMALL,
                cardStyle = Block.Content.Link.CardStyle.TEXT,
                description = Block.Content.Link.Description.ADDED,
                relations = setOf()
            ),
            blockId = block.id
        )

        verifyBlocking(setLinkAppearance, times(1)) {
            invoke(params)
        }

        coroutineTestRule.advanceTime(100)
    }
}
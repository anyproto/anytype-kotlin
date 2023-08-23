package com.anytypeio.anytype.presentation.editor.editor.template

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorTemplateTest: EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should isObjectTemplate true  when opening template object`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.TEMPLATE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                    )
                )
            )
        )
        stubOpenDocument(document = document, details = detailsList)

        val vm = buildViewModel()

        assertFalse(vm.isObjectTemplate())

        vm.onStart(root)

        advanceUntilIdle()

        assertTrue(vm.isObjectTemplate())
    }
}
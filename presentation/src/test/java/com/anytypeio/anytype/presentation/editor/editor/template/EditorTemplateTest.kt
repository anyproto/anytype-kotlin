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
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorTemplateTest: EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        stubInterceptEvents()
        stubInterceptThreadStatus()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should isObjectTemplate true  when opening template object`() = runTest {

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val typeObjectId = RandomString.make()

        val detailsList = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.SPACE_ID to defaultSpace,
                        Relations.TYPE to listOf<String>(typeObjectId),
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                    )
                ),
                typeObjectId to Block.Fields(
                    mapOf(
                        Relations.ID to typeObjectId,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.UNIQUE_KEY to ObjectTypeIds.TEMPLATE
                    )
                )
            )
        )

        stubOpenDocument(
            document = document,
            details = detailsList
        )


        val vm = buildViewModel()

        assertFalse(vm.isObjectTemplate())

        vm.onStart(id = root, space = defaultSpace)

        coroutineTestRule.advanceUntilIdle()

        assertTrue(vm.isObjectTemplate())
    }
}
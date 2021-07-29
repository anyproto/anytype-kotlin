package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyZeroInteractions

class ObjectSetInitializationTest : ObjectSetViewModelTestSetup() {

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    private val ctx: Id = MockDataFactory.randomUuid()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not start creating new record if dv is not initialized yet`() {

        // SETIP

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            )
        )

        openObjectSet.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Left(
                Exception("Error while opening object set")
            )
        }

        val vm = buildViewModel()

        // TESTING

        vm.onStart(ctx = ctx)
        vm.onCreateNewRecord()

        verifyZeroInteractions(createDataViewRecord)
    }
}
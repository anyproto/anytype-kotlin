package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyNoInteractions

class ObjectSetInitializationTest : ObjectSetViewModelTestSetup() {

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))

    private val ctx: Id = MockDataFactory.randomUuid()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    @Test
    fun `should not start creating new record if dv is not initialized yet`() {

        // SETUP

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
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

        val vm = givenViewModel()

        // TESTING

        vm.onStart(ctx = ctx)
        vm.onCreateNewDataViewObject()

       verifyNoInteractions(createNewObject)
    }
}
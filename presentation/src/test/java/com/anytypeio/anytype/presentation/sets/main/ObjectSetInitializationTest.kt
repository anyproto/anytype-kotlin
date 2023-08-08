package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetInitializationTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime(100)
    }

    @Test
    fun `should not start creating new record if dv is not initialized yet`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title),
            details = mockObjectSet.details
        )

        // TESTING
        viewModel.onStart(ctx = root)
        viewModel.proceedWithCreatingNewDataViewObject()

        // ASSERT
        verifyNoInteractions(createObject)
    }

    @Test
    fun `when open object set and setOf has empty value, should not start subscription to the records`() =
        runTest {
            // SETUP
            stubWorkspaceManager(mockObjectSet.workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
                details = mockObjectSet.detailsEmptySetOf
            )

            // TESTING
            viewModel.onStart(ctx = root)

            // ASSERT SUBSCRIPTION START
            verifyNoInteractions(repo)
        }
}
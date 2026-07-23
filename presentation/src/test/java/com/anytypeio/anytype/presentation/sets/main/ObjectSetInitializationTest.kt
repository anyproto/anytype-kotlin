package com.anytypeio.anytype.presentation.sets.main

import android.util.Log
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions
import timber.log.Timber
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetInitializationTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime(100)
    }

    @Test
    fun `should not start creating new record if dv is not initialized yet`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title),
            details = mockObjectSet.details
        )

        // TESTING
        proceedWithStartingViewModel()
        viewModel.proceedWithDataViewObjectCreate()

        // ASSERT
        verifyNoInteractions(createObject)
    }

    @Test
    fun `should not report the not-yet-initialized state at error level`() = runTest {
        // SETUP
        // ERROR is the level SentryTimberIntegration forwards as a reported issue, so anything
        // logged at it during a routine open becomes noise in crash reporting (DROID-4555).
        val reportedAtErrorLevel = mutableListOf<String>()
        val tree = object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority >= Log.ERROR) reportedAtErrorLevel.add(message)
            }
        }
        Timber.plant(tree)
        stubSpaceManager(mockObjectSet.spaceId)
        // No data view block: the state stays uninitialized for the whole test, which is what the
        // collectors started in onStart observe before the object finishes opening.
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title),
            details = mockObjectSet.details
        )

        // TESTING
        try {
            proceedWithStartingViewModel()
            advanceUntilIdle()
        } finally {
            Timber.uproot(tree)
        }

        // ASSERT
        assertTrue(
            reportedAtErrorLevel.none { it.contains(UNINITIALIZED_STATE_MESSAGE) },
            "Observing the not-yet-initialized state is an expected step of opening a set, " +
                    "but it was reported at ERROR: $reportedAtErrorLevel"
        )
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }

    companion object {
        private const val UNINITIALIZED_STATE_MESSAGE = "State was not initialized or null"
    }
}
package com.anytypeio.anytype.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CoroutinesTestRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(DispatchSafeDispatcher(testDispatcher))
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }

    fun advanceTime(millis: Long) {
        testDispatcher.scheduler.apply { advanceTimeBy(millis); runCurrent() }
    }
}

/**
 * Wraps a [TestDispatcher] to safely handle explicit [CoroutineDispatcher.dispatch] calls.
 *
 * [UnconfinedTestDispatcher] throws [UnsupportedOperationException] from [dispatch] because
 * it expects coroutines to check [isDispatchNeeded] first (which returns false, so [dispatch]
 * should never be called). However, Compose's AndroidUiDispatcher calls [dispatch] directly
 * during StateFlow cleanup, bypassing the [isDispatchNeeded] check, causing crashes.
 *
 * This wrapper delegates all operations to the underlying [TestDispatcher] but provides
 * a safe [dispatch] that runs the block immediately instead of throwing.
 */
@OptIn(InternalCoroutinesApi::class)
@ExperimentalCoroutinesApi
private class DispatchSafeDispatcher(
    private val delegate: TestDispatcher
) : CoroutineDispatcher(), Delay {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        delegate.isDispatchNeeded(context)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        (delegate as Delay).scheduleResumeAfterDelay(timeMillis, continuation)
    }

    override fun invokeOnTimeout(
        timeMillis: Long,
        block: Runnable,
        context: CoroutineContext
    ): DisposableHandle {
        return (delegate as Delay).invokeOnTimeout(timeMillis, block, context)
    }
}

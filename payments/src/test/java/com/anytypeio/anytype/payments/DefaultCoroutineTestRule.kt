package com.anytypeio.anytype.payments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCoroutineTestRule() : TestWatcher() {

    val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }

    fun advanceTime(millis: Long = 100L) {
        dispatcher.scheduler.advanceTimeBy(millis)
    }
}
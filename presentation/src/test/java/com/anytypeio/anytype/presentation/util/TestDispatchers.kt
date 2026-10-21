package com.anytypeio.anytype.presentation.util

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
val dispatchers = AppCoroutineDispatchers(
    io = UnconfinedTestDispatcher(),
    main = UnconfinedTestDispatcher(),
    computation = UnconfinedTestDispatcher()
)

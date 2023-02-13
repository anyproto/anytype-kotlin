package com.anytypeio.anytype.presentation.util

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
val dispatchers = AppCoroutineDispatchers(
    io = StandardTestDispatcher(),
    main = StandardTestDispatcher(),
    computation = StandardTestDispatcher()
)
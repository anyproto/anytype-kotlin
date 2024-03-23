package com.anytypeio.anytype.middleware.service

import kotlinx.coroutines.*

class NotificationServiceImplementation(
) : NotificationService {

    private var subscription: Job? = null

    override suspend fun startHandle() {
        subscription?.cancel()
        subscription = CoroutineScope(Dispatchers.IO).launch {

        }
    }

    override fun stopHandle() {
        subscription?.cancel()
        subscription = null
    }
}
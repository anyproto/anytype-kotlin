package com.anytypeio.anytype.middleware.service

interface NotificationService {
    suspend fun startHandle()
    fun stopHandle()
}


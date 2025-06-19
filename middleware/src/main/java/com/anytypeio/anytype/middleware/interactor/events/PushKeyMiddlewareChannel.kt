package com.anytypeio.anytype.middleware.interactor.events

import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.data.auth.event.PushKeyRemoteChannel
import com.anytypeio.anytype.middleware.interactor.EventHandlerChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

class PushKeyMiddlewareChannel(
    private val scope: CoroutineScope,
    private val channel: EventHandlerChannel,
    private val dispatcher: CoroutineDispatcher
) : PushKeyRemoteChannel {

    private val jobs = mutableListOf<Job>()

    private val _pushKeyStatus = MutableStateFlow<PushKeyUpdate>(PushKeyUpdate.EMPTY)
    val pushKeyStatus: Flow<PushKeyUpdate> = _pushKeyStatus.asStateFlow()

    override fun start() {
        Timber.i("PushKeyMiddlewareChannel start")
        jobs.cancel()
        jobs += scope.launch(dispatcher) {
            channel.flow()
                .catch {
                    Timber.w(it, "Error collecting push key updates")
                }
                .collect { emission ->
                    emission.messages.forEach { message ->
//                        message.pushEncryptionKeyUpdate?.let {
//                            val pushKeyUpdate = PushKeyUpdate(
//                                encryptionKeyId = it.encryptionKeyId,
//                                encryptionKey = it.encryptionKey
//                            )
//                            _pushKeyStatus.value = pushKeyUpdate
//                        }
                    }
                }
        }
    }

    override fun stop() {
        Timber.i("PushKeyMiddlewareChannel stop")
        jobs.cancel()
    }

    override fun observe(): Flow<PushKeyUpdate> {
        return pushKeyStatus
    }
}
package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import kotlinx.coroutines.flow.Flow

interface PushKeyChannel {
    fun observe(): Flow<PushKeyUpdate?>
}
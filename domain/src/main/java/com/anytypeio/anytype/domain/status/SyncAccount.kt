package com.anytypeio.anytype.domain.status

import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id

data class SyncAccount(
    val id: Id,
    val name: String,
    val image: Hash,
    val isOnline: Boolean,
    val lastPulled: Int,
    val lastEdited: Int,
    val devices: List<Device>,
) {
    data class Device(
        val name: String,
        val isOnline: Boolean,
        val lastPulled: Int,
        val lastEdited: Int,
    )
}
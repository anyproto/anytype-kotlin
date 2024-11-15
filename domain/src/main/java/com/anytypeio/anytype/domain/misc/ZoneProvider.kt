package com.anytypeio.anytype.domain.misc

import java.time.ZoneId

interface ZoneProvider {
    fun zone(): ZoneId
}
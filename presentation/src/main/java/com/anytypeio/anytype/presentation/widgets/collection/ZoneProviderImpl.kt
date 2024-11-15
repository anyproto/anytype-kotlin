package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.domain.misc.ZoneProvider
import java.time.ZoneId

class ZoneProviderImpl : ZoneProvider {
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
package com.anytypeio.anytype.ext

import com.anytypeio.anytype.core_utils.date.isToday
import com.anytypeio.anytype.core_utils.date.isTomorrow
import com.anytypeio.anytype.domain.account.DateHelper

class DefaultDateHelper : DateHelper {
    override fun isToday(millis: Long): Boolean {
        return millis.isToday()
    }

    override fun isTomorrow(millis: Long): Boolean {
        return millis.isTomorrow()
    }
}
package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Id

/**
 * Stores last created record.
 */
class ObjectSetRecordCache {
    /**
     * Use context id to retrieve record.
     */
    val map = mutableMapOf<Id, DVRecord>()
}
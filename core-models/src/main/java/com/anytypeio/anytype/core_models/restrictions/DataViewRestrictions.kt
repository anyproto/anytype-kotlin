package com.anytypeio.anytype.core_models.restrictions

import com.anytypeio.anytype.core_models.Id

/**
 * restricts for some actions, if present then this action is forbidden
 */
data class DataViewRestrictions(val block: Id, val restrictions: List<DataViewRestriction>)

enum class DataViewRestriction {
    VIEWS,
    RELATION,
    CREATE_OBJECT
}
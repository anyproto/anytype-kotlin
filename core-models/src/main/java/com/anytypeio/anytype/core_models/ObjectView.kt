package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

data class ObjectView(
    val root: Id,
    val blocks: List<Block>,
    val details: Map<Id, Struct>,
    val relations: List<RelationLink>,
    val objectRestrictions: List<ObjectRestriction>,
    val dataViewRestrictions: List<DataViewRestrictions>
)
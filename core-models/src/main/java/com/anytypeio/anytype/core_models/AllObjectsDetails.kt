package com.anytypeio.anytype.core_models

data class AllObjectsDetails(val details: Map<Id, Struct>) {
    companion object {
        val EMPTY = AllObjectsDetails(emptyMap())
    }
}
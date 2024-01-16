package com.anytypeio.anytype.core_models.restrictions

enum class BlockRestriction(val code: Int) {
    READ(1),
    EDIT(2),
    REMOVE(3),
    DRAG(4),
    DROP_ON(5)
}
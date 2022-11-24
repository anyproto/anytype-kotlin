package com.anytypeio.anytype.core_models.restrictions

/**
 * restricts for some actions, if present then this action is forbidden
 */
enum class ObjectRestriction(val code: Int) {

    /**
     * restricts delete
     */
    DELETE(1),

    /**
     * restricts work with relations
     */
    RELATIONS(2),

    /**
     * restricts work with details
     */
    DETAILS(4),

    /**
     * restricts work with blocks
     */
    BLOCKS(3),

    /**
     * restricts changing type
     */
    TYPE_CHANGE(5),

    /**
     * restricts changing layout
     */
    LAYOUT_CHANGE(6),

    TEMPLATE(7),

    DUPLICATE(8),

    NONE(0)

}
package com.anytypeio.anytype.core_models.restrictions

/**
 * restricts for some actions, if present then this action is forbidden
 */
enum class ObjectRestriction {

    /**
     * restricts delete
     */
    DELETE,

    /**
     * restricts work with relations
     */
    RELATIONS,

    /**
     * restricts work with details
     */
    DETAILS,

    /**
     * restricts work with blocks
     */
    BLOCKS,

    /**
     * restricts changing type
     */
    TYPE_CHANGE,

    /**
     * restricts changing layout
     */
    LAYOUT_CHANGE,

    TEMPLATE,

    DUPLICATE

}
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
    RELATION,

    /**
     * restricts edit details
     */
    DETAILS,

    /**
     * restricts create a new block
     */
    CREATE_BLOCK,

    /**
     * restricts changing type
     */
    TYPE_CHANGE,

    /**
     * restricts changing layout
     */
    LAYOUT_CHANGE

}
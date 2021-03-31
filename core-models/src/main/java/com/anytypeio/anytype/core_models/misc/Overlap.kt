package com.anytypeio.anytype.core_models.misc

import com.anytypeio.anytype.core_models.Block.Content.Text.Mark

/**
 * Possible overlappings between [Mark.range] values.
 */
enum class Overlap {

    /**
     * A and B do not overlap, they are equal.
     */
    EQUAL,

    /**
     * A is outer B, A contains B, B is inside A.
     */
    OUTER,

    /**
     * A is inside B, B contains A.
     */
    INNER,

    /**
     * A is left-anchored inside B, i.e. the left side of A is equal to the left side of B.
     */
    INNER_LEFT,

    /**
     * A is right-anchored inside B, i.e. the right side of A is equal to the right side of B.
     */
    INNER_RIGHT,

    /**
     * A overlaps B on the left
     */
    LEFT,

    /**
     * A overlaps B on the right.
     */
    RIGHT,

    /**
     * A and B do not overlap. A preceeds B.
     */
    BEFORE,

    /**
     * A and B do not overlap. B preceeds A.
     */
    AFTER
}
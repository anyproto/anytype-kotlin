package com.anytypeio.anytype.core_models

sealed class InternalFlags(val code: Int) {

    /**
     * show the object type selection interface
     */
    object ShouldSelectType : InternalFlags(1)

    /**
     * Flag to remove the object from the account, in case of closing this object when it is empty
     */
    object ShouldEmptyDelete : InternalFlags(0)

    /**
     * show the template selection interface
     */
    object ShouldSelectTemplate : InternalFlags(2)
}
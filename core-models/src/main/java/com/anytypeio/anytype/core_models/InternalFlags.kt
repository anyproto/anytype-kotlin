package com.anytypeio.anytype.core_models

sealed class InternalFlags {

    /**
     * show the object type selection interface
     */
    object ShouldSelectType : InternalFlags()

    /**
     * Flag to remove the object from the account, in case of closing this object when it is empty
     */
    object ShouldEmptyDelete : InternalFlags()

    /**
     * show the template selection interface
     */
    object ShouldSelectTemplate : InternalFlags()
}
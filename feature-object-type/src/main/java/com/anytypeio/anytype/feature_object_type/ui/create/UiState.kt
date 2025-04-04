package com.anytypeio.anytype.feature_object_type.ui.create

import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class UiTypeSetupTitleAndIconState {

    abstract val icon: ObjectIcon.TypeIcon.Default

    data class CreateNewType(
        override val icon: ObjectIcon.TypeIcon.Default,
        val initialTitle: String = "",
        val initialPlural: String = ""
    ) : UiTypeSetupTitleAndIconState()

    data class EditType(
        override val icon: ObjectIcon.TypeIcon.Default,
        val initialTitle: String = "",
        val initialPlural: String = ""
    ) : UiTypeSetupTitleAndIconState()
}
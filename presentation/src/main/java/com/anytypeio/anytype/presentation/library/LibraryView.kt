package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed interface DependentData {

    class Model(
        val item: LibraryView,
    ) : DependentData

    object None : DependentData

}

sealed interface LibraryView {
    val id: Id
    val name: String
    val dependentData: DependentData

    class MyTypeView(
        override val id: Id,
        override val name: String,
        val icon: ObjectIcon? = null,
        val sourceObject: Id? = null,
        val readOnly: Boolean = false,
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    data class LibraryTypeView(
        override val id: Id,
        override val name: String,
        val icon: ObjectIcon? = null,
        val objectTypeId: Id? = null,
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    class MyRelationView(
        override val id: Id,
        override val name: String,
        val format: RelationFormat,
        val sourceObject: Id? = null,
        val readOnly: Boolean = false,
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    data class LibraryRelationView(
        override val id: Id,
        override val name: String,
        val format: RelationFormat,
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    class UnknownView(
        override val id: Id = "",
        override val name: String = "",
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    class CreateNewTypeView(
        override val id: Id = "",
        override val name: String = "",
        override val dependentData: DependentData = DependentData.None
    ) : LibraryView

    class LibraryTypesPlaceholderView(
        override val id: Id = "",
        override val name: String = "",
        override val dependentData: DependentData = DependentData.None,
    ) : LibraryView

}
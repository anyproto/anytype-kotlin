package com.anytypeio.anytype.feature_properties.add

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.resources.StringResourceProvider

data class UiEditTypePropertiesState(
    val items: List<UiEditTypePropertiesItem>
) {
    companion object {
        val EMPTY = UiEditTypePropertiesState(emptyList())

        val DEFAULT_NEW_PROPERTY_FORMAT = RelationFormat.LONG_TEXT

        //This is a list of formats that are available for creating new properties
        val PROPERTIES_FORMATS = listOf<RelationFormat>(
            RelationFormat.LONG_TEXT,
            RelationFormat.TAG,
            RelationFormat.STATUS,
            RelationFormat.NUMBER,
            RelationFormat.DATE,
            RelationFormat.FILE,
            RelationFormat.OBJECT,
            RelationFormat.CHECKBOX,
            RelationFormat.URL,
            RelationFormat.EMAIL,
            RelationFormat.PHONE
        )
    }
}

sealed class UiEditTypePropertiesItem {

    abstract val id: Id

    sealed class Section : UiEditTypePropertiesItem() {
        data class Types(
            override val id: Id = "section_properties_types_id"
        ) : Section()

        data class Existing(
            override val id: Id = "section_properties_existing_id"
        ) : Section()
    }

    data class Format(
        override val id: Id = "property_item_format_id_${format.ordinal}",
        val format: RelationFormat,
        val prettyName: String
    ) : UiEditTypePropertiesItem()

    data class Create(
        override val id: Id = ID,
        val format: RelationFormat = UiEditTypePropertiesState.Companion.DEFAULT_NEW_PROPERTY_FORMAT,
        val title: String
    ) : UiEditTypePropertiesItem() {
        companion object {
            private const val ID = "create_new_property_id"
        }
    }

    data class Default(
        override val id: Id,
        val format: RelationFormat,
        val propertyKey: Key,
        val title: String,
    ) : UiEditTypePropertiesItem()
}

data class EditTypePropertiesVmParams(
    val objectTypeId: Id,
    val spaceId: SpaceId
)

sealed class UiEditTypePropertiesErrorState {
    data object Hidden : UiEditTypePropertiesErrorState()
    data class Show(val reason: Reason) : UiEditTypePropertiesErrorState()

    sealed class Reason {
        data class ErrorAddingProperty(val msg: String) : Reason()
        data class ErrorCreatingProperty(val msg: String) : Reason()
        data class ErrorUpdatingProperty(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}


//region MAPPING
fun ObjectWrapper.Relation.mapToStateItem(
    stringResourceProvider: StringResourceProvider
): UiEditTypePropertiesItem.Default? {
    val field = this
    if (field.key == Relations.DESCRIPTION) return null
    return UiEditTypePropertiesItem.Default(
        id = field.id,
        propertyKey = field.key,
        title = field.getName(stringResourceProvider),
        format = field.format
    )
}

/**
 * Extension function to safely get a name for the relation.
 * If the name is blank, returns a default untitled title.
 */
fun ObjectWrapper.Relation.getName(stringResourceProvider: StringResourceProvider): String =
    if (name.isNullOrBlank()) {
        stringResourceProvider.getUntitledObjectTitle()
    } else {
        name!!
    }
//endregion

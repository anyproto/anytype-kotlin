package com.anytypeio.anytype.presentation.relations.model

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView

sealed class DefaultObjectRelationValueView {

    abstract val relationKey: Id
    abstract val objectId: Id

    data class Text(
        override val objectId: Id,
        override val relationKey: Id,
        val text: String?
    ) : DefaultObjectRelationValueView()

    data class Number(
        override val objectId: Id,
        override val relationKey: Id,
        val number: String?
    ) : DefaultObjectRelationValueView()

    data class Url(
        override val objectId: Id,
        override val relationKey: Id,
        val url: String?
    ) : DefaultObjectRelationValueView()

    data class Email(
        override val objectId: Id,
        override val relationKey: Id,
        val email: String?
    ) : DefaultObjectRelationValueView()

    data class Phone(
        override val objectId: Id,
        override val relationKey: Id,
        val phone: String?
    ) : DefaultObjectRelationValueView()

    data class Checkbox(
        override val objectId: Id,
        override val relationKey: Id,
        val isChecked: Boolean
    ) : DefaultObjectRelationValueView()

    data class Date(
        override val objectId: Id,
        override val relationKey: Id,
        val timeInMillis: Long? = null,
        val dateFormat: String,
    ) : DefaultObjectRelationValueView()

    data class Tag(
        override val objectId: Id,
        override val relationKey: Id,
        val tags: List<TagView>
    ) : DefaultObjectRelationValueView()

    data class Status(
        override val objectId: Id,
        override val relationKey: Id,
        val status: List<StatusView>
    ) : DefaultObjectRelationValueView()

    data class Object(
        override val objectId: Id,
        override val relationKey: Id,
        val objects: List<ObjectView>
    ) : DefaultObjectRelationValueView()

    data class File(
        override val objectId: Id,
        override val relationKey: Id,
        val files: List<FileView>
    ) : DefaultObjectRelationValueView()
}
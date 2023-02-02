package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView

sealed class DocumentRelationView : DefaultObjectDiffIdentifier {

    abstract val relationId: Id
    abstract val relationKey: Key
    abstract val name: String
    abstract val value: String?
    abstract val isFeatured: Boolean
    abstract val isSystem: Boolean
    abstract val isReadOnly: Boolean

    override val identifier: String get() = relationId

    data class Default(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean = false,
        val format: Relation.Format
    ) : DocumentRelationView()

    data class Checkbox(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean = false,
        val isChecked: Boolean
    ): DocumentRelationView() {
        override val value: String? = null
    }

    data class Status(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean,
        val status: List<StatusView>,
    ) : DocumentRelationView()

    data class Tags(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean = false,
        val tags: List<TagView>,
    ) : DocumentRelationView()

    data class Object(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean = false,
        val objects: List<ObjectView>
    ) : DocumentRelationView()

    sealed class Source : DocumentRelationView() {
        data class Base(
            override val relationId: Id,
            override val relationKey: Key,
            override val name: String,
            override val value: String? = null,
            override val isFeatured: Boolean = false,
            override val isSystem: Boolean,
            override val isReadOnly: Boolean = false,
            val sources: List<ObjectView>
        ) : Source() {
            val isSourceByRelation : Boolean get() = sources.any { s ->
                s is ObjectView.Default && s.isRelation
            }
        }

        data class Deleted(
            override val relationId: Id,
            override val relationKey: Key,
            override val name: String,
            override val value: String? = null,
            override val isSystem: Boolean,
            override val isFeatured: Boolean = false,
            override val isReadOnly: Boolean,
            ) : Source()
    }

    data class File(
        override val relationId: Id,
        override val relationKey: Key,
        override val name: String,
        override val value: String? = null,
        override val isFeatured: Boolean = false,
        override val isSystem: Boolean,
        override val isReadOnly: Boolean,
        val files: List<FileView>
    ) : DocumentRelationView()

    sealed class ObjectType : DocumentRelationView() {
        data class Base(
            override val relationId: Id,
            override val relationKey: Key,
            override val name: String,
            override val value: String? = null,
            override val isFeatured: Boolean = false,
            override val isSystem: Boolean,
            override val isReadOnly: Boolean = false,
            val type: Id
        ) : ObjectType()

        data class Deleted(
            override val relationId: Id,
            override val relationKey: Key,
            override val name: String = "",
            override val value: String? = null,
            override val isFeatured: Boolean = false,
            override val isSystem: Boolean,
            override val isReadOnly: Boolean = false
            ) : ObjectType()
    }
}
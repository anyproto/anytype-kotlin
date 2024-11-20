package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView

sealed class ObjectRelationView : DefaultObjectDiffIdentifier {

    abstract val id: Id
    abstract val key: Key
    abstract val name: String
    abstract val value: String?
    abstract val featured: Boolean
    abstract val system: Boolean
    abstract val readOnly: Boolean

    override val identifier: String get() = id

    data class Date(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val relativeDate: RelativeDate?,
        val isTimeIncluded: Boolean = false
    ) : ObjectRelationView()

    data class Default(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val format: Relation.Format
    ) : ObjectRelationView()

    data class Checkbox(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val isChecked: Boolean
    ): ObjectRelationView() {
        override val value: String? = null
    }

    data class Status(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean,
        val status: List<StatusView>,
    ) : ObjectRelationView()

    data class Tags(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val tags: List<TagView>,
    ) : ObjectRelationView()

    data class Object(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val objects: List<ObjectView>
    ) : ObjectRelationView()

    data class Source(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean = false,
        val sources: List<ObjectView.Default>
    ) : ObjectRelationView() {
        val isSourceByRelation: Boolean get() = sources.any { s -> s.isRelation }
    }

    data class File(
        override val id: Id,
        override val key: Key,
        override val name: String,
        override val value: String? = null,
        override val featured: Boolean = false,
        override val system: Boolean,
        override val readOnly: Boolean,
        val files: List<FileView>
    ) : ObjectRelationView()

    sealed class ObjectType : ObjectRelationView() {
        data class Base(
            override val id: Id,
            override val key: Key,
            override val name: String,
            override val value: String? = null,
            override val featured: Boolean = false,
            override val system: Boolean,
            override val readOnly: Boolean = false,
            val type: Id
        ) : ObjectType()

        data class Deleted(
            override val id: Id,
            override val key: Key,
            override val name: String = "",
            override val value: String? = null,
            override val featured: Boolean = false,
            override val system: Boolean,
            override val readOnly: Boolean = false
            ) : ObjectType()
    }

    sealed class Links : ObjectRelationView() {
        data class Backlinks(
            override val id: Id,
            override val key: Key,
            override val name: String,
            override val value: String? = null,
            override val featured: Boolean = false,
            override val system: Boolean,
            override val readOnly: Boolean = false,
            val count: Int
        ) : Links()

        data class From(
            override val id: Id,
            override val key: Key,
            override val name: String,
            override val value: String? = null,
            override val featured: Boolean = false,
            override val system: Boolean,
            override val readOnly: Boolean = false,
            val count: Int
        ) : Links()
    }
}
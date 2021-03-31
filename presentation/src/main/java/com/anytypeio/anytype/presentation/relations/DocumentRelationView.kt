package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView

sealed class DocumentRelationView : DefaultObjectDiffIdentifier {

    abstract val relationId: Id
    abstract val name: String
    abstract val value: String?

    override val identifier: String get() = relationId

    data class Default(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null
    ) : DocumentRelationView()

    data class Status(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        val status: List<StatusView>,
    ) : DocumentRelationView()

    data class Tags(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        val tags: List<TagView>
    ) : DocumentRelationView()

    data class Object(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        val objects: List<ObjectView>
    ) : DocumentRelationView()

    data class File(
        override val relationId: Id,
        override val name: String,
        override val value: String? = null,
        val files: List<FileView>
    ) : DocumentRelationView()
}
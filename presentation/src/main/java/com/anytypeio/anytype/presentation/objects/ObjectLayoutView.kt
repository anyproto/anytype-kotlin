package com.anytypeio.anytype.presentation.objects

sealed class ObjectLayoutView {

    abstract val id: Int
    abstract val isSelected: Boolean
    abstract fun copy(isSelected: Boolean): ObjectLayoutView

    data class Basic(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Profile(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Todo(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Set(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class ObjectType(
        override val id: Int,
        override val isSelected: Boolean
    ) :
        ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Relation(
        override val id: Int,
        override val isSelected: Boolean
    ) :
        ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class File(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Dashboard(
        override val id: Int,
        override val isSelected: Boolean
    ) :
        ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Database(
        override val id: Int,
        override val isSelected: Boolean
    ) :
        ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Note(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Image(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Space(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }

    data class Bookmark(
        override val id: Int,
        override val isSelected: Boolean
    ) : ObjectLayoutView() {
        override fun copy(isSelected: Boolean) = copy(id = id, isSelected = isSelected)
    }
}

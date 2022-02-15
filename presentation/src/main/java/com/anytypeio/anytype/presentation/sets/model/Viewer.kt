package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed class Viewer {

    abstract val id: String
    abstract val title: String

    data class Unsupported(
        override val id: String,
        override val title: String,
        val error: String
    ) : Viewer()

    data class GridView(
        override val id: String,
        val source: String,
        val name: String,
        val columns: List<ColumnView>,
        val rows: @RawValue List<Row> = listOf()
    ) : Viewer() {

        override val title: String get() = name

        data class Row(
            val id: String,
            val name: String? = null,
            val image: Url? = null,
            val emoji: String? = null,
            val type: String? = null,
            val isChecked: Boolean? = null,
            val cells: List<CellView> = emptyList(),
            val layout: ObjectType.Layout? = null
        )

        companion object {

            fun empty(): Viewer = GridView(
                id = "",
                source = "",
                name = "",
                columns = emptyList(),
                rows = emptyList()
            )
        }
    }

    data class GalleryView(
        override val id: String,
        override val title: String,
        val items: List<Item> = listOf(),
        val largeCards: Boolean = false
    ) : Viewer() {
        sealed class Item {
            abstract val objectId: Id
            abstract val name: String
            abstract val icon: ObjectIcon
            abstract val relations: List<DefaultObjectRelationValueView>
            abstract val hideIcon: Boolean
            data class Default(
                override val objectId: Id,
                override val name: String,
                override val icon: ObjectIcon,
                override val relations: List<DefaultObjectRelationValueView>,
                override val hideIcon: Boolean,
                val bigIcon: Boolean
            ) : Item()
            data class Cover(
                override val objectId: Id,
                override val name: String,
                override val icon: ObjectIcon,
                override val relations: List<DefaultObjectRelationValueView>,
                override val hideIcon: Boolean,
                val fitImage: Boolean,
                val cover: CoverView,
                val isCoverLarge: Boolean = false
            ) : Item()
        }
    }

    data class ListView(
        override val id: String,
        override val title: String,
        val items: List<Item> = listOf()
    ) : Viewer() {
        sealed class Item {
            abstract val objectId: Id
            abstract val name: String
            abstract val description: String?
            abstract val icon: ObjectIcon
            abstract val relations: List<DefaultObjectRelationValueView>
            abstract val hideIcon: Boolean
            data class Default(
                override val objectId: Id,
                override val name: String,
                override val description: String?,
                override val icon: ObjectIcon,
                override val hideIcon: Boolean,
                override val relations: List<DefaultObjectRelationValueView>
            ) : Item()
            data class Profile(
                override val objectId: Id,
                override val name: String,
                override val description: String?,
                override val icon: ObjectIcon,
                override val hideIcon: Boolean,
                override val relations: List<DefaultObjectRelationValueView>
            ) : Item()
            data class Task(
                override val objectId: Id,
                override val name: String,
                override val description: String?,
                override val icon: ObjectIcon,
                override val hideIcon: Boolean,
                override val relations: List<DefaultObjectRelationValueView>
            ) : Item()
        }
    }

    enum class SortType { ASC, DESC }
    enum class FilterOperator { And, Or }

    sealed class Filter {

        @Parcelize
        enum class Type : Parcelable { TEXT, NUMBER, SELECTED, CHECKBOX }

        sealed class Condition : Filter() {
            abstract val title: String

            sealed class Text : Condition() {

                data class Equal(val name: String = "Is") : Text() {
                    override val title: String
                        get() = name
                }

                data class NotEqual(val name: String = "Is not") : Text() {
                    override val title: String
                        get() = name
                }

                data class Like(val name: String = "Contains") : Text() {
                    override val title: String
                        get() = name
                }

                data class NotLike(val name: String = "Doesn't contain") : Text() {
                    override val title: String
                        get() = name
                }

                data class Empty(val name: String = "Is empty") : Text() {
                    override val title: String
                        get() = name
                }

                data class NotEmpty(val name: String = "Is not empty") : Text() {
                    override val title: String
                        get() = name
                }

                data class None(val name: String = "All") : Text() {
                    override val title: String
                        get() = name
                }

                companion object {
                    fun textConditions() =
                        listOf(Equal(), NotEqual(), Like(), NotLike(), Empty(), NotEmpty(), None())
                }
            }

            sealed class Number : Condition() {
                data class Equal(val name: String = "Is equal to") : Number() {
                    override val title: String
                        get() = name
                }

                data class NotEqual(val name: String = "Is not equal to") : Number() {
                    override val title: String
                        get() = name
                }

                data class Greater(val name: String = "Is greater than") : Number() {
                    override val title: String
                        get() = name
                }

                data class Less(val name: String = "Is less than") : Number() {
                    override val title: String
                        get() = name
                }

                data class GreaterOrEqual(val name: String = "Is greater than or equal to") :
                    Number() {
                    override val title: String
                        get() = name
                }

                data class LessOrEqual(val name: String = "Is less than or equal to") : Number() {
                    override val title: String
                        get() = name
                }

                data class Empty(val name: String = "Is empty") : Number() {
                    override val title: String
                        get() = name
                }

                data class NotEmpty(val name: String = "Is not empty") : Number() {
                    override val title: String
                        get() = name
                }

                data class None(val name: String = "All") : Number() {
                    override val title: String
                        get() = name
                }

                companion object {
                    fun numberConditions() =
                        listOf(
                            Equal(),
                            NotEqual(),
                            Greater(),
                            Less(),
                            GreaterOrEqual(),
                            LessOrEqual(),
                            Empty(),
                            NotEmpty(),
                            None()
                        )
                }

            }

            sealed class Selected : Condition() {
                data class In(val name: String = "Has any of") : Selected() {
                    override val title: String
                        get() = name
                }

                data class AllIn(val name: String = "Has all of") : Selected() {
                    override val title: String
                        get() = name
                }

                data class Equal(val name: String = "Is exactly") : Selected() {
                    override val title: String
                        get() = name
                }

                data class NotIn(val name: String = "Has none of") : Selected() {
                    override val title: String
                        get() = name
                }

                data class Empty(val name: String = "Is empty") : Selected() {
                    override val title: String
                        get() = name
                }

                data class NotEmpty(val name: String = "Is not empty") : Selected() {
                    override val title: String
                        get() = name
                }

                data class None(val name: String = "All") : Selected() {
                    override val title: String
                        get() = name
                }

                companion object {
                    fun selectConditions() =
                        listOf(In(), AllIn(), Equal(), NotIn(), Empty(), NotEmpty(), None())
                }
            }

            sealed class Checkbox : Condition() {
                data class Equal(val name: String = "Is") : Checkbox() {
                    override val title: String
                        get() = name
                }

                data class NotEqual(val name: String = "Is not") : Checkbox() {
                    override val title: String
                        get() = name
                }

                data class None(val name: String = "All") : Checkbox() {
                    override val title: String
                        get() = name
                }

                companion object {
                    fun checkboxConditions() = listOf(Equal(), NotEqual(), None())
                }
            }
        }
    }
}
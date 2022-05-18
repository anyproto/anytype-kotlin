package com.anytypeio.anytype.presentation.editor.editor.slash

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.const.SlashConst
import com.anytypeio.anytype.core_utils.const.SlashConst.SLASH_OTHER_TOC_ABBREVIATION
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.core_models.ObjectType.Layout as ObjectTypeLayout

sealed class SlashWidgetState {
    data class UpdateItems(
        val mainItems: List<SlashItem>,
        val styleItems: List<SlashItem>,
        val mediaItems: List<SlashItem>,
        val objectItems: List<SlashItem>,
        val relationItems: List<SlashRelationView>,
        val otherItems: List<SlashItem>,
        val actionsItems: List<SlashItem>,
        val alignmentItems: List<SlashItem>,
        val colorItems: List<SlashItem>,
        val backgroundItems: List<SlashItem>
    ) : SlashWidgetState() {
        companion object {
            fun empty() = UpdateItems(
                mainItems = emptyList(),
                styleItems = emptyList(),
                mediaItems = emptyList(),
                objectItems = emptyList(),
                relationItems = emptyList(),
                otherItems = emptyList(),
                actionsItems = emptyList(),
                alignmentItems = emptyList(),
                colorItems = emptyList(),
                backgroundItems = emptyList()
            )
        }
    }
}

sealed class SlashItem {

    abstract fun getSearchName(): String
    abstract fun getAbbreviation(): List<String>?

    //region SUB HEADER
    sealed class Subheader : SlashItem() {

        object Style : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
            override fun getAbbreviation(): List<String>? = null
        }

        object StyleWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
            override fun getAbbreviation(): List<String>? = null
        }

        object Media : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
            override fun getAbbreviation(): List<String>? = null
        }

        object MediaWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
            override fun getAbbreviation(): List<String>? = null
        }

        object ObjectType : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
            override fun getAbbreviation(): List<String>? = null
        }

        object ObjectTypeWithBlack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
            override fun getAbbreviation(): List<String>? = null
        }

        object Other : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
            override fun getAbbreviation(): List<String>? = null
        }

        object OtherWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
            override fun getAbbreviation(): List<String>? = null
        }

        object Actions : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
            override fun getAbbreviation(): List<String>? = null
        }

        object ActionsWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
            override fun getAbbreviation(): List<String>? = null
        }

        object Alignment : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
            override fun getAbbreviation(): List<String>? = null
        }

        object AlignmentWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
            override fun getAbbreviation(): List<String>? = null
        }

        object Color : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
            override fun getAbbreviation(): List<String>? = null
        }

        object ColorWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
            override fun getAbbreviation(): List<String>? = null
        }

        object Background : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
            override fun getAbbreviation(): List<String>? = null
        }

        object BackgroundWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
            override fun getAbbreviation(): List<String>? = null
        }
    }
    //endregion

    object Back : SlashItem() {
        override fun getSearchName(): String = SlashConst.SLASH_BACK
        override fun getAbbreviation(): List<String>? = null
    }

    //region MAIN
    sealed class Main : SlashItem() {
        object Style : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
            override fun getAbbreviation(): List<String>? = null
        }

        object Media : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
            override fun getAbbreviation(): List<String>? = null
        }

        object Objects : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
            override fun getAbbreviation(): List<String>? = null
        }

        object Relations : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_RELATIONS
            override fun getAbbreviation(): List<String>? = null
        }

        object Other : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
            override fun getAbbreviation(): List<String>? = null
        }

        object Actions : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
            override fun getAbbreviation(): List<String>? = null
        }

        object Alignment : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
            override fun getAbbreviation(): List<String>? = null
        }

        object Color : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
            override fun getAbbreviation(): List<String>? = null
        }

        object Background : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
            override fun getAbbreviation(): List<String>? = null
        }
    }
    //endregion

    //region STYLE
    sealed class Style : SlashItem() {

        sealed class Type : Style() {
            object Text : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TEXT
                override fun getAbbreviation(): List<String>? = null
            }

            object Title : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TITLE
                override fun getAbbreviation(): List<String>? = null
            }

            object Heading : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_HEADING
                override fun getAbbreviation(): List<String>? = null
            }

            object Subheading : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_SUBHEADING
                override fun getAbbreviation(): List<String>? = null
            }

            object Highlighted : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_HIGHLIGHTED
                override fun getAbbreviation(): List<String>? = null
            }

            object Callout : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CALLOUT
                override fun getAbbreviation(): List<String>? = null
            }

            object Checkbox : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CHECKBOX
                override fun getAbbreviation(): List<String>? = null
            }

            object Numbered : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_NUMBERED
                override fun getAbbreviation(): List<String>? = null
            }

            object Toggle : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TOGGLE
                override fun getAbbreviation(): List<String>? = null
            }

            object Bulleted : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_BULLETED
                override fun getAbbreviation(): List<String>? = null
            }
        }

        sealed class Markup : Style() {
            object Bold : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_BOLD
                override fun getAbbreviation(): List<String>? = null
            }

            object Italic : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_ITALIC
                override fun getAbbreviation(): List<String>? = null
            }

            object Strikethrough : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_STRIKETHROUGH
                override fun getAbbreviation(): List<String>? = null
            }

            object Code : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CODE
                override fun getAbbreviation(): List<String>? = null
            }
        }
    }
    //endregion

    //region MEDIA
    sealed class Media : SlashItem() {
        object File : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_FILE
            override fun getAbbreviation(): List<String>? = null
        }

        object Picture : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_PICTURE
            override fun getAbbreviation(): List<String>? = null
        }

        object Video : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_VIDEO
            override fun getAbbreviation(): List<String>? = null
        }

        object Bookmark : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_BOOKMARK
            override fun getAbbreviation(): List<String>? = null
        }

        object Code : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_CODE
            override fun getAbbreviation(): List<String>? = null
        }
    }
    //endregion

    //region OBJECT TYPE
    data class ObjectType(
        val url: Url,
        val name: String,
        val emoji: String,
        val description: String?,
        val layout: ObjectTypeLayout
    ) : SlashItem() {
        override fun getSearchName(): String = name
        override fun getAbbreviation(): List<String>? = null
    }
    //endregion

    //region RELATION
    data class Relation(val relation: SlashRelationView.Item) : SlashItem() {
        override fun getSearchName(): String = relation.view.name
        override fun getAbbreviation(): List<String>? = null
    }

    object RelationNew : SlashItem() {
        override fun getSearchName(): String = SlashConst.SLASH_RELATION_NEW
        override fun getAbbreviation(): List<String>? = null
    }
    //endregion

    //region OTHER
    sealed class Other : SlashItem() {
        object Line : Other() {
            override fun getSearchName(): String = SlashConst.SLASH_OTHER_LINE
            override fun getAbbreviation(): List<String>? = null
        }

        object Dots : Other() {
            override fun getSearchName(): String = SlashConst.SLASH_OTHER_DOTS
            override fun getAbbreviation(): List<String>? = null
        }

        /**
         * Table of contents
         */
        object TOC : Other() {
            override fun getSearchName(): String = SlashConst.SLASH_OTHER_TOC
            override fun getAbbreviation(): List<String> = listOf(SLASH_OTHER_TOC_ABBREVIATION)
        }
    }
    //endregion

    //region ACTIONS
    sealed class Actions : SlashItem() {
        object Delete : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_DELETE
            override fun getAbbreviation(): List<String>? = null
        }

        object Duplicate : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_DUPLICATE
            override fun getAbbreviation(): List<String>? = null
        }

        object Copy : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_COPY
            override fun getAbbreviation(): List<String>? = null
        }

        object Paste : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_PASTE
            override fun getAbbreviation(): List<String>? = null
        }

        object Move : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_MOVE
            override fun getAbbreviation(): List<String>? = null
        }

        object MoveTo : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_MOVE_TO
            override fun getAbbreviation(): List<String>? = null
        }

        object CleanStyle : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_CLEAN_STYLE
            override fun getAbbreviation(): List<String>? = null
        }

        object LinkTo : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_LINK_TO
            override fun getAbbreviation(): List<String>? = null
        }
    }
    //endregion

    //region ALIGNMENT
    sealed class Alignment : SlashItem() {
        object Left : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_LEFT
            override fun getAbbreviation(): List<String>? = null
        }

        object Center : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_CENTER
            override fun getAbbreviation(): List<String>? = null
        }

        object Right : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_RIGHT
            override fun getAbbreviation(): List<String>? = null
        }
    }
    //endregion

    //region TEXT COLOR & BACKGROUND
    sealed class Color : SlashItem() {

        abstract val isSelected: Boolean
        abstract val themeColor: ThemeColor

        override fun getSearchName(): String = themeColor.code
        override fun getAbbreviation(): List<String>? = null

        data class Text(
            override val themeColor: ThemeColor,
            override val isSelected: Boolean,
        ) : Color()

        data class Background(
            override val themeColor: ThemeColor,
            override val isSelected: Boolean,
        ) : Color()
    }
    //endregion
}
package com.anytypeio.anytype.presentation.editor.editor.slash

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.const.SlashConst
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.core_models.ObjectType.Layout as ObjectTypeLayout

sealed class SlashWidgetState {
    data class UpdateItems(
        val mainItems: List<SlashItem>,
        val styleItems: List<SlashItem>,
        val mediaItems: List<SlashItem>,
        val objectItems: List<SlashItem>,
        val relationItems: List<RelationListViewModel.Model>,
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

    //region SUB HEADER
    sealed class Subheader : SlashItem() {

        object Style : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
        }

        object StyleWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
        }

        object Media : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
        }

        object MediaWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
        }

        object ObjectType : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
        }

        object ObjectTypeWithBlack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
        }

        object Other : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
        }

        object OtherWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
        }

        object Actions : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
        }

        object ActionsWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
        }

        object Alignment : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
        }

        object AlignmentWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
        }

        object Color : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
        }

        object ColorWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
        }

        object Background : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
        }

        object BackgroundWithBack : Subheader() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
        }
    }
    //endregion

    object Back : SlashItem() {
        override fun getSearchName(): String = SlashConst.SLASH_BACK
    }

    //region MAIN
    sealed class Main : SlashItem() {
        object Style : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_STYLE
        }

        object Media : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_MEDIA
        }

        object Objects : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OBJECTS
        }

        object Relations : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_RELATIONS
        }

        object Other : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_OTHER
        }

        object Actions : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ACTIONS
        }

        object Alignment : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_ALIGNMENT
        }

        object Color : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_COLOR
        }

        object Background : Main() {
            override fun getSearchName(): String = SlashConst.SLASH_MAIN_BACKGROUND
        }
    }
    //endregion

    //region STYLE
    sealed class Style : SlashItem() {

        sealed class Type : Style() {
            object Text : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TEXT
            }

            object Title : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TITLE
            }

            object Heading : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_HEADING
            }

            object Subheading : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_SUBHEADING
            }

            object Highlighted : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_HIGHLIGHTED
            }

            object Callout : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CALLOUT
            }

            object Checkbox : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CHECKBOX
            }

            object Numbered : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_NUMBERED
            }

            object Toggle : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_TOGGLE
            }

            object Bulleted : Type() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_BULLETED
            }
        }

        sealed class Markup : Style() {
            object Bold : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_BOLD
            }

            object Italic : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_ITALIC
            }

            object Breakthrough : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_BREAKTHROUGH
            }

            object Code : Markup() {
                override fun getSearchName(): String = SlashConst.SLASH_STYLE_CODE
            }
        }
    }
    //endregion

    //region MEDIA
    sealed class Media : SlashItem() {
        object File : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_FILE
        }

        object Picture : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_PICTURE
        }

        object Video : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_VIDEO
        }

        object Bookmark : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_BOOKMARK
        }

        object Code : Media() {
            override fun getSearchName(): String = SlashConst.SLASH_MEDIA_CODE
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
    }
    //endregion

    //region RELATION
    data class Relation(val relation: RelationListViewModel.Model.Item) : SlashItem() {
        override fun getSearchName(): String = relation.view.name
    }
    //endregion

    //region OTHER
    sealed class Other : SlashItem() {
        object Line : Other() {
            override fun getSearchName(): String = SlashConst.SLASH_OTHER_LINE
        }

        object Dots : Other() {
            override fun getSearchName(): String = SlashConst.SLASH_OTHER_DOTS
        }
    }
    //endregion

    //region ACTIONS
    sealed class Actions : SlashItem() {
        object Delete : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_DELETE
        }

        object Duplicate : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_DUPLICATE
        }

        object Copy : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_COPY
        }

        object Paste : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_PASTE
        }

        object Move : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_MOVE
        }

        object MoveTo : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_MOVE_TO
        }

        object CleanStyle : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_CLEAN_STYLE
        }

        object LinkTo : Actions() {
            override fun getSearchName(): String = SlashConst.SLASH_ACTION_LINK_TO
        }
    }
    //endregion

    //region ALIGNMENT
    sealed class Alignment : SlashItem() {
        object Left : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_LEFT
        }

        object Center : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_CENTER
        }

        object Right : Alignment() {
            override fun getSearchName(): String = SlashConst.SLASH_ALIGN_RIGHT
        }
    }
    //endregion

    //region TEXT COLOR & BACKGROUND
    sealed class Color: SlashItem() {
        data class Text(val code: String, val isSelected: Boolean) : Color() {
            override fun getSearchName(): String = code
        }

        data class Background(val code: String, val isSelected: Boolean) : Color() {
            override fun getSearchName(): String = code
        }
    }
    //endregion
}
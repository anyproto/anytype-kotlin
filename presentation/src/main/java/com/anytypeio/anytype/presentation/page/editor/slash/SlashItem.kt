package com.anytypeio.anytype.presentation.page.editor.slash

sealed class SlashItem {

    interface Title {
        val title: Int
    }

    interface Subtitle {
        val subtitle: Int
    }

    interface Icon {
        val icon: Int
    }

    interface SlashCategory {
        val category: Int
    }

    //---------- MAIN -----------------
    sealed class Main : SlashItem(), Title, Icon {
        data class Style(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Media(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Objects(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Relations(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Other(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Actions(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Alignment(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Color(
            override val title: Int,
            override val icon: Int
        ) : Main()

        data class Background(
            override val title: Int,
            override val icon: Int
        ) : Main()
    }

    //---------- STYLE -----------------
    sealed class Style : SlashItem() {

        sealed class Type : Style(), SlashCategory, Title, Subtitle, Icon {
            data class Text(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Title(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Heading(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Subheading(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Highlighted(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Callout(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Checkbox(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Numbered(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Toggle(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()

            data class Bulleted(
                override val title: Int,
                override val subtitle: Int,
                override val icon: Int,
                override val category: Int
            ) : Type()
        }

        sealed class Markup : Style(), SlashCategory, Title, Icon {
            data class Bold(
                override val title: Int,
                override val icon: Int,
                override val category: Int
            ) : Markup()

            data class Italic(
                override val title: Int,
                override val icon: Int,
                override val category: Int
            ) : Markup()

            data class Breakthrough(
                override val title: Int,
                override val icon: Int,
                override val category: Int
            ) : Markup()

            data class Code(
                override val title: Int,
                override val icon: Int,
                override val category: Int
            ) : Markup()

            data class Link(
                override val title: Int,
                override val icon: Int,
                override val category: Int
            ) : Markup()
        }
    }

    //---------- MEDIA -----------------
    sealed class Media : SlashItem(), SlashCategory, Title, Subtitle, Icon {
        data class File(
            override val title: Int,
            override val subtitle: Int,
            override val icon: Int,
            override val category: Int
        ) : Media()

        data class Picture(
            override val title: Int,
            override val subtitle: Int,
            override val icon: Int,
            override val category: Int
        ) : Media()

        data class Video(
            override val title: Int,
            override val subtitle: Int,
            override val icon: Int,
            override val category: Int
        ) : Media()

        data class Bookmark(
            override val title: Int,
            override val subtitle: Int,
            override val icon: Int,
            override val category: Int
        ) : Media()

        data class Code(
            override val title: Int,
            override val subtitle: Int,
            override val icon: Int,
            override val category: Int
        ) : Media()
    }

    //---------- OTHER -----------------
    sealed class Other : SlashItem(), SlashCategory, Title, Icon {
        data class Line(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Other()

        data class Dots(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Other()
    }

    //---------- ACTIONS -----------------
    sealed class Actions : SlashItem(), SlashCategory, Title, Icon {
        data class Delete(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class Duplicate(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class Copy(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class Paste(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class Move(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class MoveTo(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()

        data class CleanStyle(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Actions()
    }

    //---------- ALIGNMENT -----------------
    sealed class Alignment : SlashItem(), SlashCategory, Title, Icon {
        data class Left(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Alignment()

        data class Center(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Alignment()

        data class Right(
            override val title: Int,
            override val icon: Int,
            override val category: Int
        ) : Alignment()
    }

    companion object Category {
        const val CATEGORY_MAIN = 1
        const val CATEGORY_STYLE = 2
        const val CATEGORY_MEDIA = 3
        const val CATEGORY_OBJECT_TYPES = 4
        const val CATEGORY_RELATIONS = 5
        const val CATEGORY_OTHER = 6
        const val CATEGORY_ACTIONS = 7
        const val CATEGORY_ALIGNMENT = 8
        const val CATEGORY_COLOR = 9
        const val CATEGORY_BACKGROUND = 10
        const val CATEGORY_HEADER = 11
    }
}
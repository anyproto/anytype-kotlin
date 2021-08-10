package com.anytypeio.anytype.presentation.editor.editor.model

enum class UiBlock {
    TEXT {
        override fun category() = Category.TEXT
    },
    HEADER_ONE {
        override fun category() = Category.TEXT
    },
    HEADER_TWO {
        override fun category() = Category.TEXT
    },
    HEADER_THREE {
        override fun category() = Category.TEXT
    },
    HIGHLIGHTED {
        override fun category() = Category.TEXT
    },
    CHECKBOX {
        override fun category() = Category.LIST
    },
    BULLETED {
        override fun category() = Category.LIST
    },
    NUMBERED {
        override fun category() = Category.LIST
    },
    TOGGLE {
        override fun category() = Category.LIST
    },
    PAGE {
        override fun category() = Category.OBJECT
    },
    LINK_TO_OBJECT {
        override fun category() = Category.OBJECT
    },
    FILE {
        override fun category() = Category.OBJECT
    },
    IMAGE {
        override fun category() = Category.OBJECT
    },
    VIDEO {
        override fun category() = Category.OBJECT
    },
    BOOKMARK {
        override fun category() = Category.OBJECT
    },
    LINE_DIVIDER {
        override fun category() = Category.OTHER
    },
    THREE_DOTS {
        override fun category() = Category.OTHER
    },
    CODE {
        override fun category() = Category.OTHER
    },
    RELATION {
        override fun category() = Category.RELATION
    };

    abstract fun category(): Category

    fun isText(): Boolean = category().let { category ->
        category == Category.TEXT || category == Category.LIST
    }

    fun isCode(): Boolean = this == CODE

    enum class Category {
        TEXT, LIST, OBJECT, RELATION, OTHER
    }
}
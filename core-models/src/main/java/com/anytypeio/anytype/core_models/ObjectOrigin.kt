package com.anytypeio.anytype.core_models

enum class ObjectOrigin(val code: Int) {
    NONE(0),
    CLIPBOARD(1),
    DRAG_AND_DROP(2),
    IMPORT(3),
    WEB_CLIPPER(4),
    SHARING_EXTENSION(5),
    USE_CASE(6),
    BUILT_IN(7)
}
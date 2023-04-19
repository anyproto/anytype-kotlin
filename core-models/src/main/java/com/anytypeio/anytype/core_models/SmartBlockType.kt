package com.anytypeio.anytype.core_models

@Deprecated("To be removed")
enum class SmartBlockType(val code: Int) {
    ACCOUNT_OLD(code = 0),
    PAGE(code = 16),
    PROFILE_PAGE(code = 17),
    HOME(code = 32),
    ARCHIVE(code = 48),
    FILE(code = 256),
    TEMPLATE(code = 288),
    BUNDLED_TEMPLATE(code = 289),
    @Deprecated("")
    BUNDLED_RELATION(code = 512),
    SUB_OBJECT(code = 513),
    BUNDLED_OBJECT_TYPE(code = 514),
    ANYTYPE_PROFILE(code = 515),
    DATE(code = 516),
    WORKSPACE(code = 518),
    WIDGET(code = 112),
    MISSING_OBJECT(code = 519)
}
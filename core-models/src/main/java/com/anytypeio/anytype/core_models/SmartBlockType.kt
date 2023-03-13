package com.anytypeio.anytype.core_models

enum class SmartBlockType(val code: Int) {
    ACCOUNT_OLD(code = 0),
    BREADCRUMBS(code = 1),
    PAGE(code = 16),
    PROFILE_PAGE(code = 17),
    HOME(code = 32),
    ARCHIVE(code = 48),
    DATABASE(code = 64),
    SET(code = 65),
    COLLECTION(code = 66),
    CUSTOM_OBJECT_TYPE(code = 96),
    FILE(code = 256),
    TEMPLATE(code = 288),
    BUNDLED_TEMPLATE(code = 289),
    MARKETPLACE_TYPE(code = 272),
    MARKETPLACE_RELATION(code = 273),
    MARKETPLACE_TEMPLATE(code = 274),
    @Deprecated("")
    BUNDLED_RELATION(code = 512),
    SUB_OBJECT(code = 513),
    BUNDLED_OBJECT_TYPE(code = 514),
    ANYTYPE_PROFILE(code = 515),
    DATE(code = 516),
    WORKSPACE_OLD(code = 517),
    WORKSPACE(code = 518),
    WIDGET(code = 112)
}
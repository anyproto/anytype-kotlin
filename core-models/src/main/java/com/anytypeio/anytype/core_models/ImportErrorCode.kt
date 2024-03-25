package com.anytypeio.anytype.core_models

enum class ImportErrorCode(val code: Int) {
    NULL(0),
    UNKNOWN_ERROR(1),
    BAD_INPUT(2),
    INTERNAL_ERROR(3),
    NO_OBJECTS_TO_IMPORT(5),
    IMPORT_IS_CANCELED(6),
    LIMIT_OF_ROWS_OR_RELATIONS_EXCEEDED(7),
    FILE_LOAD_ERROR(8),
    INSUFFICIENT_PERMISSIONS(9);
}
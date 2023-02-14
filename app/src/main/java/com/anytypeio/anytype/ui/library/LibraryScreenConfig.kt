package com.anytypeio.anytype.ui.library

import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R

data class LibraryConfiguration(
    val types: LibraryScreenConfig = LibraryScreenConfig.Types(),
    val relations: LibraryScreenConfig = LibraryScreenConfig.Relations()
)

sealed class LibraryScreenConfig(
    @StringRes val mainTitle: Int,
    @StringRes val mainBtnTitle: Int,
    @StringRes val description: Int,
    val listConfig: List<LibraryListConfig>,
    val index: Int,
    val titleAlignment: Alignment.Horizontal,
    val titlePaddingEnd: Dp,
    val titlePaddingStart: Dp
) {
    class Types : LibraryScreenConfig(
        R.string.library_title_types,
        R.string.library_button_create_type,
        R.string.library_description_types,
        listOf(LibraryListConfig.Types, LibraryListConfig.TypesLibrary),
        0,
        Alignment.End,
        0.dp,
        16.dp
    )

    class Relations : LibraryScreenConfig(
        R.string.library_title_relations,
        R.string.library_button_create_relation,
        R.string.library_description_relations,
        listOf(LibraryListConfig.Relations, LibraryListConfig.RelationsLibrary),
        1,
        Alignment.Start,
        16.dp,
        0.dp
    )
}

sealed class LibraryListConfig(
    @StringRes val title: Int,
    val subtitleTabOffset: Dp,
) {
    object Types : LibraryListConfig(
        R.string.library_subtitle_types, 0.dp
    )

    object TypesLibrary : LibraryListConfig(
        R.string.library_subtitle_library, (-14).dp
    )

    object Relations : LibraryListConfig(
        R.string.library_subtitle_relations, 0.dp
    )

    object RelationsLibrary : LibraryListConfig(
        R.string.library_subtitle_library, (-14).dp
    )

}
package com.anytypeio.anytype.ui.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.TitleInter15
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel.SpaceView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Preview
@Composable
fun AddToAnytypeScreenUrlPreview() {
    AddToAnytypeScreen(
        data = SharingData.Url("https://en.wikipedia.org/wiki/Walter_Benjamin"),
        onCancelClicked = {},
        onDoneClicked = {},
        spaces = emptyList(),
        onSelectSpaceClicked = {}
    )
}

@Preview
@Composable
fun AddToAnytypeScreenNotePreview() {
    AddToAnytypeScreen(
        data = SharingData.Raw("The Work of Art in the Age of its Technological Reproducibility"),
        onCancelClicked = {},
        onDoneClicked = {},
        spaces = emptyList(),
        onSelectSpaceClicked = {}
    )
}

@Composable
fun AddToAnytypeScreen(
    spaces: List<SpaceView>,
    data: SharingData,
    onCancelClicked: () -> Unit,
    onDoneClicked: (SaveAsOption) -> Unit,
    onSelectSpaceClicked: (SpaceView) -> Unit
) {
    var isSaveAsMenuExpanded by remember { mutableStateOf(false) }
    val items = when (data) {
        is SharingData.Url -> listOf(SAVE_AS_NOTE, SAVE_AS_BOOKMARK)
        is SharingData.Image -> listOf(SAVE_AS_IMAGE)
        is SharingData.File -> listOf(SAVE_AS_FILE)
        is SharingData.Images -> listOf(SAVE_AS_IMAGES)
        is SharingData.Files -> listOf(SAVE_AS_FILES)
        else -> listOf(SAVE_AS_NOTE)
    }
    var selectedIndex by remember {
        mutableStateOf(
            when(data) {
                is SharingData.Url -> SAVE_AS_BOOKMARK
                is SharingData.Image -> SAVE_AS_IMAGE
                is SharingData.File -> SAVE_AS_FILE
                is SharingData.Images -> SAVE_AS_IMAGES
                is SharingData.Files -> SAVE_AS_FILES
                else -> SAVE_AS_NOTE
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Header()
        DataSection(data)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .noRippleClickable { isSaveAsMenuExpanded = !isSaveAsMenuExpanded }
        ) {
            Text(
                text = stringResource(R.string.sharing_menu_save_as_section_name),
                modifier = Modifier
                    .padding(top = 14.dp, start = 20.dp),
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary)
            )

            Text(
                text = when (selectedIndex) {
                    SAVE_AS_BOOKMARK -> stringResource(id = R.string.sharing_menu_save_as_bookmark_option)
                    SAVE_AS_IMAGE -> stringResource(id = R.string.sharing_menu_save_as_image_option)
                    SAVE_AS_FILE -> stringResource(id = R.string.sharing_menu_save_as_file_option)
                    SAVE_AS_IMAGES -> stringResource(id = R.string.sharing_menu_save_as_images_option)
                    SAVE_AS_FILES -> stringResource(id = R.string.sharing_menu_save_as_files_option)
                    else -> stringResource(id = R.string.sharing_menu_save_as_note_option)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 14.dp, start = 20.dp),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
            if (items.size > 1) {
                DropdownMenu(
                    expanded = isSaveAsMenuExpanded,
                    onDismissRequest = { isSaveAsMenuExpanded = false },
                    modifier = Modifier.background(
                        color = colorResource(id = R.color.background_secondary)
                    )
                ) {
                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            onClick = {
                                selectedIndex = index
                                isSaveAsMenuExpanded = false
                            }
                        ) {
                            when(s) {
                                SAVE_AS_BOOKMARK -> {
                                    Text(
                                        text = stringResource(id = R.string.sharing_menu_save_as_bookmark_option),
                                        style = BodyRegular,
                                        color = colorResource(id = R.color.text_primary)
                                    )
                                }
                                SAVE_AS_NOTE -> {
                                    Text(
                                        text = stringResource(id = R.string.sharing_menu_save_as_note_option),
                                        style = BodyRegular,
                                        color = colorResource(id = R.color.text_primary)
                                    )
                                }
                                else -> {
                                    // Draw nothing
                                }
                            }
                        }
                        if (index != items.lastIndex) {
                            Divider(
                                thickness = 0.5.dp,
                                color = colorResource(id = R.color.shape_primary)
                            )
                        }
                    }
                }
            }
        }
        val selected = spaces.firstOrNull { it.isSelected }
        if (selected != null) {
            CurrentSpaceSection(
                name = selected.obj.name.orEmpty(),
                spaces = spaces,
                onSelectSpaceClicked = onSelectSpaceClicked,
                icon = selected.icon
            )
        } else {
            CurrentSpaceSection(
                name = stringResource(id = R.string.unknown),
                spaces = spaces,
                onSelectSpaceClicked = onSelectSpaceClicked
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Buttons(onCancelClicked, onDoneClicked, selectedIndex)
    }
}

@Composable
private fun DataSection(data: SharingData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Text(
            text = stringResource(R.string.sharing_menu_data),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        )
        Text(
            text = data.data,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(
                top = 30.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 10.dp
            ),
            maxLines = 5
        )
    }
}

@Composable
private fun Buttons(
    onCancelClicked: () -> Unit,
    onDoneClicked: (SaveAsOption) -> Unit,
    selectedIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ButtonSecondary(
            onClick = onCancelClicked,
            size = ButtonSize.Large,
            text = stringResource(id = R.string.cancel),
            modifier = Modifier.weight(1.0f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        ButtonPrimary(
            onClick = { onDoneClicked(selectedIndex) },
            size = ButtonSize.Large,
            text = stringResource(id = R.string.done),
            modifier = Modifier.weight(1.0f)
        )
    }
}

@Composable
private fun CurrentSpaceSection(
    icon: SpaceIconView? = null,
    name: String,
    spaces: List<SpaceView>,
    onSelectSpaceClicked: (SpaceView) -> Unit
) {
    var isSpaceSelectMenuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .noRippleClickable {
                isSpaceSelectMenuExpanded = true
            }
    ) {
        Text(
            text = stringResource(R.string.space),
            modifier = Modifier
                .padding(top = 14.dp, start = 20.dp),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary)
        )
        val hasIcon = icon is SpaceIconView.Gradient || icon is SpaceIconView.Image
        if (icon != null && hasIcon) {
            SmallSpaceIcon(
                icon = icon,
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        bottom = 17.dp
                    )
                    .align(Alignment.BottomStart)
            )
        }
        Text(
            text = name,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    bottom = 14.dp,
                    start = if (hasIcon) 44.dp else 20.dp
                ),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        DropdownMenu(
            expanded = isSpaceSelectMenuExpanded,
            onDismissRequest = { isSpaceSelectMenuExpanded = false },
            modifier = Modifier.background(
                color = colorResource(id = R.color.background_secondary)
            )
        ) {
            spaces.forEachIndexed { index, view ->
                DropdownMenuItem(
                    onClick = {
                        onSelectSpaceClicked(view)
                        isSpaceSelectMenuExpanded = false
                    }
                ) {
                    Text(
                        text = view.obj.name.orEmpty(),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
                if (index != spaces.lastIndex) {
                    Divider(
                        thickness = 0.5.dp,
                        color = colorResource(id = R.color.shape_primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.sharing_menu_add_to_anytype_header_title),
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.Center),
            style = TitleInter15
        )
    }
}


@Composable
private fun SmallSpaceIcon(
    icon: SpaceIconView,
    modifier: Modifier
) {
   val size = 18.dp
    when (icon) {
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = com.anytypeio.anytype.ui_settings.R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        is SpaceIconView.Gradient -> {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color(icon.from.toColorInt()),
                    Color(icon.to.toColorInt())
                )
            )
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(gradient)
            )
        }
        else -> {
            // Draw nothing.
        }
    }
}

const val SAVE_AS_NOTE = 0
const val SAVE_AS_BOOKMARK = 1
const val SAVE_AS_IMAGE = 2
const val SAVE_AS_FILE = 3
const val SAVE_AS_IMAGES = 4
const val SAVE_AS_FILES = 5
typealias SaveAsOption = Int

sealed class SharingData {
    abstract val data: String
    data class Url(val url: String) : SharingData() {
        override val data: String
            get() = url
    }
    data class Raw(val raw: String) : SharingData() {
        override val data: String
            get() = raw
    }
    data class Image(val uri: String) : SharingData() {
        override val data: String
            get() = uri
    }

    data class Images(val uris: List<String>): SharingData() {
        override val data: String
            get() = uris.toString()
    }

    data class Files(val uris: List<String>): SharingData() {
        override val data: String
            get() = uris.toString()
    }

    data class File(val uri: String): SharingData() {
        override val data: String
            get() = uri
    }
}
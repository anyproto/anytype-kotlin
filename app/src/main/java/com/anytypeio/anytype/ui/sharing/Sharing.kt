package com.anytypeio.anytype.ui.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.TitleInter15

@Preview
@Composable
fun AddToAnytypeScreenUrlPreview() {
    AddToAnytypeScreen(
        data = SharingData.Url("https://en.wikipedia.org/wiki/Walter_Benjamin"),
        onCancelClicked = {},
        onDoneClicked = {}
    )
}

@Preview
@Composable
fun AddToAnytypeScreenNotePreview() {
    AddToAnytypeScreen(
        data = SharingData.Raw("The Work of Art in the Age of its Technological Reproducibility"),
        onCancelClicked = {},
        onDoneClicked = {}
    )
}

@Composable
fun AddToAnytypeScreen(
    data: SharingData,
    onCancelClicked: () -> Unit,
    onDoneClicked: (SaveAsOption) -> Unit
) {
    var isSaveAsMenuExpanded by remember { mutableStateOf(false) }
    val items = listOf(SAVE_AS_NOTE, SAVE_AS_BOOKMARK)
    var selectedIndex by remember {
        mutableStateOf(
            when(data) {
                is SharingData.Url -> SAVE_AS_BOOKMARK
                else -> SAVE_AS_NOTE
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Header()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Text(
                text = stringResource(R.string.sharing_menu_save_as_section_name),
                modifier = Modifier
                    .padding(top = 14.dp, start = 20.dp)
                    .clickable {
                        isSaveAsMenuExpanded = !isSaveAsMenuExpanded
                    },
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary)
            )

            Text(
                text = if (selectedIndex == SAVE_AS_BOOKMARK)
                    stringResource(id = R.string.sharing_menu_save_as_bookmark_option)
                else
                    stringResource(id = R.string.sharing_menu_save_as_note_option),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 14.dp, start = 20.dp)
                    .clickable {
                        isSaveAsMenuExpanded = !isSaveAsMenuExpanded
                    },
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
            DropdownMenu(
                expanded = isSaveAsMenuExpanded,
                onDismissRequest = { isSaveAsMenuExpanded = false },
                modifier = Modifier.background(
                    color = colorResource(id = R.color.background_secondary)
                )
            ) {
                items.forEachIndexed { index, s ->
                    DropdownMenuItem(onClick = {
                        selectedIndex = index
                        isSaveAsMenuExpanded = false
                    }) {
                        when(s) {
                            SAVE_AS_BOOKMARK -> {
                                Text(
                                    text = stringResource(id = R.string.sharing_menu_save_as_bookmark_option),
                                    style = BodyRegular
                                )
                            }
                            SAVE_AS_NOTE -> {
                                Text(
                                    text = stringResource(id = R.string.sharing_menu_save_as_note_option),
                                    style = BodyRegular
                                )
                            }
                            else -> {
                                // Draw nothing
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Text(
                text = "Space",
                modifier = Modifier
                    .padding(top = 14.dp, start = 20.dp)
                    .clickable {
                        isSaveAsMenuExpanded = !isSaveAsMenuExpanded
                    },
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary)
            )
            Text(
                text = "Main",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 14.dp, start = 20.dp)
                    .clickable {
                        isSaveAsMenuExpanded = !isSaveAsMenuExpanded
                    },
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
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
                text = "Cancel",
                modifier = Modifier.weight(1.0f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ButtonPrimary(
                onClick = { onDoneClicked(selectedIndex) },
                size = ButtonSize.Large,
                text = "Done",
                modifier = Modifier.weight(1.0f)
            )
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
            text = "Add to Anytype",
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.Center),
            style = TitleInter15
        )
    }
}

const val SAVE_AS_NOTE = 0
const val SAVE_AS_BOOKMARK = 1
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
}
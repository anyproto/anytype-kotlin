package com.anytypeio.anytype.ui.sharing

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.ui.MultipleEventCutter
import com.anytypeio.anytype.core_utils.ui.get
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel.SpaceView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun AddToAnytypeScreenUrlPreview() {
    AddToAnytypeScreen(
        data = SharingData.Url("https://en.wikipedia.org/wiki/Walter_Benjamin"),
        onCancelClicked = {},
        onAddClicked = {},
        spaces = listOf(
            SpaceView(
                obj = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
                isSelected = true,
                icon = SpaceIconView.DataSpace.Placeholder()
            )
        ),
        onSelectSpaceClicked = {},
        onOpenClicked = {},
        content = "https://en.wikipedia.org/wiki/Walter_Benjamin",
        progressState = AddToAnytypeViewModel.ProgressState.Done(""),
        //progressState = AddToAnytypeViewModel.ProgressState.Error(" I understand that contributing to this repository will require me to agree with the CLA  I understand that contributing to this repository will require me to agree with the CLA\n")
        //progressState = AddToAnytypeViewModel.ProgressState.Progress(processId = "dasda", progress = 0.8f)
    )
}

@Preview
@Composable
fun AddToAnytypeScreenNotePreview() {
    AddToAnytypeScreen(
        data = SharingData.Text("The Work of Art in the Age of its Technological Reproducibility"),
        onCancelClicked = {},
        onAddClicked = {},
        spaces = listOf(
            SpaceView(
                obj = ObjectWrapper.SpaceView(map = mapOf()),
                isSelected = false,
                icon = SpaceIconView.DataSpace.Placeholder()
            )
        ),
        onSelectSpaceClicked = {},
        content = "",
        progressState = AddToAnytypeViewModel.ProgressState.Progress(
            processId = "dasda",
            progress = 0.8f,
            wrapperObjId = ""
        ),
        onOpenClicked = {},
    )
}

@Composable
fun AddToAnytypeScreen(
    content: String,
    spaces: List<SpaceView>,
    data: SharingData,
    progressState: AddToAnytypeViewModel.ProgressState,
    onCancelClicked: () -> Unit,
    onAddClicked: (SaveAsOption) -> Unit,
    onSelectSpaceClicked: (SpaceView) -> Unit,
    onOpenClicked: (Id) -> Unit
) {
    var isSaveAsMenuExpanded by remember { mutableStateOf(false) }
    val items = when (data) {
        is SharingData.Url -> listOf(SAVE_AS_NOTE, SAVE_AS_BOOKMARK)
        is SharingData.Image -> listOf(SAVE_AS_IMAGE)
        is SharingData.File -> listOf(SAVE_AS_FILE)
        is SharingData.Images -> listOf(SAVE_AS_IMAGES)
        is SharingData.Files -> listOf(SAVE_AS_FILES)
        is SharingData.Text -> listOf(SAVE_AS_NOTE)
        is SharingData.Videos -> listOf(SAVE_AS_VIDEOS)
    }
    var selectedIndex by remember {
        mutableStateOf(
            when (data) {
                is SharingData.Url -> SAVE_AS_BOOKMARK
                is SharingData.Image -> SAVE_AS_IMAGE
                is SharingData.File -> SAVE_AS_FILE
                is SharingData.Images -> SAVE_AS_IMAGES
                is SharingData.Files -> SAVE_AS_FILES
                is SharingData.Text -> SAVE_AS_NOTE
                is SharingData.Videos -> SAVE_AS_VIDEOS
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        val throttler = remember {
            MultipleEventCutter.Companion.get(interval = DROPDOWN_MENU_VISIBILITY_WINDOW_INTERVAL)
        }
        Header()
        DataSection(content)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable {
                    throttler.processEvent {
                        isSaveAsMenuExpanded = !isSaveAsMenuExpanded
                    }
                }
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
                    SAVE_AS_VIDEOS -> stringResource(id = R.string.sharing_menu_save_as_videos_option)
                    else -> stringResource(id = R.string.sharing_menu_save_as_note_option)
                },
                modifier = Modifier
                    .padding(top = 6.dp, start = 20.dp, bottom = 14.dp),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
            if (items.size > 1) {
                DropdownMenu(
                    expanded = isSaveAsMenuExpanded,
                    onDismissRequest = {
                        throttler.processEvent {
                            isSaveAsMenuExpanded = false
                        }
                    },
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
                            when (s) {
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
        com.anytypeio.anytype.core_ui.foundation.Divider(paddingEnd = 20.dp, paddingStart = 20.dp)
        val selected = spaces.firstOrNull { it.isSelected }
        if (selected != null) {
            CurrentSpaceSection(
                name = selected.obj.name.orEmpty().ifEmpty {
                    stringResource(R.string.untitled)
                },
                spaces = spaces,
                onSelectSpaceClicked = onSelectSpaceClicked,
                icon = selected.icon
            )
        } else {
            CurrentSpaceSection(
                name = stringResource(id = R.string.three_dots_text_placeholder),
                spaces = spaces,
                onSelectSpaceClicked = onSelectSpaceClicked
            )
        }
        DefaultLinearProgressIndicator(progressState = progressState)
        when (progressState) {
            is AddToAnytypeViewModel.ProgressState.Done -> {
                ButtonsDone(
                    progressState = progressState,
                    onCancelClicked = onCancelClicked,
                    onOpenClicked = onOpenClicked
                )
            }
            is AddToAnytypeViewModel.ProgressState.Error -> {
                Buttons(
                    onCancelClicked = onCancelClicked,
                    selectedIndex = selectedIndex,
                    progressState = progressState,
                    onAddClicked = onAddClicked
                )
            }
            AddToAnytypeViewModel.ProgressState.Init -> {
                Buttons(
                    onCancelClicked = onCancelClicked,
                    selectedIndex = selectedIndex,
                    progressState = progressState,
                    onAddClicked = onAddClicked
                )
            }
            is AddToAnytypeViewModel.ProgressState.Progress -> {
                ButtonsProgress(onCancelClicked = onCancelClicked)
            }
        }
    }
}

@Composable
private fun DefaultLinearProgressIndicator(progressState: AddToAnytypeViewModel.ProgressState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        val visible = progressState is AddToAnytypeViewModel.ProgressState.Progress
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            if (progressState is AddToAnytypeViewModel.ProgressState.Progress) {
                Indicator(progress = progressState.progress)
            }
        }
        val doneVisibility = progressState is AddToAnytypeViewModel.ProgressState.Done
        AnimatedVisibility(
            visible = doneVisibility,
            modifier = Modifier,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Text(
                text = stringResource(id = R.string.sharing_menu_add_to_anytype_success),
                style = Caption1Medium,
                color = colorResource(id = R.color.palette_system_green),
                modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
            )
        }
        val errorVisible = progressState is AddToAnytypeViewModel.ProgressState.Error
        AnimatedVisibility(
            visible = errorVisible,
            modifier = Modifier,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            if (progressState is AddToAnytypeViewModel.ProgressState.Error) {
                Text(
                    text = stringResource(
                        id = R.string.sharing_menu_add_to_anytype_error,
                        progressState.error
                    ),
                    style = Caption1Medium,
                    maxLines = 2,
                    color = colorResource(id = R.color.palette_dark_red),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun Indicator(progress: Float) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = ""
    ).value
    LinearProgressIndicator(
        progress = animatedProgress,
        color = colorResource(id = R.color.text_primary),
        modifier = Modifier
            .height(6.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        backgroundColor = colorResource(id = R.color.shape_tertiary),
        strokeCap = StrokeCap.Round
    )
}

@Composable
private fun DataSection(content: String) {
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
            text = content,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier
                .padding(
                    top = 30.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 10.dp
                )
                .verticalScroll(rememberScrollState()),
            maxLines = 5
        )
    }
}

@Composable
private fun ButtonsDone(
    progressState: AddToAnytypeViewModel.ProgressState.Done,
    onCancelClicked: () -> Unit,
    onOpenClicked: (Id) -> Unit
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
            onClick = {
                onOpenClicked(progressState.wrapperObjId)
            },
            size = ButtonSize.Large,
            text = stringResource(id = R.string.sharing_menu_btn_open),
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
private fun ButtonsProgress(
    onCancelClicked: () -> Unit,
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
        ButtonPrimaryLoading(
            size = ButtonSize.Large,
            text = stringResource(id = R.string.sharing_menu_btn_add),
            modifierBox = Modifier.weight(1.0f),
            modifierButton = Modifier.fillMaxWidth(),
            loading = true
        )
    }
}

@Composable
private fun Buttons(
    onCancelClicked: () -> Unit,
    onAddClicked: (SaveAsOption) -> Unit,
    selectedIndex: Int,
    progressState: AddToAnytypeViewModel.ProgressState
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
        ButtonPrimaryLoading(
            onClick = { onAddClicked(selectedIndex) },
            size = ButtonSize.Large,
            text = stringResource(id = R.string.sharing_menu_btn_add),
            modifierBox = Modifier.weight(1.0f),
            modifierButton = Modifier.fillMaxWidth(),
            loading = progressState is AddToAnytypeViewModel.ProgressState.Progress
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
    val throttler = remember {
        MultipleEventCutter.Companion.get(interval = DROPDOWN_MENU_VISIBILITY_WINDOW_INTERVAL)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .noRippleClickable {
                throttler.processEvent {
                    isSpaceSelectMenuExpanded = true
                }
            }
    ) {
        Text(
            text = stringResource(R.string.space),
            modifier = Modifier
                .padding(top = 14.dp, start = 20.dp),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasIcon = icon is SpaceIconView.DataSpace || icon is SpaceIconView.ChatSpace
            if (icon != null && hasIcon) {
                SpaceIconView(
                    icon = icon,
                    modifier = Modifier.padding(end = 8.dp),
                    mainSize = 20.dp,
                    onSpaceIconClick = {
                        // Do nothing.
                    }
                )
            }
            Text(
                text = name,
                modifier = Modifier,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        DropdownMenu(
            expanded = isSpaceSelectMenuExpanded,
            onDismissRequest = {
                throttler.processEvent {
                    isSpaceSelectMenuExpanded = false
                }
            },
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
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    com.anytypeio.anytype.core_ui.foundation.Divider(paddingEnd = 20.dp, paddingStart = 20.dp)
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
            style = Title2
        )
    }
}

const val SAVE_AS_NOTE = 0
const val SAVE_AS_BOOKMARK = 1
const val SAVE_AS_IMAGE = 2
const val SAVE_AS_FILE = 3
const val SAVE_AS_IMAGES = 4
const val SAVE_AS_FILES = 5
const val SAVE_AS_VIDEOS = 6
typealias SaveAsOption = Int

sealed class SharingData {
    abstract val data: String

    data class Url(val url: String) : SharingData() {
        override val data: String
            get() = url
    }

    data class Text(val raw: String) : SharingData() {
        override val data: String
            get() = raw
    }

    data class Image(val uri: String) : SharingData() {
        override val data: String
            get() = uri
    }

    data class Images(val uris: List<String>) : SharingData() {
        override val data: String
            get() = uris.toString()
    }

    data class Files(val uris: List<String>) : SharingData() {
        override val data: String
            get() = uris.toString()
    }

    data class File(val uri: String) : SharingData() {
        override val data: String
            get() = uri
    }

    data class Videos(val uris: List<String>) : SharingData() {
        override val data: String
            get() = uris.toString()
    }
}

const val DROPDOWN_MENU_VISIBILITY_WINDOW_INTERVAL = 150L
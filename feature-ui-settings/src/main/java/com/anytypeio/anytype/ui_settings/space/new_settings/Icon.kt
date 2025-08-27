package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.ui_settings.R
import timber.log.Timber

@Composable
fun NewSpaceIcon(
    icon: SpaceIconView,
    modifier: Modifier = Modifier,
    uiEvent: (UiEvent) -> Unit,
    isEditEnabled: Boolean
) {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    val path = uri.parseImagePath(context)
                    uiEvent(UiEvent.OnSpaceImagePicked(path))
                }.onFailure {
                    context.toast(context.getString(R.string.error_while_loading_picture))
                }
            } else {
                Timber.w("Uri was null after picking image")
            }
        }
    )

    val isSpaceIconMenuExpanded = remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpaceIconView(
            modifier = Modifier.size(112.dp),
            icon = icon,
            onSpaceIconClick = {
                if (isEditEnabled) {
                    isSpaceIconMenuExpanded.value = !isSpaceIconMenuExpanded.value
                }
            }
        )
        if (isEditEnabled) {
            Box {
                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp))
                ) {
                    DropdownMenu(
                        modifier = Modifier
                            .background(
                                shape = RoundedCornerShape(10.dp),
                                color = colorResource(id = R.color.background_secondary)
                            ),
                        expanded = isSpaceIconMenuExpanded.value,
                        offset = DpOffset(x = 0.dp, y = 6.dp),
                        onDismissRequest = {
                            isSpaceIconMenuExpanded.value = false
                        }
                    ) {
                        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                            DropdownMenuItem(
                                onClick = {
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                    isSpaceIconMenuExpanded.value = false
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.space_settings_apply_upload_image),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary)
                                )
                            }
                        }
                        if (icon is SpaceIconView.DataSpace.Image) {
                            Divider(
                                thickness = 0.5.dp,
                                color = colorResource(id = R.color.shape_primary)
                            )
                            DropdownMenuItem(
                                onClick = {
                                    uiEvent(UiEvent.IconMenu.OnRemoveIconClicked)
                                    isSpaceIconMenuExpanded.value = false
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.remove_image),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
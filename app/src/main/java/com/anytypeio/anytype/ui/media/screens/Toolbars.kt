package com.anytypeio.anytype.ui.media.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.R
import com.anytypeio.anytype.localization.R as LocalizationR

@Composable
fun MediaTopBar(
    modifier: Modifier = Modifier,
    title: String = "",
    isArchived: Boolean = false,
    onBackClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        // Back arrow (left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(28.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(7.dp)
                )
                .noRippleClickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(com.anytypeio.anytype.core_ui.R.drawable.ic_back_24),
                contentDescription = "Back",
                colorFilter = ColorFilter.tint(colorResource(R.color.control_secondary))
            )
        }

        // Title (center)
        if (title.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = title,
                        style = PreviewTitle2Regular,
                        color = colorResource(R.color.text_white),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Three-dots menu (right)
        if (!isArchived) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .noRippleClickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(com.anytypeio.anytype.core_ui.R.drawable.ic_action_more),
                        contentDescription = "Menu",
                        colorFilter = ColorFilter.tint(colorResource(R.color.control_secondary))
                    )
                }

                DropdownMenu(
                    modifier = Modifier.width(254.dp),
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = colorResource(R.color.background_secondary),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp
                ) {
                    DropdownMenuItem(
                        onClick = {
                            menuExpanded = false
                            onDownloadClick()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(R.color.text_primary),
                                    text = stringResource(LocalizationR.string.download)
                                )
                                Image(
                                    painter = painterResource(R.drawable.ic_object_action_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    Divider(
                        thickness = 0.5.dp,
                        color = colorResource(R.color.shape_primary)
                    )
                    DropdownMenuItem(
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(R.color.palette_system_red),
                                    text = stringResource(LocalizationR.string.delete)
                                )
                                Image(
                                    painter = painterResource(R.drawable.icon_delete_red),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(R.color.palette_system_red)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun MediaTopBarPreview() {
    MediaTopBar(
        title = "photo_2024.jpg"
    )
}

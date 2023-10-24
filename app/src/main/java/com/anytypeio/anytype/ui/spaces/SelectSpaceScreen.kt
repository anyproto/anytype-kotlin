package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SelectSpaceView
import com.anytypeio.anytype.presentation.spaces.WorkspaceView
import com.anytypeio.anytype.ui_settings.main.SpaceImageBlock

@Composable
fun SelectSpaceScreen(
    spaces: List<SelectSpaceView>,
    onAddClicked: () -> Unit,
    onSpaceClicked: (WorkspaceView) -> Unit,
    onSpaceSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Column() {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 3),
            modifier = Modifier.padding(
                bottom = 16.dp
            )
        ) {
            spaces.forEach { item ->
                when (item) {
                    is SelectSpaceView.Profile -> {
                        item(
                            span = {
                                GridItemSpan(MAX_SPAN_COUNT)
                            }
                        ) {
                            SelectSpaceProfileHeader(
                                profile = item,
                                onSpaceSettingsClicked = onSpaceSettingsClicked,
                                onProfileClicked = onProfileClicked
                            )
                        }
                    }
                    is SelectSpaceView.Space -> {
                        item(
                            span = {
                                GridItemSpan(1)
                            }
                        ) {
                            SelectSpaceSpaceItem(item, onSpaceClicked)
                        }
                    }
                    is SelectSpaceView.Create -> {
                        item(
                            span = {
                                GridItemSpan(1)
                            }
                        ) {
                            SelectSpaceCreateButton(onAddClicked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectSpaceCreateButton(onAddClicked: () -> Unit) {
    Column(modifier = Modifier) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FFFFFF))
                .clickable { onAddClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32_white),
                contentDescription = "Plus icon",
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Spacer(modifier = Modifier.height(28.dp))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectSpaceSpaceItem(
    item: SelectSpaceView.Space,
    onSpaceClicked: (WorkspaceView) -> Unit
) {
    Column(modifier = Modifier) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (item.view.isSelected)
                        Modifier.border(
                            width = if (item.view.isSelected) 2.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                    else
                        Modifier
                )
        ) {
            SpaceImageBlock(
                icon = item.view.icon,
                onSpaceIconClick = { onSpaceClicked(item.view) },
                gradientBackground = colorResource(id = R.color.default_gradient_background)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier.fillMaxSize(),
            text = item.view.name.orEmpty(),
            textAlign = TextAlign.Center,
            style = Caption1Medium,
            color = Color.White,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SelectSpaceProfileHeader(
    profile: SelectSpaceView.Profile,
    onSpaceSettingsClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(bottom = 6.dp)
    ) {
        SelectSpaceProfileIcon(
            modifier = Modifier
                .padding(start = 30.dp)
                .align(Alignment.CenterStart),
            name = profile.name,
            icon = profile.icon,
            onProfileIconClick = onProfileClicked
        )
        Text(
            text = profile.name.orNull() ?: stringResource(id = R.string.untitled),
            style = HeadlineHeading,
            color = Color.White,
            modifier = Modifier
                .align(
                    Alignment.CenterStart
                )
                .padding(
                    start = 74.dp,
                    end = 74.dp
                )
                .clickable { onProfileClicked() }
        )
        Box(
            modifier = Modifier
                .padding(end = 30.dp)
                .size(32.dp)
                .align(Alignment.CenterEnd)
                .clickable { onSpaceSettingsClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_space_settings),
                contentDescription = "Space settings icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun SelectSpaceProfileIcon(
    modifier: Modifier,
    name: String,
    icon: ProfileIconView,
    onProfileIconClick: () -> Unit
) {
    when (icon) {
        is ProfileIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .noRippleClickable {
                        onProfileIconClick.invoke()
                    }
            )
        }
        is ProfileIconView.Gradient -> {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color(icon.from.toColorInt()),
                    Color(icon.to.toColorInt())
                )
            )
            Box(modifier = modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(gradient)
                .noRippleClickable {
                    onProfileIconClick.invoke()
                })
        }
        else -> {
            val nameFirstChar = if (name.isEmpty()) {
                stringResource(id = R.string.account_default_name)
            } else {
                name.first().uppercaseChar().toString()
            }
            Box(
                modifier = modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorResource(id = R.color.shape_primary))
                    .noRippleClickable {
                        onProfileIconClick.invoke()
                    }
            ) {
                Text(
                    text = nameFirstChar,
                    style = MaterialTheme.typography.h3.copy(
                        color = colorResource(id = R.color.text_white),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

const val MAX_SPAN_COUNT = 3
package com.anytypeio.anytype.gallery_experience.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationSpacesState
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.gallery_experience.models.SpaceView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryInstallationSpacesScreen(
    state: GalleryInstallationSpacesState,
    onNewSpaceClick: () -> Unit,
    onSpaceClick: (SpaceView) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background_secondary),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
                if (state.isNewButtonVisible) {
                    NewSpaceItem(onNewSpaceClick = onNewSpaceClick)
                }
                state.spaces.forEach { space ->
                    SpaceItem(space = space, onSpaceClick = onSpaceClick)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        dragHandle = null
    )
}

@Composable
private fun NewSpaceItem(onNewSpaceClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { onNewSpaceClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(48.dp)
                .background(
                    shape = RoundedCornerShape(8.dp),
                    color = colorResource(id = R.color.background_highlighted).copy(alpha = 0.04f)
                )
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(8.dp)
                ),
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = "Install to new space",
            contentScale = ContentScale.Inside
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 20.dp),
            text = stringResource(id = R.string.gallery_experience_install_new),
            style = Title3,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun SpaceItem(space: SpaceView, onSpaceClick: (SpaceView) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { onSpaceClick(space) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIcon(
            icon = space.icon,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 20.dp),
            text = space.obj.name.orEmpty(),
            style = Title3,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun SpaceIcon(
    icon: SpaceIconView,
    modifier: Modifier
) {
    when (icon) {
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
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
                    .clip(CircleShape)
                    .background(gradient)
            )
        }

        else -> {
            // Draw nothing.
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GallerySpacesScreenPreview() {
    GalleryInstallationSpacesScreen(
        state = GalleryInstallationSpacesState(
            listOf(
                SpaceView(
                    obj = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
                    icon = SpaceIconView.Placeholder
                )
            ),
            isNewButtonVisible = true
        ),
        onNewSpaceClick = {},
        onSpaceClick = {},
        onDismiss = {}
    )
}
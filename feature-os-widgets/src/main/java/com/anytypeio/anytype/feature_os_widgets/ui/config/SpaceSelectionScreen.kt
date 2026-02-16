package com.anytypeio.anytype.feature_os_widgets.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceSelectionScreen(
    spaces: List<ObjectWrapper.SpaceView>,
    urlBuilder: UrlBuilder,
    onSpaceSelected: (ObjectWrapper.SpaceView) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.select_space),
                        style = BodyBold,
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_24),
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.background_primary)
                )
            )
        },
        containerColor = colorResource(id = R.color.background_primary)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (spaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_spaces_available),
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = spaces,
                        key = { it.id }
                    ) { space ->
                        SpaceGridItem(
                            space = space,
                            urlBuilder = urlBuilder,
                            onClick = { onSpaceSelected(space) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceGridItem(
    space: ObjectWrapper.SpaceView,
    urlBuilder: UrlBuilder,
    onClick: () -> Unit
) {
    val icon = space.toSpaceIconView(urlBuilder)

    Column(
        modifier = Modifier.noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(86.dp)
                .width(92.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SpaceIconView(
                icon = icon,
                mainSize = 80.dp,
                onSpaceIconClick = onClick
            )
        }

        // Space Name
        Text(
            text = space.name.orEmpty().ifEmpty { stringResource(R.string.untitled) },
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 4.dp)
        )
    }
}

/**
 * Converts ObjectWrapper.SpaceView to SpaceIconView for rendering.
 */
fun ObjectWrapper.SpaceView.toSpaceIconView(urlBuilder: UrlBuilder): SpaceIconView {
    val isChat = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
    val color = iconOption?.toInt()?.let { SystemColor.color(it) } ?: SystemColor.SKY
    val imageUrl = iconImage?.takeIf { it.isNotEmpty() }?.let { urlBuilder.medium(it) }

    return if (imageUrl != null) {
        if (isChat) {
            SpaceIconView.ChatSpace.Image(url = imageUrl, color = color)
        } else {
            SpaceIconView.DataSpace.Image(url = imageUrl, color = color)
        }
    } else {
        if (isChat) {
            SpaceIconView.ChatSpace.Placeholder(color = color, name = name.orEmpty())
        } else {
            SpaceIconView.DataSpace.Placeholder(color = color, name = name.orEmpty())
        }
    }
}

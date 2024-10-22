package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.anytypeio.anytype.BuildConfig.USE_EDGE_TO_EDGE
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.util.DraggableItem
import com.anytypeio.anytype.core_ui.foundation.util.dragContainer
import com.anytypeio.anytype.core_ui.foundation.util.rememberDragDropState
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultViewModel.VaultSpaceView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.ui.widgets.types.gradient


@Composable
fun VaultScreen(
    spaces: List<VaultSpaceView>,
    onSpaceClicked: (VaultSpaceView) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onOrderChanged: (List<Id>) -> Unit
) {
    var spaceList by remember {
        mutableStateOf<List<VaultSpaceView>>(spaces)
    }

    spaceList = spaces

    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        onDragEnd = {
            onOrderChanged(
                spaceList.map { it.space.id }
            )
        },
        onMove = { fromIndex, toIndex ->
            spaceList = spaceList.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_primary)
            )
            .then(
                if (USE_EDGE_TO_EDGE && SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier.windowInsetsPadding(WindowInsets.systemBars)
                else
                    Modifier
            )
    ) {

       VaultScreenToolbar(
           onPlusClicked = onCreateSpaceClicked,
           onSettingsClicked = onSettingsClicked,
           spaceCountLimitReached = spaces.size >= SelectSpaceViewModel.MAX_SPACE_COUNT
       )

       LazyColumn(
           modifier = Modifier
               .fillMaxSize()
               .padding(top = 48.dp)
               .dragContainer(dragDropState)
           ,
           state = lazyListState,
           verticalArrangement = Arrangement.spacedBy(8.dp)
       ) {
           itemsIndexed(
               items = spaceList,
               key = { _, item ->
                   item.space.id
               }
           ) { idx, item ->
               if (idx == 0) {
                   Spacer(modifier = Modifier.height(4.dp))
               }
               DraggableItem(dragDropState = dragDropState, index = idx) {
                   VaultSpaceCard(
                       title = item.space.name.orEmpty(),
                       subtitle = when (item.space.spaceAccessType) {
                           SpaceAccessType.PRIVATE -> stringResource(id = R.string.space_type_private_space)
                           SpaceAccessType.DEFAULT -> stringResource(id = R.string.space_type_default_space)
                           SpaceAccessType.SHARED -> stringResource(id = R.string.space_type_shared_space)
                           else -> EMPTY_STRING_VALUE
                       },
                       wallpaper = item.wallpaper,
                       onCardClicked = { onSpaceClicked(item) },
                       icon = item.icon
                   )
               }
               if (idx == spaces.lastIndex && spaces.size < SelectSpaceViewModel.MAX_SPACE_COUNT) {
                   VaultSpaceAddCard(
                       onCreateSpaceClicked = onCreateSpaceClicked
                   )
                   Spacer(modifier = Modifier.height(40.dp))
               }
           }
           if (spaceList.isEmpty()) {
               item {
                   VaultSpaceAddCard(
                       onCreateSpaceClicked = onCreateSpaceClicked
                   )
                   Spacer(modifier = Modifier.height(40.dp))
               }
           }
       }
    }
}


@Composable
fun VaultScreenToolbar(
    spaceCountLimitReached: Boolean = false,
    onPlusClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = stringResource(R.string.vault_my_spaces),
            style = Title1,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.align(Alignment.Center)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_vault_settings),
            contentDescription = "Settings icon",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .noRippleClickable {
                    onSettingsClicked()
                }
        )
        if (!spaceCountLimitReached) {
            Image(
                painter = painterResource(id = R.drawable.ic_vault_top_toolbar_plus),
                contentDescription = "Plus button",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .noRippleClickable {
                        onPlusClicked()
                    }
            )
        }
    }
}

@Composable
fun VaultSpaceCard(
    title: String,
    subtitle: String,
    onCardClicked: () -> Unit,
    icon: SpaceIconView,
    wallpaper: Wallpaper
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .then(
                when (wallpaper) {
                    is Wallpaper.Color -> {
                        val color = WallpaperColor.entries.find {
                            it.code == wallpaper.code
                        }
                        if (color != null) {
                            Modifier.background(
                                color = Color(color.hex.toColorInt()).copy(0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                        } else {
                            Modifier
                        }
                    }

                    is Wallpaper.Gradient -> {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = gradient(
                                    gradient = wallpaper.code,
                                    alpha = 0.3f
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    is Wallpaper.Default -> {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = gradient(
                                    gradient = CoverGradient.SKY,
                                    alpha = 0.3f
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    else -> Modifier
                }
            )
            .clickable {
                onCardClicked()
            }
    ) {
        SpaceIconView(
            icon = icon,
            onSpaceIconClick = {
                onCardClicked()
            },
            mainSize = 64.dp,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 24.dp,
                    start = 96.dp,
                    end = 16.dp
                )
        ) {
            Text(
                text = title.ifEmpty { stringResource(id = R.string.untitled) },
                style = BodyBold,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = Relations3,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

@Composable
fun VaultSpaceAddCard(
    onCreateSpaceClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                onCreateSpaceClicked()
            }
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 32.dp),
            painter = painterResource(id = R.drawable.ic_vault_create_space_card_button_plus),
            contentDescription = "Plus icon"
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultScreenToolbarPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {}
    )
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultSpaceCardPreview() {
    VaultSpaceCard(
        title = "B&O Museum",
        subtitle = "Private space",
        onCardClicked = {},
        wallpaper = Wallpaper.Default,
        icon = SpaceIconView.Placeholder()
    )
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
fun VaultScreenPreview() {
    VaultScreen(
        spaces = buildList {
            add(
                VaultSpaceView(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "B&O Museum",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble()
                        )
                    ),
                    icon = SpaceIconView.Placeholder()
                )
            )
        },
        onSpaceClicked = {},
        onCreateSpaceClicked = {},
        onSettingsClicked = {},
        onOrderChanged = {}
    )
}
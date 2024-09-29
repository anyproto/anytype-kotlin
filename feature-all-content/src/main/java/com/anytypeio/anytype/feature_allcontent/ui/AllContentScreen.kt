package com.anytypeio.anytype.feature_allcontent.ui

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.widgets.DefaultBasicAvatarIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultEmojiObjectIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultFileObjectImageIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultObjectBookmarkIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultObjectImageIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultProfileAvatarIcon
import com.anytypeio.anytype.core_ui.widgets.DefaultProfileIconImage
import com.anytypeio.anytype.core_ui.widgets.DefaultTaskObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_allcontent.BuildConfig
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiContentState
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun AllContentWrapperScreen(
    uiTitleState: UiTitleState,
    uiTabsState: UiTabsState,
    uiMenuButtonViewState: MenuButtonViewState,
    uiMenuState: UiMenuState,
    uiState: UiContentState,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (AllContentSort) -> Unit
) {
    val objects = remember { mutableStateOf<List<UiContentItem>>(emptyList()) }
    if (uiState is UiContentState.Content) {
        objects.value = uiState.items
    }
    AllContentMainScreen(
        uiTitleState = uiTitleState,
        uiTabsState = uiTabsState,
        uiMenuButtonViewState = uiMenuButtonViewState,
        onTabClick = onTabClick,
        objects = objects,
        isLoading = uiState is UiContentState.Loading,
        onQueryChanged = onQueryChanged,
        uiMenuState = uiMenuState,
        onModeClick = onModeClick,
        onSortClick = onSortClick
    )
}

@Composable
fun AllContentMainScreen(
    uiTitleState: UiTitleState,
    uiTabsState: UiTabsState,
    uiMenuButtonViewState: MenuButtonViewState,
    uiMenuState: UiMenuState,
    objects: MutableState<List<UiContentItem>>,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (AllContentSort) -> Unit,
    isLoading: Boolean
) {
    val modifier = Modifier
        .background(color = colorResource(id = R.color.background_primary))

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        topBar = {
            Column(
                modifier = if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                else
                    Modifier.fillMaxWidth()
            ) {
                if (uiTitleState !is UiTitleState.Hidden) {
                    AllContentTopBarContainer(
                        titleState = uiTitleState,
                        menuButtonState = uiMenuButtonViewState,
                        uiMenuState = uiMenuState,
                        onSortClick = onSortClick,
                        onModeClick = onModeClick,
                    )
                }

                if (uiTabsState is UiTabsState.Default) {
                    AllContentTabs(tabsViewState = uiTabsState) { tab ->
                        onTabClick(tab)
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                AllContentSearchBar(onQueryChanged)
                Spacer(modifier = Modifier.size(10.dp))
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                        .padding(paddingValues)
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
            if (isLoading) {
                Box(modifier = contentModifier) {
                    LoadingState()
                }
            } else {
                ContentItems(modifier = contentModifier, items = objects.value)
            }
        }
    )
}

@Composable
private fun ContentItems(modifier: Modifier, items: List<UiContentItem>) {
    LazyColumn(modifier = modifier) {
        items(
            count = items.size,
            key = { index -> items[index].id },
            contentType = { index ->
                when (items[index]) {
                    is UiContentItem.Group -> "group"
                    is UiContentItem.Item -> "item"
                }
            }
        ) { index ->
            when (val item = items[index]) {
                is UiContentItem.Group -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(start = 20.dp, bottom = 8.dp),
                            text = item.title(),
                            style = Caption1Regular,
                            color = colorResource(id = R.color.text_secondary),
                        )
                    }
                }

                is UiContentItem.Item -> {
                    Item(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .bottomBorder()
                            .animateItem(),
                        item = item
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingState() {
    val loadingAlpha by animateFloatAsState(targetValue = 1f, label = "")
    DotsLoadingIndicator(
        animating = true,
        modifier = Modifier
            .graphicsLayer { alpha = loadingAlpha }
            .align(Alignment.Center),
        animationSpecs = FadeAnimationSpecs(itemCount = 3),
        color = colorResource(id = R.color.glyph_active),
        size = ButtonSize.Small
    )
}

@DefaultPreviews
@Composable
fun PreviewLoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        LoadingState()
    }
}

@Composable
private fun Item(
    modifier: Modifier,
    item: UiContentItem.Item
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp, 12.dp, 12.dp, 12.dp)
                .size(48.dp)
                .align(CenterVertically)
        ) {
            AllContentItemIcon(icon = item.icon, modifier = Modifier)
        }
        Column(
            modifier = Modifier
                .align(CenterVertically)
                .padding(0.dp, 0.dp, 60.dp, 0.dp)
        ) {

            val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }

            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val description = item.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = Relations3,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val typeName = item.typeName
            if (!typeName.isNullOrBlank()) {
                Text(
                    text = typeName,
                    style = Relations3,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AllContentItemIcon(
    icon: ObjectIcon,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    onTaskIconClicked: (Boolean) -> Unit = {},
    avatarBackgroundColor: Int = R.color.shape_secondary,
    avatarFontSize: TextUnit = 28.sp,
    avatarTextStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        color = colorResource(id = R.color.text_white)
    )
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> DefaultProfileAvatarIcon(
            modifier = modifier,
            iconSize = iconSize,
            icon = icon,
            avatarTextStyle = avatarTextStyle,
            avatarFontSize = avatarFontSize,
            avatarBackgroundColor = avatarBackgroundColor
        )

        is ObjectIcon.Profile.Image -> DefaultProfileIconImage(icon, modifier, iconSize)
        is ObjectIcon.Basic.Emoji -> DefaultEmojiObjectIcon(modifier, iconSize, icon)
        is ObjectIcon.Basic.Image -> DefaultObjectImageIcon(icon.hash, modifier, iconSize)
        is ObjectIcon.Basic.Avatar -> DefaultBasicAvatarIcon(modifier, iconSize, icon)
        is ObjectIcon.Bookmark -> DefaultObjectBookmarkIcon(icon.image, modifier, iconSize)
        is ObjectIcon.Task -> DefaultTaskObjectIcon(modifier, iconSize, icon, onTaskIconClicked)
        is ObjectIcon.File -> {
            DefaultFileObjectImageIcon(
                fileName = icon.fileName.orEmpty(),
                mime = icon.mime.orEmpty(),
                modifier = modifier,
                iconSize = iconSize,
                extension = icon.extensions
            )
        }

        else -> {
            // Draw nothing.
        }
    }
}

@Composable
private fun BoxScope.ErrorState(message: String) {
    Text(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.Center),
        text = "Error : message",
        color = colorResource(id = R.color.palette_system_red),
        style = UXBody
    )
}

@Composable
fun UiContentItem.Group.title(): String {
    return when (this) {
        is UiContentItem.Group.Today -> stringResource(R.string.allContent_group_today)
        is UiContentItem.Group.Yesterday -> stringResource(R.string.allContent_group_yesterday)
        is UiContentItem.Group.Previous7Days -> stringResource(R.string.allContent_group_prev_7)
        is UiContentItem.Group.Previous14Days -> stringResource(R.string.allContent_group_prev_14)
        is UiContentItem.Group.Month -> title
        is UiContentItem.Group.MonthAndYear -> title
    }
}

object AllContentNavigation {
    const val ALL_CONTENT_MAIN = "all_content_main"
}

@Composable
fun Modifier.bottomBorder(
    strokeWidth: Dp = 0.5.dp,
    color: Color = colorResource(R.color.shape_primary)
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.ButtonSize
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
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentUiState
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun AllContentWrapperScreen(
    uiState: AllContentUiState,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val tabsState = remember { mutableStateOf<TabsViewState>(TabsViewState.Hidden) }
    val titleState = remember { mutableStateOf<TopBarViewState>(TopBarViewState.Hidden) }
    val objects = remember { mutableStateOf<List<DefaultObjectView>>(emptyList()) }
    LaunchedEffect(uiState) {
        if (uiState is AllContentUiState.Initial) {
            tabsState.value = uiState.tabsViewState
            titleState.value = uiState.topToolbarState
        }
    }
    if (uiState is AllContentUiState.Content) {
        objects.value = uiState.items
    }
    AllContentMainScreen(
        titleState = titleState,
        tabs = tabsState,
        onTabClick = onTabClick,
        objects = objects,
        isLoading = uiState is AllContentUiState.Loading,
        onQueryChanged = onQueryChanged
    )
}

@Composable
fun AllContentMainScreen(
    titleState: MutableState<TopBarViewState>,
    tabs: MutableState<TabsViewState>,
    objects: MutableState<List<DefaultObjectView>>,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
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
                if (titleState.value is TopBarViewState.Default) {
                    AllContentTopBarContainer(
                        state = titleState.value as TopBarViewState.Default
                    )
                }

                if (tabs.value is TabsViewState.Default) {
                    AllContentTabs(
                        tabsViewState = tabs.value as TabsViewState.Default,
                    ) { tab ->
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
                LazyColumn(modifier = contentModifier) {
                    items(
                        count = objects.value.size,
                        key = { index -> objects.value[index].id }
                    ) {
                        Item(
                            modifier = Modifier.animateItem(),
                            view = objects.value[it]
                        )
                    }
                }
            }
        }
    )
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
    view: DefaultObjectView
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp, 12.dp, 12.dp, 12.dp)
                .size(48.dp)
                .align(CenterVertically)
        ) {
            AllContentItemIcon(icon = view.icon, modifier = Modifier)
        }
        Column(
            modifier = Modifier
                .align(CenterVertically)
                .padding(0.dp, 0.dp, 60.dp, 0.dp)
        ) {

            val name = view.name.trim().ifBlank { stringResource(R.string.untitled) }

            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val description = view.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = Relations3,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val typeName = view.typeName
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

object AllContentNavigation {
    const val ALL_CONTENT_MAIN = "all_content_main"
}
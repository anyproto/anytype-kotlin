package com.anytypeio.anytype.ui.publishtoweb

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.publishtoweb.PublishToWebViewState
import com.anytypeio.anytype.ui_settings.space.new_settings.NewSpaceNameInputField
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishToWebScreen(
    viewState: PublishToWebViewState,
    onPublishClicked: (String, Boolean) -> Unit = { _, _ -> },
    onUnpublishClicked: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateClicked: (String, Boolean) -> Unit = { _, _ -> },
    onPreviewClicked: () -> Unit = {}
) {

    var showEditUrl by remember { mutableStateOf(false) }

    var showJoinSpaceBannerChecked by remember { mutableStateOf(true) }

    val initialUrl = if (viewState is PublishToWebViewState.Init) "" else "/${viewState.uri}"
    var url by rememberSaveable(viewState.uri) { mutableStateOf(initialUrl) }
    val isLoading = viewState is PublishToWebViewState.Loading

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Dragger(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Header(
                text = stringResource(R.string.publish_to_web)
            )

            Section(
                title = stringResource(R.string.web_publishing_customize_url)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = colorResource(R.color.transparent_tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    text = viewState.domain,
                    style = BodyRegular,
                    color = colorResource(R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(
                        width = 0.5.dp,
                        color = colorResource(R.color.shape_primary),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .noRippleClickable {
                        showEditUrl = true
                    }
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    style = BodyRegular.copy(
                        color = colorResource(R.color.text_primary)
                    ),
                    text = url
                )
            }
            Section(
                title = stringResource(R.string.preferences)
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_plus_rounded_24),
                    contentDescription = stringResource(R.string.content_description_plus_button)
                )
                Text(
                    text = stringResource(R.string.web_publishing_join_space_button),
                    style = BodyRegular,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
                Switch(
                    checked = showJoinSpaceBannerChecked,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = colorResource(R.color.color_accent_80)
                    ),
                    onCheckedChange = {
                        showJoinSpaceBannerChecked = it
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(color = colorResource(R.color.shape_transparent_tertiary))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                PreviewCard(
                    objectName = viewState.objectName,
                    spaceName = viewState.spaceName,
                    onPreviewClicked = onPreviewClicked,
                    showSpaceHeader = showJoinSpaceBannerChecked,
                    objectIcon = when(viewState) {
                        is PublishToWebViewState.NotPublished -> viewState.icon
                        is PublishToWebViewState.Published -> viewState.icon
                        is PublishToWebViewState.Publishing -> viewState.icon
                        is PublishToWebViewState.FailedToPublish -> viewState.icon
                        is PublishToWebViewState.FailedToUpdate -> viewState.icon
                        else -> ObjectIcon.None
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                when(viewState) {
                    is PublishToWebViewState.NotPublished -> {
                        ButtonPrimary(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                onPublishClicked(
                                    url,
                                    showJoinSpaceBannerChecked
                                )
                            },
                            text = stringResource(R.string.web_publishing_publish),
                            size = ButtonSize.Large
                        )
                    }
                    is PublishToWebViewState.Published -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            ButtonSecondary(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onUnpublishClicked(
                                        url,
                                        showJoinSpaceBannerChecked
                                    )
                                },
                                text = stringResource(R.string.web_publishing_unpublish),
                                size = ButtonSize.Large
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ButtonPrimary(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onUpdateClicked(
                                        url, showJoinSpaceBannerChecked
                                    )
                                },
                                text = stringResource(R.string.web_publishing_update),
                                size = ButtonSize.Large
                            )
                        }
                    }
                    is PublishToWebViewState.Publishing -> {
                        ButtonPrimaryLoading(
                            modifierButton = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                // Do nothing
                            },
                            text = stringResource(R.string.web_publishing_update),
                            size = ButtonSize.Large,
                            loading = true
                        )
                    }
                    is PublishToWebViewState.FailedToPublish -> {
                        ButtonPrimary(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                onPublishClicked(
                                    url,
                                    showJoinSpaceBannerChecked
                                )
                            },
                            text = stringResource(R.string.web_publishing_publish),
                            size = ButtonSize.Large
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Failed to publish: ${viewState.err}",
                            style = Caption1Regular,
                            color = colorResource(R.color.palette_system_red),
                            maxLines = 3,
                            textAlign = TextAlign.Center
                        )
                    }
                    is PublishToWebViewState.FailedToUpdate -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            ButtonSecondary(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onUnpublishClicked(
                                        url,
                                        showJoinSpaceBannerChecked
                                    )
                                },
                                text = stringResource(R.string.web_publishing_unpublish),
                                size = ButtonSize.Large
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ButtonPrimary(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onUpdateClicked(
                                        url,
                                        showJoinSpaceBannerChecked
                                    )
                                },
                                text = stringResource(R.string.web_publishing_update),
                                size = ButtonSize.Large
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            text = stringResource(
                                R.string.web_publishing_failed_to_update,
                                viewState.err
                            ),
                            style = Caption1Regular,
                            color = colorResource(R.color.palette_system_red),
                            maxLines = 3,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        // Do nothing.
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .background(color = colorResource(R.color.background_secondary))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    color = colorResource(R.color.glyph_active),
                    trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                    strokeWidth = 2.dp
                )
            }
        }

        if (showEditUrl) {
            ModalBottomSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                modifier = Modifier.padding(top = 48.dp),
                containerColor = colorResource(R.color.background_secondary),
                onDismissRequest = {
                    showEditUrl = false
                },
                dragHandle = {
                    Dragger(
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            ) {
                EditUrlField(
                    initialInput = url,
                    onSaveFieldValueClicked = { value ->
                        Timber.d("DROID-3786 SAVING VALUE: $value")
                        url = value
                        showEditUrl = false
                    }
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun PublishToWebScreenPreview() {
    PublishToWebScreen(
        viewState = PublishToWebViewState.NotPublished(
            domain = "Test",
            uri = "test",
            objectName = "What I Learned as a Product Designer",
            spaceName = "Anytype Design"
        )
    )
}

@DefaultPreviews
@Composable
fun PublishToWebScreenNotPublishedPreview() {
    PublishToWebScreen(
        viewState = PublishToWebViewState.NotPublished(
            domain = "Test",
            uri = "test",
            objectName = "What I Learned as a Product Designer",
            spaceName = "Anytype Design"
        )
    )
}

@DefaultPreviews
@Composable
fun PublishToWebScreenPublishedPreview() {
    PublishToWebScreen(
        viewState = PublishToWebViewState.Published(
            domain = "Test",
            uri = "provence",
            objectName = "What I Learned as a Product Designer",
            spaceName = "Anytype Design"
        )
    )
}

@DefaultPreviews
@Composable
fun PublishToWebScreenPublishingPreview() {
    PublishToWebScreen(
        viewState = PublishToWebViewState.Publishing(
            domain = "Test",
            uri = "provence",
            objectName = "What I Learned as a Product Designer",
            spaceName = "Anytype Design"
        )
    )
}

@Composable
private fun PreviewCard(
    objectName: String,
    spaceName: String,
    objectIcon: ObjectIcon = ObjectIcon.None,
    showSpaceHeader: Boolean = false,
    onPreviewClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(
                color = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_secondary),
                shape = RoundedCornerShape(8.dp)
            )
            .fillMaxWidth()
            .height(242.dp)
            .clickable {
                onPreviewClicked()
            }
    ) {

        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.shape_secondary),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = colorResource(R.color.transparent_tertiary),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        if (showSpaceHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(end = 8.dp)
                ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spaceName,
                    style = Caption2Medium,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.glyph_button),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                        text = stringResource(R.string.web_publishing_join),
                        style = Caption2Medium,
                        color = colorResource(R.color.text_label_inversion)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (objectIcon != ObjectIcon.None) {
            ListWidgetObjectIcon(
                icon = objectIcon,
                iconSize = 40.dp,
                modifier = Modifier.padding(
                    start = 32.dp,
                    bottom = 8.dp
                )
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = objectName,
            style = HeadlineSubheading,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(R.color.text_primary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(color = colorResource(R.color.shape_secondary))
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .height(6.dp)
                .width(178.dp)
                .padding(horizontal = 32.dp)
                .background(color = colorResource(R.color.shape_secondary))
        )
    }
}

@DefaultPreviews
@Composable
private fun PreviewCardPreview() {
    PreviewCard(
        objectName = "What I Learned as a Product Designersigner What I Learned as a Product Designer",
        spaceName = "Anytype Design",
        onPreviewClicked = {},
        showSpaceHeader = false,
        objectIcon = ObjectIcon.Basic.Emoji(
            unicode = "❤️"
        )
    )
}

@Composable
private fun EditUrlField(
    initialInput: String,
    onSaveFieldValueClicked: (String) -> Unit
) {

    var fieldInput by remember { mutableStateOf(initialInput) }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colorResource(id = R.color.background_secondary),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp)
                        .noRippleClickable {
                            onSaveFieldValueClicked(fieldInput)
                        },
                    text = stringResource(R.string.done),
                    style = PreviewTitle1Medium,
                    color = if (fieldInput != initialInput) {
                        colorResource(id = R.color.text_primary)
                    } else {
                        colorResource(id = R.color.text_tertiary)
                    }
                )
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)

            Box(
                modifier = contentModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                NewSpaceNameInputField(
                    fieldTitle = stringResource(R.string.web_publishing_customize_url),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            shape = RoundedCornerShape(16.dp),
                            width = 2.dp,
                            color = colorResource(id = R.color.palette_system_amber_50)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    name = fieldInput,
                    onNameSet = { newName ->
                        fieldInput = newName
                    },
                    isEditEnabled = true
                )
            }
        }
    )
}
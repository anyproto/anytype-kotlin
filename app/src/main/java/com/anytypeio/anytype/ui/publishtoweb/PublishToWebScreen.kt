package com.anytypeio.anytype.ui.publishtoweb

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.publishtoweb.PublishToWebViewState

@Composable
fun PublishToWebScreen(
    viewState: PublishToWebViewState,
    onPublishClicked: (String, Boolean) -> Unit = { _, _ -> },
    onUnpublishClicked: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateClicked: (String, Boolean) -> Unit = { _, _ -> },
    onPreviewClicked: () -> Unit = {}
) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showJoinSpaceBannerChecked by remember { mutableStateOf(true) }

    val textFieldState = if (viewState !is PublishToWebViewState.Init)
        rememberTextFieldState(initialText = "/" + viewState.uri)
    else
        TextFieldState()

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
                    ),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    state = textFieldState,
                    textStyle = BodyRegular.copy(
                        color = colorResource(R.color.text_primary)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
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
                    showSpaceHeader = showJoinSpaceBannerChecked
                )
                Spacer(modifier = Modifier.height(12.dp))
                when(viewState) {
                    is PublishToWebViewState.NotPublished -> {
                        ButtonPrimary(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = {
                                onPublishClicked(
                                    textFieldState.text.toString(),
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
                                        textFieldState.text.toString(),
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
                                        textFieldState.text.toString(), showJoinSpaceBannerChecked
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
                                    textFieldState.text.toString(),
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
                                        textFieldState.text.toString(),
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
                                        textFieldState.text.toString(),
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
                        text = "Join",
                        style = Caption2Medium,
                        color = colorResource(R.color.text_label_inversion)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(32.dp))
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
        showSpaceHeader = false
    )
}
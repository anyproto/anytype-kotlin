package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.ButtonWarningLoading
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.UxSmallTextRegular


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertWithTwoButtons() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_PRIMARY,
            icon = R.drawable.ic_popup_update_56,
            firstButtonText = "Cancel",
            secondButtonText = "Update",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertWithWarningAndTwoButtons() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_PRIMARY,
            icon = R.drawable.ic_popup_duck_56,
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertWithWarningButton() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_WARNING,
            icon = R.drawable.ic_popup_question_56,
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertWithMessageButton() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_WARNING,
            icon = R.drawable.ic_popup_alert_56,
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AlertWithLoadingSecondButton() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_PRIMARY,
            icon = R.drawable.ic_popup_update_56,
            firstButtonText = "Cancel",
            secondButtonText = "Processing",
            title = "Processing request",
            description = "Please wait while we process your request...",
            isSecondButtonLoading = true
        )
    )
}

@Composable
fun GenericAlert(
    modifier: Modifier = Modifier,
    onFirstButtonClicked: () -> Unit = {},
    onSecondButtonClicked: () -> Unit = {},
    config: AlertConfig,
    addBottomSpacer: Boolean = true
) {
    val icon = config.icon
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        AlertIcon(icon)
        Spacer(modifier = Modifier.height(16.dp))
        AlertTitle(config.title)
        if (config.withDescription) {
            Spacer(modifier = Modifier.height(8.dp))
            AlertDescription(config.description)
        }
        Spacer(modifier = Modifier.height(19.dp))
        AlertButtons(
            config = config,
            onLeftButtonClicked = onFirstButtonClicked,
            onRightButtonClicked = onSecondButtonClicked
        )
        if (addBottomSpacer) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlertDescription(description: String, style : TextStyle = UxSmallTextRegular) {
    Text(
        text = description,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        color = colorResource(id = R.color.text_primary),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AlertTitle(title: String, style: TextStyle = HeadlineHeading) {
    Text(
        text = title,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        color = colorResource(id = R.color.text_primary),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AlertIcon(icon: Int) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(56.dp),
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = R.string.alert_icon_description)
        )
    }
}

@Composable
private fun AlertButtons(
    config: AlertConfig,
    onLeftButtonClicked: () -> Unit,
    onRightButtonClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (config) {
            is AlertConfig.WithOneButton -> {
                when (config.firstButtonType) {
                    BUTTON_SECONDARY -> {
                        ButtonSecondary(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_PRIMARY -> {
                        ButtonPrimary(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_WARNING -> {
                        ButtonWarning(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            is AlertConfig.WithTwoButtons -> {
                when (config.firstButtonType) {
                    BUTTON_SECONDARY -> {
                        ButtonSecondary(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_PRIMARY -> {
                        ButtonPrimary(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_WARNING -> {
                        ButtonWarning(
                            text = config.firstButtonText,
                            onClick = onLeftButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                when (config.secondButtonType) {
                    BUTTON_SECONDARY -> {
                        if (config.isSecondButtonLoading) {
                            ButtonSecondaryLoading(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifierButton = Modifier.fillMaxWidth(),
                                modifierBox = Modifier.weight(1f),
                                loading = true
                            )
                        } else {
                            ButtonSecondary(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    BUTTON_PRIMARY -> {
                        if (config.isSecondButtonLoading) {
                            ButtonPrimaryLoading(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifierButton = Modifier.fillMaxWidth(),
                                modifierBox = Modifier.weight(1f),
                                loading = true
                            )
                        } else {
                            ButtonPrimary(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    BUTTON_WARNING -> {
                        if (config.isSecondButtonLoading) {
                            ButtonWarningLoading(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifierButton = Modifier.fillMaxWidth(),
                                modifierBox = Modifier.weight(1f),
                                loading = true
                            )
                        } else {
                            ButtonWarning(
                                text = config.secondButtonText,
                                onClick = onRightButtonClicked,
                                size = ButtonSize.Large,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Prompt(
    showDragger: Boolean = true,
    title: String,
    description: String,
    icon: Int? = null,
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showDragger) {
            Dragger(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
        if (icon != null) {
            AlertIcon(icon)
            Spacer(modifier = Modifier.height(15.dp))
        }
        Text(
            text = title,
            style = HeadlineHeading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = BodyCalloutRegular,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = primaryButtonText,
            onClick = throttledClick(
                onClick = {
                    onPrimaryButtonClicked()
                }
            ),
            size = ButtonSize.Large
        )
        Spacer(modifier = Modifier.height(8.dp))
        ButtonSecondary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = secondaryButtonText,
            onClick = throttledClick(
                onClick = {
                    onSecondaryButtonClicked()
                }
            ),
            size = ButtonSize.Large
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PromptPreview() {
    Prompt(
        icon = R.drawable.ic_popup_alert_56,
        title = "Get notified",
        description = "Please enable notifications to stay informed about any requests to join or leave spaces.",
        primaryButtonText = "Enable",
        secondaryButtonText = "Not now",
        onPrimaryButtonClicked = {},
        onSecondaryButtonClicked = {}
    )
}

sealed class AlertConfig {

    abstract val title: String
    abstract val description: String
    abstract val icon: Int

    val withDescription get() = description.isNotEmpty()

    data class WithTwoButtons(
        override val icon: Int,
        override val title: String,
        override val description: String,
        val firstButtonText: String,
        val secondButtonText: String,
        val firstButtonType: ButtonType,
        val secondButtonType: ButtonType,
        val isSecondButtonLoading: Boolean = false
    ) : AlertConfig()

    data class WithOneButton(
        override val icon: Int,
        override val title: String,
        override val description: String,
        val firstButtonText: String,
        val firstButtonType: ButtonType
    ) : AlertConfig()
}

typealias ButtonType = Int

const val BUTTON_PRIMARY = 0
const val BUTTON_SECONDARY = 1
const val BUTTON_WARNING = 2
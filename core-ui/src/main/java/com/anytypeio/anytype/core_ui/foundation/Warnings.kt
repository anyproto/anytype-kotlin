package com.anytypeio.anytype.core_ui.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading


@Preview
@Composable
fun AlertWithTwoButtons() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_PRIMARY,
            icon = AlertConfig.Icon(
                icon = R.drawable.ic_alert_update,
                gradient = GRADIENT_TYPE_GREEN
            ),
            firstButtonText = "Cancel",
            secondButtonText = "Update",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview
@Composable
fun AlertWithWarningAndTwoButtons() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_PRIMARY,
            icon = AlertConfig.Icon(
                icon = R.drawable.ic_alert_error,
                gradient = GRADIENT_TYPE_RED
            ),
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview
@Composable
fun AlertWithWarningButton() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_WARNING,
            icon = AlertConfig.Icon(
                icon = R.drawable.ic_alert_question_warning,
                gradient = GRADIENT_TYPE_RED
            ),
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Preview
@Composable
fun AlertWithMessageButton() {
    GenericAlert(
        onFirstButtonClicked = {},
        onSecondButtonClicked = {},
        config = AlertConfig.WithTwoButtons(
            firstButtonType = BUTTON_SECONDARY,
            secondButtonType = BUTTON_WARNING,
            icon = AlertConfig.Icon(
                icon = R.drawable.ic_alert_message,
                gradient = GRADIENT_TYPE_BLUE
            ),
            firstButtonText = "Later",
            secondButtonText = "Retry",
            title = "It's time to update",
            description = "Some of your data was managed in a newer version of Anytype. Please update the app to work with all your docs and the latest features."
        )
    )
}

@Composable
fun GenericAlert(
    onFirstButtonClicked: () -> Unit = {},
    onSecondButtonClicked: () -> Unit = {},
    config: AlertConfig
) {
    val icon = config.icon
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        if (icon != null) { AlertIcon(icon) }
        Spacer(modifier = Modifier.height(16.dp))
        AlertTitle(config.title)
        Spacer(modifier = Modifier.height(8.dp))
        AlertDescription(config.description)
        Spacer(modifier = Modifier.height(20.dp))
        AlertButtons(
            config = config,
            onLeftButtonClicked = onFirstButtonClicked,
            onRightButtonClicked = onSecondButtonClicked
        )
    }
}

@Composable
fun AlertDescription(description: String, style : TextStyle = BodyRegular) {
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
fun AlertIcon(icon: AlertConfig.Icon) {
    val gradientColors = when(icon.gradient) {
        GRADIENT_TYPE_GREEN -> listOf(GREEN_FROM, GREEN_TO)
        GRADIENT_TYPE_RED -> listOf(RED_FROM, RED_TO)
        GRADIENT_TYPE_BLUE -> listOf(BLUE_FROM, BLUE_TO)
        else -> emptyList()
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
    ) {
        if (!isSystemInDarkTheme()) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                val aspectRatio = maxWidth / maxHeight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(maxOf(aspectRatio, 1f), maxOf(1 / aspectRatio, 1f))
                        .background(Brush.radialGradient(gradientColors))
                        .fillMaxWidth()
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .height(72.dp)
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = icon.icon),
                contentDescription = "Alert icon"
            )
        }
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
            .height(68.dp)
            .padding(horizontal = 20.dp),
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
                        ButtonSecondary(
                            text = config.secondButtonText,
                            onClick = onRightButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_PRIMARY -> {
                        ButtonPrimary(
                            text = config.secondButtonText,
                            onClick = onRightButtonClicked,
                            size = ButtonSize.Large,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BUTTON_WARNING -> {
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

sealed class AlertConfig {

    abstract val title: String
    abstract val description: String
    abstract val icon: Icon?

    data class WithTwoButtons(
        override val icon: Icon?,
        override val title: String,
        override val description: String,
        val firstButtonText: String,
        val secondButtonText: String,
        val firstButtonType: ButtonType,
        val secondButtonType: ButtonType
    ) : AlertConfig()

    data class WithOneButton(
        override val icon: Icon?,
        override val title: String,
        override val description: String,
        val firstButtonText: String,
        val firstButtonType: ButtonType
    ) : AlertConfig()

    data class Icon(
        val gradient: GradientType,
        @DrawableRes val icon: Int,
    )
}

object OvalCornerShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = size.toRect()
        val path = Path().apply {
            addOval(rect)
        }
        return Outline.Generic(path)
    }
}

typealias ButtonType = Int

const val BUTTON_PRIMARY = 0
const val BUTTON_SECONDARY = 1
const val BUTTON_WARNING = 2

typealias GradientType = Int

const val GRADIENT_TYPE_GREEN = 0
const val GRADIENT_TYPE_RED = 1
const val GRADIENT_TYPE_BLUE = 2
val GREEN_FROM = Color(0xFFA9F496)
val GREEN_TO = Color(0xFF00BCF2AF)
val RED_FROM = Color(0xFFFFBCBC)
val RED_TO = Color(0xFF00FFE6E6)
val BLUE_FROM = Color(0xFF80D1FF)
val BLUE_TO = Color(0xFF00BBE7FF)
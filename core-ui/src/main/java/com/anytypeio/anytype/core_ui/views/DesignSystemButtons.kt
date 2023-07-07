package com.anytypeio.anytype.core_ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicator

class ButtonPrimaryXSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.primaryXSmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonPrimarySmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.primarySmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonPrimaryMedium @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.primaryMediumButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonPrimaryLarge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.primaryLargeButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonSecondaryXSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.secondaryXSmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonSecondarySmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.secondarySmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonSecondaryMedium @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.secondaryMediumButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonSecondaryLarge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.secondaryLargeButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonWarningXSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.warningXSmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonWarningSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.warningSmallButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonWarningMedium @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.warningMediumButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

class ButtonWarningLarge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.warningLargeButtonStyle,
) : AppCompatTextView(context, attrs, defStyleAttr)

/**
 * Composable Buttons
 */
@Composable
fun ButtonPrimary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.button_pressed) else colorResource(
            id = R.color.glyph_selected
        )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(id = R.color.button_text),
                disabledBackgroundColor = colorResource(id = R.color.shape_tertiary),
                disabledContentColor = colorResource(id = R.color.text_tertiary)
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Text(
                text = text,
                style = size.textStyle
            )
        }
    }
}

@Composable
fun LoadingButtonPrimary(
    text: String = "",
    modifier: Modifier = Modifier,
    size: ButtonSize,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {
    val contentAlpha by animateFloatAsState(targetValue = if (loading) 0f else 1f)
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.button_pressed) else colorResource(
            id = R.color.glyph_selected
        )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                enabled = enabled,
                shape = RoundedCornerShape(size.cornerSize),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    contentColor = colorResource(id = R.color.button_text),
                    disabledBackgroundColor = colorResource(id = R.color.shape_tertiary),
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                ),
                modifier = modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = size.contentPadding
            ) {
                Text(
                    text = text,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                    style = size.textStyle
                )
            }
            LoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.button_text),
                size = size
            )
        }
    }
}

@Composable
fun ButtonSecondary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.shape_transparent) else Color.Transparent
    val borderColor = if (enabled) colorResource(id = R.color.shape_primary) else colorResource(
        id = R.color.shape_secondary
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(id = R.color.text_primary),
                disabledBackgroundColor = Color.Transparent,
                disabledContentColor = colorResource(id = R.color.text_tertiary)
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Text(
                text = text,
                style = size.textStyle
            )
        }
    }
}

@Composable
fun ButtonLoadingSecondary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: ButtonSize,
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {

    val contentAlpha by animateFloatAsState(targetValue = if (loading) 0f else 1f)
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.shape_transparent) else Color.Transparent
    val borderColor = if (enabled) colorResource(id = R.color.shape_primary) else colorResource(
        id = R.color.shape_secondary
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(id = R.color.text_primary),
                disabledBackgroundColor = Color.Transparent,
                disabledContentColor = colorResource(id = R.color.text_tertiary)
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Text(
                text = text,
                style = size.textStyle,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
            )
        }
        LoadingIndicator(
            animating = loading,
            modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
            animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
            color = colorResource(id = R.color.text_primary),
            size = size
        )
    }
}

@Composable
fun ButtonWarning(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val contentColor =
        if (isPressed.value) colorResource(id = R.color.palette_light_red) else colorResource(
            id = R.color.palette_system_red
        )
    val borderColor = colorResource(id = R.color.shape_primary)

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = contentColor
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Text(
                text = text,
                style = size.textStyle
            )
        }
    }
}

@Composable
fun LoadingButtonWarning(
    text: String = "",
    modifier: Modifier = Modifier,
    size: ButtonSize,
    onClick: () -> Unit = {},
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {
    val contentAlpha by animateFloatAsState(targetValue = if (loading) 0f else 1f)
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val contentColor =
        if (isPressed.value) colorResource(id = R.color.palette_light_red) else colorResource(
            id = R.color.palette_system_red
        )
    val borderColor = colorResource(id = R.color.shape_primary)

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(size.cornerSize),
                border = BorderStroke(width = 1.dp, color = borderColor),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = contentColor
                ),
                modifier = modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = size.contentPadding
            ) {
                Text(
                    text = text,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                    style = size.textStyle
                )
            }
            LoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = contentColor,
                size = size
            )
        }
    }
}

enum class ButtonSize(
    val cornerSize: Dp,
    var contentPadding: PaddingValues,
    val textStyle: TextStyle
) {
    XSmall(
        cornerSize = 6.dp,
        contentPadding = PaddingValues(9.5.dp, 4.8.dp, 9.5.dp, 4.8.dp),
        textStyle = Caption1Medium
    ),
    XSmallSecondary(
        cornerSize = 6.dp,
        contentPadding = PaddingValues(9.5.dp, 4.8.dp, 9.5.dp, 4.8.dp),
        textStyle = Caption1Regular
    ),
    Small(
        cornerSize = 8.dp,
        contentPadding = PaddingValues(11.dp, 7.4.dp, 11.dp, 7.4.dp),
        textStyle = BodyCalloutMedium
    ),
    SmallSecondary(
        cornerSize = 8.dp,
        contentPadding = PaddingValues(11.dp, 7.4.dp, 11.dp, 7.4.dp),
        textStyle = BodyCalloutRegular
    ),
    Medium(
        cornerSize = 10.dp,
        contentPadding = PaddingValues(55.dp, 10.dp, 55.dp, 10.dp),
        textStyle = ButtonMedium
    ),
    MediumSecondary(
        cornerSize = 10.dp,
        contentPadding = PaddingValues(55.dp, 10.dp, 55.dp, 10.dp),
        textStyle = UXBody
    ),
    Large(
        cornerSize = 12.dp,
        contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 12.dp),
        textStyle = ButtonMedium
    ),
    LargeSecondary(
        cornerSize = 12.dp,
        contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 12.dp),
        textStyle = ButtonRegular
    )

}

object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}

@Composable
@Preview
fun MyPrimaryButton() {
    ButtonPrimary(
        onClick = {},
        size = ButtonSize.Large,
        text = "Login",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
@Preview
fun MySecondaryButton() {
    ButtonSecondary(
        onClick = {},
        size = ButtonSize.LargeSecondary,
        text = "Cancel",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
@Preview
fun MyWarningButton() {
    ButtonWarning(
        onClick = {},
        size = ButtonSize.Large,
        text = "Log out",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
@Preview
fun MyLoadingWarningButton() {
    LoadingButtonWarning(
        onClick = {},
        size = ButtonSize.Large,
        text = "Log out",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}
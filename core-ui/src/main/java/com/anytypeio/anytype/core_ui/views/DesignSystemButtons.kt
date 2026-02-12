package com.anytypeio.anytype.core_ui.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs

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
@OptIn(ExperimentalMaterial3Api::class)
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
        if (isPressed.value) colorResource(id = R.color.glyph_button).copy(alpha = 0.15f)
        else colorResource(id = R.color.glyph_button)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(id = R.color.button_text),
                disabledBackgroundColor = colorResource(id = R.color.shape_secondary),
                disabledContentColor = colorResource(id = R.color.text_label_inversion)
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonPrimaryDarkTheme(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = Color(0xFFF3F2EC)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = Color(0xFF1F1E1D),
                disabledBackgroundColor = Color(0xFF1F1E1D),
                disabledContentColor = Color(0xFF1F1E1D)
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonPrimaryLoading(
    text: String = "",
    modifierBox: Modifier = Modifier,
    modifierButton: Modifier = Modifier,
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
        if (isPressed.value) colorResource(id = R.color.glyph_button).copy(alpha = 0.15f)
        else colorResource(id = R.color.glyph_button)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                enabled = enabled,
                shape = RoundedCornerShape(size.cornerSize),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    contentColor = colorResource(id = R.color.button_text),
                    disabledBackgroundColor = colorResource(id = R.color.shape_secondary),
                    disabledContentColor = colorResource(id = R.color.text_label_inversion)
                ),
                modifier = modifierButton
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
                    style = size.textStyle,
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.button_text),
                size = size
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonSecondaryDarkTheme(
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

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(id = R.color.text_white),
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonSecondaryLoading(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifierBox: Modifier = Modifier,
    modifierButton: Modifier = Modifier,
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

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = {if (!loading) onClick()},
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
                modifier = modifierButton
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
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha },
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.text_primary),
                size = size
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonWarning(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize,
    isEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val contentColor =
        if (isPressed.value) colorResource(id = R.color.palette_light_red) else colorResource(
            id = R.color.palette_system_red
        )
    val borderColor = colorResource(id = R.color.shape_primary)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
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
            contentPadding = size.contentPadding,
            enabled = isEnabled
        ) {
            Text(
                text = text,
                style = size.textStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonWarningLoading(
    text: String = "",
    modifierBox: Modifier = Modifier,
    modifierButton: Modifier = Modifier,
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

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(size.cornerSize),
                border = BorderStroke(width = 1.dp, color = borderColor),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = contentColor
                ),
                modifier = modifierButton
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
                    style = size.textStyle,
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
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
        contentPadding = PaddingValues(9.5.dp, 6.038.dp, 9.5.dp, 6.038.dp),
        textStyle = Caption1Medium
    ),
    XSmallSecondary(
        cornerSize = 6.dp,
        contentPadding = PaddingValues(9.5.dp, 6.038.dp, 9.5.dp, 6.038.dp),
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
        contentPadding = PaddingValues(0.dp, 13.52.dp, 0.dp, 12.dp),
        textStyle = ButtonMedium
    ),
    LargeSecondary(
        cornerSize = 12.dp,
        contentPadding = PaddingValues(0.dp, 13.52.dp, 0.dp, 12.dp),
        textStyle = ButtonRegular
    )

}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
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
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
fun MyPrimaryButtonDisabled() {
    ButtonPrimary(
        onClick = {},
        size = ButtonSize.Large,
        text = "Login",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        enabled = false
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
fun MyPrimaryButtonDark() {
    ButtonPrimaryDarkTheme(
        onClick = {},
        size = ButtonSize.Large,
        text = "Login",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(backgroundColor = 0x000000, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
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
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(backgroundColor = 0x000000, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
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

class ButtonPrimarySmallIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private lateinit var button: TextView
    private lateinit var icon: ImageView
    private lateinit var buttonContainer: FrameLayout
    private lateinit var iconContainer: FrameLayout

    init {
        setup(context)
    }

    private fun setup(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.ds_button_icon, this, true)
        button = findViewById(R.id.button)
        icon = findViewById(R.id.icon)
        buttonContainer = findViewById(R.id.buttonContainer)
        iconContainer = findViewById(R.id.iconContainer)
    }

    fun setOnButtonClickListener(listener: OnClickListener?) {
        buttonContainer.setOnClickListener(listener)
    }

    fun setOnIconClickListener(listener: OnClickListener?) {
        iconContainer.setOnClickListener(listener)
    }
}
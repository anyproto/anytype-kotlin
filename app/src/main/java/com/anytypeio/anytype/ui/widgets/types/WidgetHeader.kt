package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetHeader(
    icon: ObjectIcon,
    title: String,
    isCardMenuExpanded: MutableState<Boolean>,
    onWidgetHeaderClicked: () -> Unit,
    onWidgetMenuTriggered: () -> Unit,
    onObjectCheckboxClicked: (Boolean) -> Unit = {},
    onExpandElement: () -> Unit = {},
    onCreateElement: () -> Unit = {},
    isExpanded: Boolean = false,
    isInEditMode: Boolean = true,
    hasReadOnlyAccess: Boolean = false,
    canCreateObject: Boolean
) {
    val haptic = LocalHapticFeedback.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            iconSize = 18.dp,
            icon = icon,
            modifier = Modifier.padding(end = 12.dp),
            onTaskIconClicked = onObjectCheckboxClicked,
            emojiFontSize = 18f
        )

        Text(
            text = title.ifEmpty { stringResource(id = R.string.untitled) },
            style = HeadlineSubheading,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isInEditMode)
                        Modifier
                    else if (hasReadOnlyAccess) {
                        Modifier.noRippleClickable {
                            onWidgetHeaderClicked()
                        }
                    } else
                        Modifier.combinedClickable(
                            onClick = onWidgetHeaderClicked,
                            onLongClick = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isCardMenuExpanded.value) {
                                    onWidgetMenuTriggered()
                                }
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )
        )

        if (canCreateObject) {
            Box(
                Modifier
                    .size(18.dp)
                    .noRippleClickable {
                        onCreateElement()
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_widget_system_plus_18),
                    contentDescription = stringResource(R.string.content_description_plus_button),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        val rotation = getAnimatedRotation(isExpanded)
        Box(
            modifier = Modifier
                .size(18.dp)
                .noRippleClickable { onExpandElement() }) {
            Image(
                painterResource(R.drawable.ic_widget_tree_expand),
                contentDescription = "Expand icon",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation.value }
            )
        }
    }
}

@Composable
fun getAnimatedRotation(isExpanded: Boolean): Animatable<Float, AnimationVector1D> {
    val currentRotation = remember {
        mutableStateOf(
            if (isExpanded) ArrowIconDefaults.Expanded else ArrowIconDefaults.Collapsed
        )
    }
    val rotation = remember { Animatable(currentRotation.value) }
    LaunchedEffect(isExpanded) {
        rotation.animateTo(
            targetValue = if (isExpanded)
                ArrowIconDefaults.Expanded
            else
                ArrowIconDefaults.Collapsed,
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ) {
            currentRotation.value = value
        }
    }
    return rotation
}


@Immutable
object ArrowIconDefaults {
    const val Collapsed = -90f
    const val Expanded = 0f
}

@DefaultPreviews
@Composable
fun WidgetHeaderPreview() {
    WidgetHeader(
        icon = ObjectIcon.TypeIcon.Default.DEFAULT,
        title = "Widget title",
        isCardMenuExpanded = remember { mutableStateOf(false) },
        onWidgetHeaderClicked = {},
        onWidgetMenuTriggered = {},
        onObjectCheckboxClicked = {},
        onExpandElement = {},
        onCreateElement = {},
        isInEditMode = false,
        hasReadOnlyAccess = false,
        canCreateObject = true
    )
}
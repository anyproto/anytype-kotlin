package com.anytypeio.anytype.ui.library.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.libraryTabOffset(
    currentTabPosition: TabPosition,
    tabWidth: Dp
): Modifier {
    val currentTabWidth = animateDpAsState(
        targetValue = tabWidth,
        animationSpec = tween(
            durationMillis = ANIMATION_LENGTH,
            easing = FastOutSlowInEasing
        )
    )

    val targetValue = if (currentTabPosition.left == 0.dp) {
        (currentTabPosition.left + currentTabPosition.right - tabWidth - TAB_OFFSET.dp)
    } else {
        (currentTabPosition.left)
    }

    val indicatorOffset = animateDpAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = ANIMATION_LENGTH,
            easing = FastOutSlowInEasing
        )
    )
    return fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset.value)
        .height(INDICATOR_HEIGHT.dp)
        .width(currentTabWidth.value + TAB_OFFSET.dp)
}

private const val TAB_OFFSET = 32
private const val INDICATOR_HEIGHT = 1
private const val ANIMATION_LENGTH = 150
package com.anytypeio.anytype.core_ui.gestures

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import sh.calvin.reorderable.ReorderableCollectionItemScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableCollectionItemScope.ReorderableItemModifier(
    lazyItemScope: LazyItemScope,
    isMenuExpanded: Boolean,
    isReadOnly: Boolean,
    view: View,
    onItemClicked: () -> Unit,
    onItemLongClicked: () -> Unit,
    dragModifier: Modifier? = null,
    shouldEnableLongClick: Boolean = true
): Modifier {
    val haptic = LocalHapticFeedback.current
    val touchSlop = LocalViewConfiguration.current.touchSlop

    var longPressConsumed by remember { mutableStateOf(false) }

    val baseModifier = with(lazyItemScope) {
        Modifier.animateItem(placementSpec = null)
    }
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
        .alpha(if (isMenuExpanded) 0.8f else 1f)
        .background(
            shape = RoundedCornerShape(16.dp),
            color = colorResource(id = R.color.dashboard_card_background)
        )

    val interactionModifier = when {

        isReadOnly -> {
            Modifier.noRippleClickable { onItemClicked() }
        }

        dragModifier != null -> {
            // When drag is enabled, use simple click + custom drag detector.
            // Menu is only shown if shouldEnableLongClick is true.
            Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (longPressConsumed) {
                        longPressConsumed = false
                    } else {
                        onItemClicked()
                    }
                }
                .draggableHandle(
                    dragGestureDetector = LongPressWithSlopDetector(
                        touchSlop = touchSlop,
                        onMenuTrigger = {
                            // Only show menu if widget has menu items
                            if (shouldEnableLongClick) {
                                longPressConsumed = true
                                onItemLongClicked()
                            }
                        },
                        haptic = haptic,
                        onDragStarted = {
                            ViewCompat.performHapticFeedback(
                                view,
                                HapticFeedbackConstantsCompat.GESTURE_START
                            )
                        },
                        onDragStopped = {
                            ViewCompat.performHapticFeedback(
                                view,
                                HapticFeedbackConstantsCompat.GESTURE_END
                            )
                        }
                    )
                )
        }

        shouldEnableLongClick -> {
            // When drag is not enabled, use standard combinedClickable
            Modifier.combinedClickable(
                onClick = { onItemClicked() },
                onLongClick = {
                    onItemLongClicked()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        }

        else -> {
            Modifier.noRippleClickable { onItemClicked() }
        }
    }

    return baseModifier.then(interactionModifier)
}
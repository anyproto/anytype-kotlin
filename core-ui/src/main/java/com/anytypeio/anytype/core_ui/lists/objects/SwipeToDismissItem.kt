package com.anytypeio.anytype.core_ui.lists.objects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.DismissBackground
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import kotlinx.coroutines.delay

@Composable
fun SwipeToDismissItem(
    item: UiObjectsListItem.Item,
    modifier: Modifier,
    animationDuration: Int = 500,
    onObjectClicked: (UiObjectsListItem.Item) -> Unit,
    onMoveToBin: (UiObjectsListItem.Item) -> Unit,
) {
    var isRemoved by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
            return@rememberSwipeToDismissBoxState true
        },
        positionalThreshold = { it * .5f }
    )

    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
        LaunchedEffect(Unit) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    LaunchedEffect(key1 = isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onMoveToBin(item)
        }
    }
    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            modifier = modifier,
            state = dismissState,
            enableDismissFromEndToStart = item.isPossibleToDelete,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                DismissBackground(
                    actionText = stringResource(R.string.move_to_bin),
                    dismissState = dismissState
                )
            },
            content = {
                ObjectsListItem(
                    modifier = Modifier
                        .noRippleThrottledClickable {
                            onObjectClicked(item)
                        },
                    item = item
                )
            }
        )
    }
}
package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun Modifier.noRippleClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
) = clickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)
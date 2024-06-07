package com.anytypeio.anytype.core_ui.views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R

@Composable
fun ButtonUpgrade(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.button_pressed) else colorResource(
            id = R.color.glyph_selected
        )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(
            modifier = modifier
                .height(32.dp)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .background(color = backgroundColor, shape = RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    indication = null
                )
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = text,
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.button_text)
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(backgroundColor = 0x000000, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")fun MyButtonUpgrade() {
    ButtonUpgrade(
        onClick = {},
        text = "✦ Upgrade",
        modifier = Modifier.padding(32.dp)
    )
}
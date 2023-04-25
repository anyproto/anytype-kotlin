package com.anytypeio.anytype.ui.widgets.types

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.Relations2

@Composable
fun EmptyWidgetPlaceholder(
    @StringRes text: Int
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = text),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 18.dp, horizontal = 16.dp),
            style = Relations2.copy(
                color = colorResource(id = R.color.text_secondary_widgets),
            ),
            textAlign = TextAlign.Center
        )
    }
}
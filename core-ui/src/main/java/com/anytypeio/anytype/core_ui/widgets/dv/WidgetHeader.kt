package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.TitleInter15

@Composable
fun WidgetHeader(title: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp, bottom = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = title, //stringResource(R.string.view_layout_widget_title),
                style = Title1,
                color = colorResource(R.color.text_primary)
            )
        }
    }
}
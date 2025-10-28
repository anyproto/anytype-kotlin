package com.anytypeio.anytype.ui.widgets.types

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.presentation.widgets.WidgetView

@Composable
fun EmptyWidgetPlaceholder(
    @StringRes text: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(id = text),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
            style = Relations2.copy(
                color = colorResource(id = R.color.text_secondary),
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun WidgetView.Name.getPrettyName(): String {
    return when (this) {
        is WidgetView.Name.Bundled -> stringResource(id = source.res())
        is WidgetView.Name.Default -> prettyPrintName.ifEmpty { stringResource(id = R.string.untitled) }
        WidgetView.Name.Empty -> stringResource(id = R.string.untitled)
    }
}

@Composable
fun WidgetView.Name.getPrettyNameAndColor(): Pair<String, Color> {
    return when (this) {
        is WidgetView.Name.Bundled -> stringResource(id = source.res()) to colorResource(R.color.text_primary)
        is WidgetView.Name.Default -> if (prettyPrintName.isNotEmpty()) {
            prettyPrintName to colorResource(R.color.text_primary)
        } else {
            stringResource(id = R.string.untitled) to colorResource(R.color.text_tertiary)
        }

        WidgetView.Name.Empty -> stringResource(id = R.string.untitled) to colorResource(R.color.text_tertiary)
    }
}

@Composable
fun WidgetView.Element.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.Element.getPrettyNameAndColor(): Pair<String, Color> {
    return name.getPrettyNameAndColor()
}

@Composable
fun WidgetView.Link.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.Tree.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.SetOfObjects.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.SetOfObjects.Element.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.Gallery.getPrettyName(): String {
    return name.getPrettyName()
}

@Composable
fun WidgetView.Tree.Element.getPrettyName(): String {
    return name.getPrettyName()
}
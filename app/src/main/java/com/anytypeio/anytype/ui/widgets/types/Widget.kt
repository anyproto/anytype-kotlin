package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.getPrettyName
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.presentation.widgets.WidgetView
import kotlin.text.ifEmpty

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

@Composable
fun EmptyWidgetPlaceholderWithCreateButton(
    @StringRes text: Int,
    onCreateClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = text),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
            style = Relations2.copy(
                color = colorResource(id = R.color.text_secondary_widgets),
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        ButtonSecondary(
            onClick = onCreateClicked,
            size = ButtonSize.XSmall,
            text = stringResource(id = R.string.create_object),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
fun WidgetView.Element.getPrettyName(): String {
    return name.getPrettyName()
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun EmptyWidgetPlaceholderPreview() {
    EmptyWidgetPlaceholder(text = R.string.empty_tree_widget)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun EmptyWidgetPlaceholderWithButtonPreview() {
    EmptyWidgetPlaceholderWithCreateButton(
        text = R.string.empty_tree_widget,
        onCreateClicked = {

        }
    )
}
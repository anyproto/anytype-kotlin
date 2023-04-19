package com.anytypeio.anytype.ui.types.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.presentation.types.TypeEditViewModel
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody

@Composable
fun TypeEditHeader(
    vm: TypeEditViewModel,
    readOnly: Boolean
) {

    Column {

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
        ) {
            Dragger(modifier = Modifier.align(Alignment.Center))
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(EditHeaderDefaults.PaddingValues)
                .height(EditHeaderDefaults.Height)
        ) {
            if (!readOnly) {
                Spacer(modifier = Modifier.weight(1f))
            }
            Text(
                text = stringResource(id = R.string.type_editing_title),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
            if (!readOnly) {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.type_editing_uninstall),
                        color = colorResource(id = R.color.palette_system_red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable {
                                vm.uninstallType()
                            },
                        textAlign = TextAlign.End,
                        style = UXBody
                    )
                }
            }
        }
    }

}

@Immutable
private object EditHeaderDefaults {
    val PaddingValues = PaddingValues(start = 12.dp, top = 6.dp, end = 16.dp, bottom = 12.dp)
    val Height = 48.dp
}
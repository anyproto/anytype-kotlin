package com.anytypeio.anytype.feature_object_type.ui.icons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeIconScreen(
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    onIconClicked: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        LazyVerticalGrid(
            columns = 
        ) { }
    }

}
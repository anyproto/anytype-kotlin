package com.anytypeio.anytype.core_ui.relations

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueItemAction
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewState

@Composable
fun ObjectValueScreen(
    state: ObjectValueViewState,
    action: (ObjectValueItemAction) -> Unit,
    onQueryChanged: (String) -> Unit
) {}
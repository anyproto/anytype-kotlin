package com.anytypeio.anytype.core_ui.features.editor.modal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.relations.CheckedIcon
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLanguageBottomSheet(
    languages: List<Pair<String, String>>,
    selectedLanguage: String?,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    ModalBottomSheet(
        modifier = Modifier.systemBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        SelectLanguageContent(
            languages = languages,
            selectedLanguage = selectedLanguage,
            query = query,
            onQueryChanged = { query = it },
            onLanguageSelected = onLanguageSelected
        )
    }
}

@Composable
fun SelectLanguageContent(
    languages: List<Pair<String, String>>,
    selectedLanguage: String?,
    query: String,
    onQueryChanged: (String) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val filteredLanguages = languages
        .filter { (_, displayName) ->
            query.isBlank() || displayName.contains(query, ignoreCase = true)
        }
        .sortedByDescending { (key, _) ->
            key.equals(selectedLanguage, ignoreCase = true)
        }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(6.dp))
        Dragger(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(6.dp))

        SearchField(
            query = query,
            onQueryChanged = onQueryChanged,
            onFocused = {}
        )

        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(
                items = filteredLanguages,
                key = { _, item -> item.first }
            ) { index, (key, displayName) ->
                LanguageItem(
                    name = displayName,
                    isSelected = key.equals(selectedLanguage, ignoreCase = true),
                    onClick = { onLanguageSelected(key) },
                    showDivider = index < filteredLanguages.lastIndex
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CheckedIcon(
                isSelected = isSelected,
                modifier = Modifier.size(24.dp)
            )
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.align(Alignment.BottomCenter),
                paddingStart = 0.dp,
                paddingEnd = 0.dp
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun SelectLanguageContentPreview() {
    SelectLanguageContent(
        languages = listOf(
            "kotlin" to "Kotlin",
            "java" to "Java",
            "python" to "Python",
            "javascript" to "JavaScript",
            "typescript" to "TypeScript",
            "swift" to "Swift",
            "rust" to "Rust",
            "go" to "Go"
        ),
        selectedLanguage = "kotlin",
        query = "",
        onQueryChanged = {},
        onLanguageSelected = {}
    )
}

@DefaultPreviews
@Composable
private fun LanguageItemPreview() {
    Column {
        LanguageItem(
            name = "Kotlin",
            isSelected = true,
            onClick = {},
            showDivider = true
        )
        LanguageItem(
            name = "Java",
            isSelected = false,
            onClick = {},
            showDivider = true
        )
        LanguageItem(
            name = "Python",
            isSelected = false,
            onClick = {},
            showDivider = false
        )
    }
}

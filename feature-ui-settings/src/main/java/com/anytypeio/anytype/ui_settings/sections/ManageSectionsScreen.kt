package com.anytypeio.anytype.ui_settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.presentation.spaces.ManageSectionsState
import com.anytypeio.anytype.presentation.spaces.SectionItem
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.localization.R as LocalizationR

@Composable
fun ManageSectionsScreen(
    state: ManageSectionsState,
    onSectionVisibilityChanged: (WidgetSectionType, Boolean) -> Unit,
    onSectionsReordered: (List<SectionItem>) -> Unit,
    onBackPressed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_primary))
    ) {
        // Header with title and Done button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(LocalizationR.string.manage_sections_title),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.weight(1f)
            )
            
            ButtonSecondary(
                text = stringResource(LocalizationR.string.done),
                onClick = onBackPressed,
                size = ButtonSize.Small
            )
        }

        when (state) {
            is ManageSectionsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(LocalizationR.string.loading),
                        style = BodyCalloutRegular,
                        color = colorResource(R.color.text_secondary)
                    )
                }
            }
            is ManageSectionsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(LocalizationR.string.error_loading_sections),
                        style = BodyCalloutRegular,
                        color = colorResource(R.color.text_secondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
            is ManageSectionsState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.sections) { section ->
                        SectionListItem(
                            section = section,
                            onVisibilityChanged = { isVisible ->
                                onSectionVisibilityChanged(section.type, isVisible)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionListItem(
    section: SectionItem,
    onVisibilityChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = section.canToggle) {
                onVisibilityChanged(!section.isVisible)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Icon(
            painter = painterResource(
                id = if (section.isVisible) {
                    R.drawable.ic_checkbox_checked
                } else {
                    R.drawable.ic_checkbox_unchecked
                }
            ),
            contentDescription = null,
            tint = if (section.isVisible) {
                colorResource(R.color.palette_system_amber_100)
            } else {
                colorResource(R.color.shape_primary)
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Section title
        Text(
            text = getSectionTitle(section.type),
            style = BodyCalloutRegular,
            color = if (section.canToggle) {
                colorResource(R.color.text_primary)
            } else {
                colorResource(R.color.text_secondary)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun getSectionTitle(type: WidgetSectionType): String {
    return when (type) {
        WidgetSectionType.UNREAD -> stringResource(LocalizationR.string.section_unread)
        WidgetSectionType.PINNED -> stringResource(LocalizationR.string.section_pinned)
        WidgetSectionType.OBJECTS -> stringResource(LocalizationR.string.section_objects)
        WidgetSectionType.RECENTLY_EDITED -> stringResource(LocalizationR.string.section_recently_edited)
        WidgetSectionType.BIN -> stringResource(LocalizationR.string.section_bin)
    }
}

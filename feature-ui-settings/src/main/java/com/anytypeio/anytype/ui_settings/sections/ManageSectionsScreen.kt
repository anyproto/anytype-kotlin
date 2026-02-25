package com.anytypeio.anytype.ui_settings.sections

import android.view.View
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.spaces.ManageSectionsState
import com.anytypeio.anytype.presentation.spaces.SectionItem
import com.anytypeio.anytype.ui_settings.R
import com.anytypeio.anytype.localization.R as LocalizationR
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableScope

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(LocalizationR.string.manage_sections_title),
                style = HeadlineSubheading,
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
            
            Text(
                text = stringResource(LocalizationR.string.done),
                style = BodyRegular,
                color = colorResource(R.color.control_accent),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .noRippleThrottledClickable {
                        onBackPressed()
                    }
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
                        style = BodyRegular,
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
                        style = BodyRegular,
                        color = colorResource(R.color.text_secondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
            is ManageSectionsState.Content -> {
                val view = LocalView.current
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ReorderableColumn(
                        modifier = Modifier.fillMaxWidth(),
                        list = state.sections,
                        onSettle = { fromIndex, toIndex ->
                            if (fromIndex != toIndex) {
                                val reordered = state.sections.toMutableList()
                                val item = reordered.removeAt(fromIndex)
                                reordered.add(toIndex, item)
                                onSectionsReordered(reordered)
                            }
                        },
                        onMove = {
                            ViewCompat.performHapticFeedback(
                                view,
                                HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                            )
                        }
                    ) { index, section, isDragging ->
                        key(section.type) {
                            DraggableSectionListItem(
                                section = section,
                                onVisibilityChanged = { isVisible ->
                                    onSectionVisibilityChanged(section.type, isVisible)
                                },
                                view = view
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderableScope.DraggableSectionListItem(
    section: SectionItem,
    onVisibilityChanged: (Boolean) -> Unit,
    view: View
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .longPressDraggableHandle(
                    onDragStarted = {
                        ViewCompat.performHapticFeedback(
                            view,
                            HapticFeedbackConstantsCompat.GESTURE_START
                        )
                    },
                    onDragStopped = {
                        ViewCompat.performHapticFeedback(
                            view,
                            HapticFeedbackConstantsCompat.GESTURE_END
                        )
                    }
                )
                .clickable(enabled = section.canToggle) {
                    onVisibilityChanged(!section.isVisible)
                }
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon - checkbox or empty space
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (section.canToggle) {
                    Image(
                        painter = painterResource(
                            id = if (section.isVisible) {
                                com.anytypeio.anytype.core_ui.R.drawable.ic_checkbox_checked
                            } else {
                                com.anytypeio.anytype.core_ui.R.drawable.ic_checkbox_unchecked
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Section title
            Text(
                text = getSectionTitle(section.type),
                style = BodyRegular,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.weight(1f)
            )

            // Drag handle icon
            if (section.canReorder) {
                Image(
                    painter = painterResource(
                        id = com.anytypeio.anytype.core_ui.R.drawable.ic_drag_handle_dots
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Bottom divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            color = colorResource(R.color.shape_primary),
            thickness = 0.5.dp
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

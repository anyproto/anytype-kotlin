package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingLinkLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingEmailAndSelectionViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.UsecaseItem
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSelectionItem

private val usecaseItems = listOf(
    UsecaseItem(R.drawable.ic_onboarding_usecase_habit, R.string.onboarding_usecase_habit_tracking, "HabitTracking"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_know, R.string.onboarding_usecase_knowledge, "Knowledge"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_life, R.string.onboarding_usecase_life_planning, "LifePlanning"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_messag, R.string.onboarding_usecase_messaging, "Messaging"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_note, R.string.onboarding_usecase_notes, "NoteTaking"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_projects, R.string.onboarding_usecase_projects, "Projects"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_personal, R.string.onboarding_usecase_team_work, "TeamWork"),
    UsecaseItem(R.drawable.ic_onboarding_usecase_other, R.string.onboarding_usecase_other, "Other"),
)

@Composable
fun OnboardingUsecaseScreen(
    vm: OnboardingEmailAndSelectionViewModel,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onContinueClicked: (UsecaseItem) -> Unit = {},
    onSkipClicked: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        vm.sendAnalyticsOnboardingUseCaseScreen()
    }

    var selectedUsecase by remember { mutableStateOf<UsecaseItem?>(null) }
    val shuffledUseCases = remember { usecaseItems.shuffled() }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterStart)
                    .noRippleClickable {
                        onBackClicked()
                    },
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "Back button"
            )
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_anytype_logo),
                contentDescription = "Anytype logo",
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 81.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.onboarding_usecase_title),
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitleSemibold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.48).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.onboarding_usecase_description),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.CenterHorizontally),
                content = {
                    shuffledUseCases.forEach { usecase ->
                        UsecaseSelectionItem(
                            usecase = usecase,
                            isSelected = selectedUsecase == usecase,
                            onSelected = { 
                                selectedUsecase = if (selectedUsecase == usecase) {
                                    null // Deselect if already selected
                                } else {
                                    usecase // Select if not selected
                                }
                            }
                        )
                    }
                }
            )
            // Bottom padding to ensure items aren't hidden behind buttons
            Spacer(modifier = Modifier.height(160.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .align(Alignment.BottomCenter)
        ) {
            ButtonOnboardingPrimaryLarge(
                text = stringResource(id = R.string.onboarding_button_continue),
                onClick = {
                    selectedUsecase?.let { usecase ->
                        onContinueClicked(usecase)
                    }
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
                loading = isLoading,
                enabled = selectedUsecase != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonOnboardingLinkLarge(
                text = stringResource(id = R.string.onboarding_button_skip),
                onClick = {
                    onSkipClicked()
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun UsecaseSelectionItem(
    usecase: UsecaseItem,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val (textColor, backgroundColor) = if (isSelected) {
        R.color.text_primary to R.color.control_accent_25
    } else {
        R.color.text_secondary to R.color.shape_transparent_secondary
    }

    // Outer Box with padding to accommodate the floating checkmark
    Box(
        modifier = Modifier.padding(top = 8.dp, end = 8.dp)
    ) {
        // Inner Box with the main content
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color = colorResource(backgroundColor))
                .clickable { onSelected() }
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = usecase.iconRes),
                        contentDescription = "Use case icon"
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    modifier = Modifier.padding(vertical = 15.dp),
                    text = stringResource(id = usecase.titleResId),
                    style = BodyCalloutRegular,
                    color = colorResource(textColor),
                    maxLines = 1
                )
            }
        }

        // Checkmark icon positioned outside the inner box
        if (isSelected) {
            Image(
                painter = painterResource(id = R.drawable.ic_onboarding_section_selected),
                contentDescription = "Selected",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp), // Offset to position outside the box
            )
        }
    }
}
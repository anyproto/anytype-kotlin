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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular

data class ProfessionItem(
    val emoji: String,
    val titleResId: Int
)

private val professionItems = listOf(
    ProfessionItem("✍️", R.string.onboarding_selection_writer),
    ProfessionItem("💻", R.string.onboarding_selection_developer),
    ProfessionItem("🎓", R.string.onboarding_selection_student),
    ProfessionItem("📈", R.string.onboarding_selection_marketer),
    ProfessionItem("👔", R.string.onboarding_selection_manager),
    ProfessionItem("🔬", R.string.onboarding_selection_researcher),
    ProfessionItem("🎯", R.string.onboarding_selection_designer),
    ProfessionItem("🎨", R.string.onboarding_selection_artist),
    ProfessionItem("🚀", R.string.onboarding_selection_entrepreneur),
    ProfessionItem("💼", R.string.onboarding_selection_consultant),
    ProfessionItem("💼", R.string.onboarding_selection_other),
)

@Composable
fun OnboardingSelectionScreen(
    isLoading: Boolean,
    onBackClicked: () -> Unit
) {
    var selectedProfession by remember { mutableStateOf<ProfessionItem?>(null) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top toolbar with back button - same pattern as SetEmailWrapper
        Image(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 9.dp)
                .noRippleClickable {
                    onBackClicked()
                },
            painter = painterResource(id = R.drawable.ic_back_onboarding_32),
            contentDescription = stringResource(R.string.content_description_back_button_icon)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Title and Description (Fixed at top)
            Spacer(modifier = Modifier.height(72.dp)) // Space for back button
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.onboarding_selection_title),
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitleSemibold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.onboarding_selection_description),
                style = UXBody,
                color = colorResource(id = R.color.text_secondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profession items in staggered grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                content = {
                    professionItems.forEach { profession ->
                        ProfessionSelectionItem(
                            profession = profession,
                            isSelected = selectedProfession == profession,
                            onSelected = { selectedProfession = profession }
                        )
                    }
                }
            )
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
                    //validateAndSubmit()
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
                loading = isLoading,
                enabled = selectedProfession != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            OnBoardingButtonSecondary(
                text = stringResource(id = R.string.onboarding_button_skip),
                onClick = {

                },
                size = ButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProfessionSelectionItem(
    profession: ProfessionItem,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val (textColor, backgroundColor) = if (isSelected) {
        R.color.text_primary to R.color.control_accent_125
    } else {
        R.color.text_secondary to R.color.shape_transparent_secondary
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = colorResource(backgroundColor))
            .clickable { onSelected() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = profession.emoji,
            style = BodyCalloutRegular,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(id = profession.titleResId),
            style = BodyCalloutRegular,
            color = colorResource(textColor),
            maxLines = 1
        )
    }
}

@DefaultPreviews
@Composable
private fun OnboardingSelectionScreenPreview() {
    OnboardingSelectionScreen(
        isLoading = false,
        onBackClicked = {}
    )
}

@DefaultPreviews
@Composable
private fun ProfessionSelectionItemPreview() {
    Column {
        ProfessionSelectionItem(
            profession = ProfessionItem("💻", R.string.onboarding_selection_developer),
            isSelected = false,
            onSelected = {}
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfessionSelectionItem(
            profession = ProfessionItem("🎨", R.string.onboarding_selection_artist),
            isSelected = true,
            onSelected = {}
        )
    }
}
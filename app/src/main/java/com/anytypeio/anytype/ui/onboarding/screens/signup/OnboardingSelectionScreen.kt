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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingLinkLarge

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterStart)
                    .noRippleClickable {
                        onBackClicked()
                    },
                painter = painterResource(id = R.drawable.ic_back_onboarding_32),
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
            Spacer(modifier = Modifier.height(24.dp))
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
                    //validateAndSubmit()
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
                loading = isLoading,
                enabled = selectedProfession != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonOnboardingLinkLarge(
                text = stringResource(id = R.string.onboarding_button_skip),
                onClick = {

                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
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
        R.color.text_primary to R.color.control_accent_25
    } else {
        R.color.text_secondary to R.color.shape_transparent_secondary
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = colorResource(backgroundColor))
            .clickable { onSelected() }
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
            .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profession.emoji,
                style = BodyCalloutRegular,
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            modifier = Modifier.padding(vertical = 15.dp),
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
    Column {
        //Spacer(modifier = Modifier.height(38.dp))
        OnboardingSelectionScreen(
            isLoading = false,
            onBackClicked = {}
        )
    }
}
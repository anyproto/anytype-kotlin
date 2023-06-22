package com.anytypeio.anytype.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ConditionLogin
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.TitleLogin
import com.anytypeio.anytype.ui.onboarding.OnboardingInput

@Composable
fun RecoveryScreenWrapper(
    backClick: () -> Unit,
    nextClick: (String) -> Unit,
    scanQrClick: () -> Unit
) {
    RecoveryScreen(
        backClick,
        nextClick,
        scanQrClick
    )
}

@Composable
fun RecoveryScreen(
    backClick: () -> Unit,
    nextClick: (String) -> Unit,
    scanQrClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 24.dp)
                .noRippleClickable {
                    backClick.invoke()
                },
            painter = painterResource(id = R.drawable.icon_back_onboarding),
            contentDescription = "back"
        )
        Text(
            modifier = Modifier.align(Alignment.TopCenter),
            text = stringResource(id = R.string.login),
            style = TitleLogin.copy(
                color = OnBoardingTextPrimaryColor
            )
        )

        val text = remember {
            mutableStateOf("")
        }
        LazyColumn(
            content = {
                item {
                    OnboardingInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                            .padding(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 18.dp),
                        text = text,
                        singleLine = false,
                        placeholder = stringResource(id = R.string.onboarding_type_recovery_phrase)
                    )
                }
                item {
                    OnBoardingButtonPrimary(
                        text = stringResource(id = R.string.next),
                        onClick = {
                            nextClick.invoke(text.value)
                        },
                        enabled = text.value.isNotEmpty(),
                        size = ButtonSize.Large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    )
                }
                item {
                    Text(
                        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.onboarding_login_or),
                        style = ConditionLogin.copy(
                            color = OnBoardingTextPrimaryColor
                        )
                    )
                }
                item {
                    OnBoardingButtonSecondary(
                        text = stringResource(id = R.string.or_scan_qr_code),
                        onClick = {
                            scanQrClick.invoke()
                        },
                        enabled = true,
                        size = ButtonSize.Large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    )
                }
            }
        )
    }
}
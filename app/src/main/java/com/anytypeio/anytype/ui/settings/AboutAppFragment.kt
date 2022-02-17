package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.AboutAppViewModel
import javax.inject.Inject


class AboutAppFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: AboutAppViewModel.Factory

    private val vm by viewModels<AboutAppViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { aboutAppScreen(vm) }
        }
    }

    override fun injectDependencies() {
        componentManager().aboutAppComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().aboutAppComponent.release()
    }
}

@Composable
fun aboutAppScreen(
    vm: AboutAppViewModel
) {

    Column {
        Box(
            modifier = Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier.size(
                    height = 4.dp,
                    width = 48.dp
                ).background(
                    color = colorResource(R.color.shape_primary),
                    shape = RoundedCornerShape(6.dp)
                )
            )
        }
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(
                top = 75.dp,
                bottom = 16.dp
            )
        ) {
            Text(
                text = stringResource(R.string.about),
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.app_version),
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.build_number),
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = BuildConfig.VERSION_CODE.toString(),
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.library),
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = vm.libraryVersion.collectAsState().value,
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 32.dp
            )
        ) {
            Box(modifier = Modifier.weight(1.0f, true)) {
                Text(
                    text = stringResource(R.string.user_id),
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary)
                )
            }
            Box(
                modifier = Modifier.weight(2.0f, true),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = vm.userId.collectAsState().value,
                    fontFamily = fonts,
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_primary)
                )
            }
        }
    }
}

val fonts = FontFamily(
    Font(R.font.inter_regular),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_medium, weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold)
)

//val     typography = Typography(
//    body1 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp
//    ),
//    h1 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Bold,
//        fontSize = 28.sp
//    ),
//    h2 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Bold,
//        fontSize = 22.sp
//    ),
//    h3 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Bold,
//        fontSize = 17.sp
//    )
//)
package com.anytypeio.anytype.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import com.anytypeio.anytype.ui_settings.about.AboutAppScreen
import com.anytypeio.anytype.ui_settings.about.AboutAppViewModel
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
            setContent {
                MaterialTheme(typography = typography) {
                    AboutAppScreen(
                        version = getVersionText(),
                        libraryVersion = vm.libraryVersion.collectAsState().value,
                        anytypeId = vm.userId.collectAsState().value,
                        onAnytypeIdClicked = { copyAnytypeIdToClipboard(vm.userId.value) }
                    )
                }
            }
        }
    }

    private fun getVersionText(): String {
        val version = BuildConfig.VERSION_NAME
        return if (version.isNotEmpty()) {
            if (BuildConfig.DEBUG)
                "$version-debug"
            else
                "$version-alpha"
        } else {
            resources.getString(R.string.unknown)
        }
    }

    private fun copyAnytypeIdToClipboard(id: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                ClipData.newPlainText(KeychainPhraseDialog.MNEMONIC_LABEL, id)
            clipboard.setPrimaryClip(clip)
            toast("Your Anytype ID is copied to clipboard.")
        } catch (e: Exception) {
            toast("Could not copy your Anytype ID. Please try again later, or copy it manually.")
        }
    }

    override fun injectDependencies() {
        componentManager().aboutAppComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().aboutAppComponent.release()
    }
}

val fonts = FontFamily(
    Font(R.font.inter_regular),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_medium, weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold)
)

val typography = Typography(
    body1 = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h1 = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    h2 = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    h3 = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    body2 = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    caption = TextStyle(
        fontFamily = fonts,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    )
)
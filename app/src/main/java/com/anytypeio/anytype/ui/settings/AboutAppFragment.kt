package com.anytypeio.anytype.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui_settings.about.AboutAppScreen
import com.anytypeio.anytype.ui_settings.about.AboutAppViewModel
import javax.inject.Inject
import timber.log.Timber


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
                        buildNumber = getBuildNumber(),
                        libraryVersion = vm.libraryVersion.collectAsState().value,
                        anytypeId = vm.userId.collectAsState().value,
                        onMetaClicked = { copyMetaToClipboard() }
                    ) {
                        vm.onExternalLinkClicked(it)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expand()
        subscribe(vm.navigation) {
            when (it) {
                is AboutAppViewModel.Navigation.OpenExternalLink -> {
                    proceedWithExternalLink(it)
                }
            }
        }
    }

    private fun proceedWithExternalLink(it: AboutAppViewModel.Navigation.OpenExternalLink) {
        val url = it.link.toExternalUrl()
        try {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }.let(::startActivity)
        } catch (e: Exception) {
            Timber.e(e, "Error while browsing url")
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

    private fun getBuildNumber() = BuildConfig.VERSION_CODE

    private fun copyMetaToClipboard() {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                ClipData.newPlainText(
                    "Your Anytype technical info",
                    getString(
                        R.string.about_meta_info_for_copy,
                        getDeviceName(),
                        Build.VERSION.SDK_INT,
                        getVersionText(),
                        getBuildNumber(),
                        vm.libraryVersion.value,
                        vm.userId.value
                    )
                )
            clipboard.setPrimaryClip(clip)
            toast("Technical info copied.")
        } catch (e: Exception) {
            toast("Could not copy your technical info. Please try again later, or copy it manually.")
        }
    }

    private fun AboutAppViewModel.ExternalLink.toExternalUrl() =
        when (this) {
            AboutAppViewModel.ExternalLink.AnytypeCommunity -> getString(R.string.about_anytype_community)
            AboutAppViewModel.ExternalLink.HelpAndTutorials -> getString(R.string.about_help_and_tutorials_link)
            AboutAppViewModel.ExternalLink.PrivacyPolicy -> getString(R.string.about_privacy_policy_link)
            AboutAppViewModel.ExternalLink.TermsOfUse -> getString(R.string.about_terms_of_use)
            AboutAppViewModel.ExternalLink.WhatIsNew -> getString(R.string.about_what_is_new_link)
        }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model
        } else "$model $manufacturer"
    }

    override fun injectDependencies() {
        componentManager().aboutAppComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().aboutAppComponent.release()
    }
}

val fonts = FontFamily(
    Font(R.font.inter_regular, weight = FontWeight.Normal),
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
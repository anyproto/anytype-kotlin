package com.anytypeio.anytype.ui.payments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.views.BaseTwoButtonsDarkThemeAlertDialog
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.screens.CodeScreen
import com.anytypeio.anytype.payments.screens.MainMembershipScreen
import com.anytypeio.anytype.payments.screens.WelcomeScreen
import com.anytypeio.anytype.payments.screens.TierViewScreen
import com.anytypeio.anytype.payments.viewmodel.MembershipErrorState
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.payments.viewmodel.MembershipNavigation
import com.anytypeio.anytype.payments.viewmodel.MembershipViewModel
import com.anytypeio.anytype.payments.viewmodel.MembershipViewModelFactory
import com.anytypeio.anytype.payments.viewmodel.TierAction
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

class MembershipFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MembershipViewModelFactory
    private val vm by viewModels<MembershipViewModel> { factory }
    private lateinit var navController: NavHostController

    private val argTierId get() = argOrNull<String>(ARG_TIER_ID)

    @Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.subscribe(vm.initBillingClient) { init ->
            if (init) {
                lifecycle.addObserver(billingClientLifecycle)
            }
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    val bottomSheetNavigator = rememberBottomSheetNavigator()
                    navController = rememberNavController(bottomSheetNavigator)
                    SetupNavigation(bottomSheetNavigator, navController)
                    ErrorScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle()
        when (val state = errorStateScreen.value) {
            MembershipErrorState.Hidden -> {
                //do nothing
            }
            is MembershipErrorState.Show -> {
                BaseTwoButtonsDarkThemeAlertDialog(
                    dialogText = state.message,
                    dismissButtonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                    actionButtonText = stringResource(id = R.string.membership_error_button_text_action),
                    onActionButtonClick = { vm.onTierAction(TierAction.ContactUsError(state.message)) },
                    onDismissButtonClick = { vm.hideError() },
                    onDismissRequest = { vm.hideError() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun SetupNavigation(
        bottomSheetNavigator: BottomSheetNavigator,
        navController: NavHostController
    ) {
        ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
            NavigationGraph(navController = navController)
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    private fun NavigationGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = MembershipNavigation.Main.route) {
            composable(MembershipNavigation.Main.route) {
                InitMainScreen()
            }
            bottomSheet(MembershipNavigation.Tier.route) {
                InitTierScreen()
            }
            bottomSheet(MembershipNavigation.Code.route) {
                InitCodeScreen()
            }
            bottomSheet(MembershipNavigation.Welcome.route) {
                InitWelcomeScreen()
            }
        }
    }

    @Composable
    private fun InitMainScreen() {
        skipCollapsed()
        expand()
        MainMembershipScreen(
            state = vm.viewState.collectAsStateWithLifecycle().value,
            tierClicked = vm::onTierClicked,
            tierAction = vm::onTierAction
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun InitTierScreen() {
        TierViewScreen(
            state = vm.tierState.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissTier,
            actionTier = vm::onTierAction,
            anyNameTextField = vm.anyNameState,
            anyEmailTextField = vm.anyEmailState
        )
    }

    @Composable
    private fun InitCodeScreen() {
        CodeScreen(
            state = vm.codeState.collectAsStateWithLifecycle().value,
            action = vm::onTierAction,
            onDismiss = vm::onDismissCode
        )
    }

    @Composable
    private fun InitWelcomeScreen() {
        WelcomeScreen(
            state = vm.welcomeState.collectAsStateWithLifecycle().value,
            onDismiss = vm::onDismissWelcome
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.showTierOnStart(tierId = argTierId)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
        subscribe(vm.navigation) { command ->
            Timber.d("MembershipFragment command: $command")
            when (command) {
                MembershipNavigation.Tier -> navController.navigate(MembershipNavigation.Tier.route)
                MembershipNavigation.Code -> navController.navigate(MembershipNavigation.Code.route)
                MembershipNavigation.Welcome -> {
                    navController.popBackStack(MembershipNavigation.Main.route, false)
                    navController.navigate(MembershipNavigation.Welcome.route)
                }
                MembershipNavigation.Dismiss -> navController.popBackStack()
                is MembershipNavigation.OpenUrl -> {
                    try {
                        if (command.url == null) {
                            toast("Url is null")
                            return@subscribe
                        }
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(command.url)
                        }.let {
                            startActivity(it)
                        }
                    } catch (e: Throwable) {
                        toast("Couldn't parse url: ${command.url}")
                    }
                }

                MembershipNavigation.Main -> {}
                is MembershipNavigation.OpenEmail -> {
                    val mail = resources.getString(R.string.payments_email_to)
                    val subject =
                        resources.getString(R.string.payments_email_subject, command.accountId)
                    val body = resources.getString(R.string.payments_email_body)
                    val mailBody = mail +
                            "?subject=$subject" +
                            "&body=$body"
                    proceedWithAction(SystemAction.MailTo(mailBody))
                }

                is MembershipNavigation.OpenErrorEmail -> {
                    val deviceModel = android.os.Build.MODEL
                    val osVersion = android.os.Build.VERSION.RELEASE
                    val appVersion = BuildConfig.VERSION_NAME
                    val currentDateTime: String
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    currentDateTime = sdf.format(Date())
                    val mail = resources.getString(R.string.membership_support_email)
                    val subject =
                        resources.getString(R.string.membership_support_subject, command.accountId)
                    val body = getString(
                        R.string.membership_support_body,
                        command.error, currentDateTime, deviceModel, osVersion, appVersion
                    )
                    val mailBody = mail +
                            "?subject=$subject" +
                            "&body=$body"
                    proceedWithAction(SystemAction.MailTo(mailBody))
                }
            }
        }
        subscribe(vm.launchBillingCommand) { event ->
            billingClientLifecycle.launchBillingFlow(
                activity = requireActivity(),
                params = event
            )
        }
    }

    override fun onDestroy() {
        lifecycle.removeObserver(billingClientLifecycle)
        super.onDestroy()
    }

    override fun injectDependencies() {
        componentManager().membershipComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().membershipComponent.release()
    }

    companion object {
        const val ARG_TIER_ID = "args.membership.tier"
        fun args(tierId: String?) = bundleOf(ARG_TIER_ID to tierId)
    }
}
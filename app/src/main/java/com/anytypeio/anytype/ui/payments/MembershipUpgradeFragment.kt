package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.payments.screens.MembershipUpgradeScreen
import com.anytypeio.anytype.presentation.membership.MembershipUpgradeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject

class MembershipUpgradeFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MembershipUpgradeViewModel.Factory
    private val vm by viewModels<MembershipUpgradeViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            dialog?.setOnShowListener { dg ->
                val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.setBackgroundColor(requireContext().color(android.R.color.transparent))
            }
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MembershipUpgradeScreen(
                    onDismiss = { },
                    onButtonClicked = vm::onContactButtonClicked
                )
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when (command) {
                            is MembershipUpgradeViewModel.Command.ShowEmail -> {
                                proceedWithEmailCreate(command.account)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithEmailCreate(accountId: Id) {
        val mail = resources.getString(R.string.payments_email_to)
        val subject = resources.getString(R.string.payments_email_subject, accountId)
        val body = resources.getString(R.string.payments_email_body)
        val mailBody = mail +
                "?subject=$subject" +
                "&body=$body"
        proceedWithAction(SystemAction.MailTo(mailBody))
    }

    override fun injectDependencies() {
        componentManager().membershipUpgradeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().membershipUpgradeComponent.release()
    }
}
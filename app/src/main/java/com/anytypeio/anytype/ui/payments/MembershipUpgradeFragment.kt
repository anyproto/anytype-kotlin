package com.anytypeio.anytype.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.payments.screens.MembershipUpgradeScreen

class MembershipUpgradeFragment : BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(context = requireContext(), dialog = requireDialog()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MembershipUpgradeScreen(
                    onDismiss = { },
                    onButtonClicked = { proceedWithButtonClick() }
                )
            }
        }
    }

    private fun proceedWithButtonClick() {
//        val mail = resources.getString(R.string.payments_email_to)
//        val subject = resources.getString(R.string.payments_email_subject, command.accountId)
//        val body = resources.getString(R.string.payments_email_body)
//        val mailBody = mail +
//                "?subject=$subject" +
//                "&body=$body"
//        proceedWithAction(SystemAction.MailTo(mailBody))
    }

    override fun injectDependencies() {
        //componentManager().membershipUpgradeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        //componentManager().membershipUpgradeComponent.release()
    }
}
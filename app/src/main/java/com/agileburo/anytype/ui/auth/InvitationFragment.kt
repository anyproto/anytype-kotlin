package com.agileburo.anytype.ui.auth

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.toast
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_invitation.*

class InvitationFragment : NavigationFragment(R.layout.fragment_invitation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnConfirm.setOnClickListener {
            if (edtCode.text.isNullOrEmpty()) {
                requireActivity().toast(
                    msg = getString(R.string.code_empty),
                    gravity = Gravity.TOP,
                    duration = Toast.LENGTH_SHORT
                )
            } else {
                navObserver.onChanged(
                    EventWrapper(
                        AppNavigation.Command.OpenCreateAccount(
                            invitationCode = edtCode.text.toString()
                        )
                    )
                )
            }
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}
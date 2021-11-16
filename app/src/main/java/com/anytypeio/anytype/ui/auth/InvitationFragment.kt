package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.ui.base.NavigationFragment
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
        btnBack.setOnClickListener {
            navObserver.onChanged(EventWrapper(AppNavigation.Command.Exit))
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}
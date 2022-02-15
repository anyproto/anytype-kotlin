package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.databinding.FragmentInvitationBinding
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.ui.base.NavigationFragment

class InvitationFragment : NavigationFragment<FragmentInvitationBinding>(R.layout.fragment_invitation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm.setOnClickListener {
            if (binding.edtCode.text.isNullOrEmpty()) {
                requireActivity().toast(
                    msg = getString(R.string.code_empty),
                    gravity = Gravity.TOP,
                    duration = Toast.LENGTH_SHORT
                )
            } else {
                navObserver.onChanged(
                    EventWrapper(
                        AppNavigation.Command.OpenCreateAccount(
                            invitationCode = binding.edtCode.text.toString()
                        )
                    )
                )
            }
        }
        binding.btnBack.setOnClickListener {
            navObserver.onChanged(EventWrapper(AppNavigation.Command.Exit))
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInvitationBinding = FragmentInvitationBinding.inflate(
        inflater, container, false
    )
}
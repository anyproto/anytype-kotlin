package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.invitationScreenShow
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_ui.extensions.toast
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.databinding.FragmentInvitationBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.ui.base.NavigationFragment
import javax.inject.Inject

class InvitationFragment : NavigationFragment<FragmentInvitationBinding>(R.layout.fragment_invitation) {

    @Inject
    lateinit var analytics: Analytics

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.sendEvent(
            analytics = analytics,
            eventName = invitationScreenShow
        )
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

    override fun injectDependencies() {
        componentManager().authComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().authComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInvitationBinding = FragmentInvitationBinding.inflate(
        inflater, container, false
    )
}
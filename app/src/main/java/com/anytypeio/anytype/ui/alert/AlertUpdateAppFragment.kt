package com.anytypeio.anytype.ui.alert

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentAlertBinding
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener

class AlertUpdateAppFragment : BaseBottomSheetFragment<FragmentAlertBinding>() {

    companion object {
        const val DOWNLOAD_ANYTYPE_URL = "https://download.anytype.io/"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.later.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onExitToDesktopClicked()
            dismiss()
        }
        binding.update.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(DOWNLOAD_ANYTYPE_URL)
            )
            startActivity(intent)
        }
    }

    override fun injectDependencies() {
        // Do nothing
    }

    override fun releaseDependencies() {
        // Do nothing
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAlertBinding = FragmentAlertBinding.inflate(
        inflater, container, false
    )
}
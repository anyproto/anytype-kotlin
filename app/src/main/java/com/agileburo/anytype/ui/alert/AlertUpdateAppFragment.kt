package com.agileburo.anytype.ui.alert

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_alert.*

class AlertUpdateAppFragment : BaseBottomSheetFragment() {

    companion object {
        const val TG_PACKAGE = "org.telegram.messenger"
        const val TG_WEB_PACKAGE = "org.thunderdog.challegram"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        later.setOnClickListener {
            (parentFragment as? OnFragmentInteractionListener)?.onExitToDesktopClicked()
            dismiss()
        }
        update.setOnClickListener {
            val intent = telegramIntent(requireContext())
            startActivity(intent)
        }
    }

    private fun telegramIntent(context: Context): Intent =
        try {
            try {
                context.packageManager.getPackageInfo(TG_PACKAGE, 0)
            } catch (e: Exception) {
                context.packageManager.getPackageInfo(TG_WEB_PACKAGE, 0)
            }
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_app)))
        } catch (e: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_web)))
        }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}
}
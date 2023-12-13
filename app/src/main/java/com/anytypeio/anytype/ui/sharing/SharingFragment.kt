package com.anytypeio.anytype.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class SharingFragment : BaseBottomSheetComposeFragment() {

    private val sharedData get() = arg<String>(SHARING_DATE_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                AddToAnytypeScreen(sharedData)
            }
        }
    }

    companion object {
        private const val SHARING_DATE_KEY = "arg.sharing.data-key"
        fun new(data: String) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_DATE_KEY to data)
        }
    }
}
package com.anytypeio.anytype.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SharingFragment : BaseBottomSheetComposeFragment() {

    private val sharedData get() = arg<String>(SHARING_DATE_KEY)

    @Inject
    lateinit var factory: AddToAnytypeViewModel.Factory

    private val vm by viewModels<AddToAnytypeViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            vm
            MaterialTheme(
                typography = typography
            ) {
                AddToAnytypeScreen(sharedData)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().addToAnytypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addToAnytypeComponent.release()
    }

    companion object {
        private const val SHARING_DATE_KEY = "arg.sharing.data-key"
        fun new(data: String) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_DATE_KEY to data)
        }
    }
}
package com.anytypeio.anytype.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SharingFragment : BaseBottomSheetComposeFragment() {

    private val sharedData : SharingData get() {
        val result = arg<String>(SHARING_DATE_KEY)
        return if (URLUtil.isValidUrl(result)) {
            SharingData.Url(result)
        } else {
            SharingData.Raw(result)
        }
    }

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
            MaterialTheme(
                typography = typography
            ) {
                AddToAnytypeScreen(
                    data = sharedData,
                    onDoneClicked = { option ->
                        when(option) {
                            SAVE_AS_BOOKMARK -> vm.onCreateBookmark(url = sharedData.data)
                            SAVE_AS_NOTE -> vm.onCreateNote(sharedData.data)
                        }
                    },
                    onCancelClicked = {
                        dismiss()
                    }
                )
                LaunchedEffect(Unit) {
                    vm.navigation.collect { nav ->
                        when(nav) {
                            is OpenObjectNavigation.OpenDataView -> {

                            }
                            is OpenObjectNavigation.OpenEditor -> {
                                dismiss()
                                findNavController().navigate(
                                    R.id.objectNavigation,
                                    bundleOf(
                                        EditorFragment.ID_KEY to nav.target
                                    )
                                )
                            }
                            is OpenObjectNavigation.UnexpectedLayoutError -> {

                            }
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    vm.toasts.collect { toast ->
                        toast(toast)
                    }
                }
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
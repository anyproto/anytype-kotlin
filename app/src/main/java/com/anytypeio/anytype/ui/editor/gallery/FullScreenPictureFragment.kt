package com.anytypeio.anytype.ui.editor.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentFullScreenPictureBinding
import coil3.load

@Deprecated("Use MediaActivity")
class FullScreenPictureFragment : BaseFragment<FragmentFullScreenPictureBinding>(R.layout.fragment_full_screen_picture) {

    private val url: String
        get() = requireArguments().getString(ARG_URL_KEY) ?: throw IllegalStateException()

    private val ignoreRootWindowInsets
        get() = argOrNull<Boolean>(ARG_IGNORE_INSETS_KEY)


    companion object {

        fun new(target: String, url: String) = FullScreenPictureFragment().apply {
            arguments = bundleOf(
                ARG_URL_KEY to url,
                ARG_TARGET_KEY to target
            )
        }

        fun args(
            url: String,
            ignoreRootWindowInsets: Boolean = false
        ) : Bundle = bundleOf(ARG_URL_KEY to url, ARG_IGNORE_INSETS_KEY to ignoreRootWindowInsets)

        const val ARG_URL_KEY = "arg.full_screen_picture.url"
        private const val ARG_TARGET_KEY = "arg.full_screen_picture.target"
        private const val ARG_IGNORE_INSETS_KEY = "arg.full_screen_picture.ignore-insets"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.picture.load(url)
        binding.picture.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onApplyWindowRootInsets() {
        if (ignoreRootWindowInsets != true) {
            super.onApplyWindowRootInsets()
        } else {
            // DO nothing.
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFullScreenPictureBinding = FragmentFullScreenPictureBinding.inflate(
        inflater, container, false
    )
}
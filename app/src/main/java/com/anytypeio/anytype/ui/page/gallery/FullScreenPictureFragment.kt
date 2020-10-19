package com.anytypeio.anytype.ui.page.gallery

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_full_screen_picture.*

class FullScreenPictureFragment : BaseFragment(R.layout.fragment_full_screen_picture) {

    private val url: String
        get() = requireArguments().getString(ARG_URL_KEY) ?: throw IllegalStateException()

    companion object {

        fun new(target: String, url: String) = FullScreenPictureFragment().apply {
            arguments = bundleOf(
                ARG_URL_KEY to url,
                ARG_TARGET_KEY to target
            )
        }

        const val ARG_URL_KEY = "arg.full_screen_picture.url"
        const val ARG_TARGET_KEY = "arg.full_screen_picture.target"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(picture).load(url).into(picture)
        picture.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}
package com.anytypeio.anytype.ui.editor.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentFullScreenPictureBinding
import com.bumptech.glide.Glide

class FullScreenPictureFragment : BaseFragment<FragmentFullScreenPictureBinding>(R.layout.fragment_full_screen_picture) {

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
        Glide.with(binding.picture).load(url).into(binding.picture)
        binding.picture.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFullScreenPictureBinding = FragmentFullScreenPictureBinding.inflate(
        inflater, container, false
    )
}
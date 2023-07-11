package com.anytypeio.anytype.sample.design_system

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.sample.R
import com.anytypeio.anytype.sample.databinding.FragmentContentStyleMultilineBinding

class ContentStyleMultiline: Fragment(R.layout.fragment_content_style_multiline) {

    private var fragmentBinding: FragmentContentStyleMultilineBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val _binding = FragmentContentStyleMultilineBinding.bind(view)
        fragmentBinding = _binding
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentBinding = null
    }
}
package com.agileburo.anytype.core_ui.features.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.agileburo.anytype.core_ui.R

class PageNavigationLinksFragment : Fragment(R.layout.fragment_navigation_links) {

    companion object {

        val ARG_TYPE = "arg.navigation.fragment.type"

        fun newInstance(type: Int): PageNavigationLinksFragment =
            PageNavigationLinksFragment()
                .apply {
                    val bundle = Bundle().apply {
                        putInt(ARG_TYPE, type)
                    }
                    arguments = bundle
                }
    }

}
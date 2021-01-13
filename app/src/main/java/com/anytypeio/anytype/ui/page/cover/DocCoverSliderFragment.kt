package com.anytypeio.anytype.ui.page.cover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.domain.common.Id
import kotlinx.android.synthetic.main.fragment_doc_cover_slider.*

class DocCoverSliderFragment : BaseBottomSheetFragment() {

    val ctx get() = arg<String>(CTX_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_doc_cover_slider, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager.adapter = SliderAdapter(this)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(ctx: Id): DocCoverSliderFragment = DocCoverSliderFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.doc-cover-slider.ctx"
    }

    private inner class SliderAdapter(
        fr: DocCoverSliderFragment
    ) : FragmentStateAdapter(fr) {

        val ctx = fr.ctx

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DocCoverGalleryFragment.new(ctx)
                1 -> DocCoverGalleryFragment.new(ctx)
                else -> throw IllegalStateException()
            }
        }
    }
}
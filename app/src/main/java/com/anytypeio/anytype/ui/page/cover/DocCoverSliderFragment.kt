package com.anytypeio.anytype.ui.page.cover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_models.Id
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_doc_cover_slider.*

class DocCoverSliderFragment : BaseBottomSheetFragment(), DocCoverAction {

    val ctx get() = arg<String>(CTX_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_doc_cover_slider, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager.adapter = SliderAdapter(this)
        TabLayoutMediator(tabs, pager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_gallery)
                1 -> tab.text = getString(R.string.tab_upload_image)
            }
        }.attach()
        lifecycleScope.subscribe(btnRemove.clicks()) { onRemoveCover() }
    }

    override fun onResume() {
        super.onResume()
        expand()
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(ctx: Id): DocCoverSliderFragment = DocCoverSliderFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.doc-cover-slider.ctx"
    }

    override fun onImagePicked(path: String) {
        withParent<DocCoverAction> { onImagePicked(path) }.also { dismiss() }
    }

    override fun onImageSelected(hash: String) {
        withParent<DocCoverAction> { onImageSelected(hash) }.also { dismiss() }
    }

    override fun onRemoveCover() {
        withParent<DocCoverAction> { onRemoveCover().also { dismiss() } }
    }

    private inner class SliderAdapter(
        fr: DocCoverSliderFragment
    ) : FragmentStateAdapter(fr) {

        val ctx = fr.ctx

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DocCoverGalleryFragment.new(ctx)
                1 -> UploadCoverImageFragment.new(ctx)
                else -> throw IllegalStateException()
            }
        }
    }
}

interface DocCoverAction {
    fun onImagePicked(path: String)
    fun onImageSelected(hash: String)
    fun onRemoveCover()
}
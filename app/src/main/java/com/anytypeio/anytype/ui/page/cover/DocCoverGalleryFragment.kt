package com.anytypeio.anytype.ui.page.cover

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.page.modal.DocCoverGalleryAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.page.cover.SelectDocCoverViewModel
import kotlinx.android.synthetic.main.fragment_doc_cover_gallery.*
import javax.inject.Inject

class DocCoverGalleryFragment : BaseFragment(R.layout.fragment_doc_cover_gallery) {

    @Inject
    lateinit var factory: SelectDocCoverViewModel.Factory

    private val vm by viewModels<SelectDocCoverViewModel> { factory }

    private val ctx: String get() = arg(CTX_KEY)

    private val docCoverGalleryAdapter by lazy {
        DocCoverGalleryAdapter(
            onSolidColorClicked = { vm.onSolidColorSelected(color = it, ctx = ctx) },
            onGradientClicked = { vm.onGradientColorSelected(gradient = it, ctx = ctx) },
            onImageClicked = { withParent<DocCoverAction> { onImageSelected(it) } }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spacing = requireContext().dimen(R.dimen.cover_gallery_item_spacing).toInt() / 2

        docCoverGalleryRecycler.apply {
            adapter = docCoverGalleryAdapter
            layoutManager = GridLayoutManager(context, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (docCoverGalleryAdapter.getItemViewType(position)) {
                            R.layout.item_doc_cover_gallery_header -> SPAN_COUNT
                            else -> 1
                        }
                    }
                }
            }
            addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        when (parent.findViewHolderForLayoutPosition(position)) {
                            is DocCoverGalleryAdapter.ViewHolder.Header -> {
                                outRect.top = spacing * 4
                                outRect.left = spacing
                                outRect.bottom = 0
                            }
                            else -> {
                                outRect.left = spacing
                                outRect.right = spacing
                                outRect.top = spacing * 2
                                val total = parent.adapter?.itemCount ?: 0
                                if (position >= total - 1)
                                    outRect.bottom = spacing * 4
                                else
                                    outRect.bottom = 0
                            }
                        }
                    }
                }
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.views) { docCoverGalleryAdapter.views = it }
        lifecycleScope.subscribe(vm.isDismissed) {
            if (it) findNavController().popBackStack()
        }
    }

    override fun injectDependencies() {
        componentManager().docCoverGalleryComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().docCoverGalleryComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id): DocCoverGalleryFragment = DocCoverGalleryFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.doc-cover-galler.ctx"
        const val SPAN_COUNT = 3
    }
}
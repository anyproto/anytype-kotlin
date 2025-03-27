package com.anytypeio.anytype.ui.dashboard

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.wallpaper.WallpaperSelectAdapter
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentWallpaperSelectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.wallpaper.WallpaperSelectViewModel
import javax.inject.Inject

class WallpaperSelectFragment : BaseBottomSheetFragment<FragmentWallpaperSelectBinding>() {

    @Inject
    lateinit var factory: WallpaperSelectViewModel.Factory

    private val vm by viewModels<WallpaperSelectViewModel> { factory }

    private val wallpaperSelectAdapter by lazy {
        WallpaperSelectAdapter { vm.onWallpaperSelected(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spacing = requireContext().dimen(R.dimen.cover_gallery_item_spacing).toInt()
        binding.wallpaperRecycler.apply {
            adapter = wallpaperSelectAdapter
            layoutManager = GridLayoutManager(context, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (wallpaperSelectAdapter.getItemViewType(position)) {
                            R.layout.item_wallpaper_select_section -> 3
                            else -> 1
                        }
                    }
                }
            }
            addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        val holder = parent.findViewHolderForLayoutPosition(position)
                        if (holder !is WallpaperSelectAdapter.SectionViewHolder) {
                            outRect.left = spacing
                            outRect.right = spacing
                            outRect.top = spacing * 2
                            outRect.bottom = 0
                        }
                        // Drawing space at the bottom of the two last wallpapers. TODO refactor
                        if (position == state.itemCount - 3) {
                            outRect.bottom = spacing * 2
                        }
                        if (position == state.itemCount - 2) {
                            outRect.bottom = spacing * 2
                        }
                        if (position == state.itemCount - 1) {
                            outRect.bottom = spacing * 2
                        }
                    }
                }
            )
        }
        with(lifecycleScope) {
            subscribe(vm.state) { wallpaperSelectAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        }

        skipCollapsed()
        expand()

    }

    override fun injectDependencies() {
        componentManager().wallpaperSelectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().wallpaperSelectComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWallpaperSelectBinding = FragmentWallpaperSelectBinding.inflate(
        inflater, container, false
    )
}
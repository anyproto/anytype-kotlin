package com.anytypeio.anytype.ui.editor.cover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.objects.CoverSliderBaseViewModel
import com.anytypeio.anytype.presentation.objects.CoverSliderObjectSetViewModel
import com.anytypeio.anytype.presentation.objects.CoverSliderObjectViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_doc_cover_slider.*
import javax.inject.Inject

abstract class CoverSliderBaseFragment : BaseBottomSheetFragment(), DocCoverAction {

    abstract val ctx: String

    abstract val vm: CoverSliderBaseViewModel

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

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) findNavController().popBackStack()
            }
        }
        super.onStart()
    }

    override fun onColorPicked(color: CoverColor) {
        vm.onSolidColorSelected(ctx = ctx, color = color)
    }

    override fun onGradientPicked(gradient: String) {
        vm.onGradientColorSelected(ctx = ctx, gradient = gradient)
    }

    override fun onImagePicked(path: String) {
        vm.onImagePicked(ctx = ctx, path = path)
    }

    override fun onImageSelected(hash: String) {
        vm.onImageSelected(ctx = ctx, hash = hash)
    }

    override fun onRemoveCover() {
        vm.onRemoveCover(ctx = ctx)
    }

    private inner class SliderAdapter(
        fr: CoverSliderBaseFragment
    ) : FragmentStateAdapter(fr) {

        val ctx = fr.ctx

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SelectCoverGalleryFragment.new(ctx)
                1 -> UploadCoverImageFragment()
                else -> throw IllegalStateException()
            }
        }
    }
}

class CoverSliderObjectFragment : CoverSliderBaseFragment() {

    override val ctx get() = arg<String>(CTX_KEY)

    @Inject
    lateinit var factory: CoverSliderObjectViewModel.Factory
    override val vm by viewModels<CoverSliderObjectViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectCoverSliderComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectCoverSliderComponent.release(ctx)
    }

    companion object {
        const val CTX_KEY = "arg.object-cover-slider.ctx"

        fun new(ctx: Id) = CoverSliderObjectFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }
    }
}

class CoverSliderObjectSetFragment : CoverSliderBaseFragment() {

    override val ctx get() = arg<String>(CTX_KEY)

    @Inject
    lateinit var factory: CoverSliderObjectSetViewModel.Factory
    override val vm by viewModels<CoverSliderObjectSetViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectSetCoverSliderComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetCoverSliderComponent.release(ctx)
    }

    companion object {
        const val CTX_KEY = "arg.object-set-cover-slider.ctx"

        fun new(ctx: Id) = CoverSliderObjectSetFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }
    }
}

interface DocCoverAction {
    fun onColorPicked(color: CoverColor)
    fun onGradientPicked(gradient: String)
    fun onImagePicked(path: String)
    fun onImageSelected(hash: String)
    fun onRemoveCover()
}
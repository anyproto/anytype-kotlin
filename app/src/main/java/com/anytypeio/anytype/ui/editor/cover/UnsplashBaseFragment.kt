package com.anytypeio.anytype.ui.editor.cover

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.cover.UnsplashImageAdapter
import com.anytypeio.anytype.core_ui.features.editor.modal.DocCoverGalleryAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentUnsplashBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.cover.UnsplashViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class UnsplashBaseFragment : BaseBottomSheetTextInputFragment<FragmentUnsplashBinding>() {

    val ctx get() = arg<String>(CTX_KEY)

    override val textInput: EditText get() = binding.searchToolbar.binding.filterInputField

    @Inject
    lateinit var factory: UnsplashViewModel.Factory

    private val vm by viewModels<UnsplashViewModel> { factory }

    private val unsplashImageAdapter by lazy {
        UnsplashImageAdapter(
            onImageClicked = { img ->
                vm.onImageSelected(ctx = ctx, img = img)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spacing = requireContext().dimen(R.dimen.cover_gallery_item_spacing).toInt()
        binding.unsplashRecycler.apply {
            adapter = unsplashImageAdapter
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(
                object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        val position = parent.getChildAdapterPosition(view)
                        val holder = parent.findViewHolderForLayoutPosition(position)
                        if (holder !is DocCoverGalleryAdapter.ViewHolder.Header) {
                            outRect.left = spacing
                            outRect.right = spacing
                            outRect.top = spacing * 2
                            outRect.bottom = 0
                        }
                    }
                }
            )
        }
        binding.searchToolbar.binding.filterInputField.doAfterTextChanged {
            vm.onQueryChanged(it.toString())
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.isCompleted.collect { isCompleted ->
                        if (isCompleted) onCompleted()
                    }
                }
                launch {
                    vm.isFailed.collect { isFailed ->
                        if (isFailed) binding.tvError.visible() else binding.tvError.gone()
                    }
                }
                launch {
                    vm.images.collect { unsplashImageAdapter.submitList(it) }
                }
                launch {
                    vm.isLoading.collect { isLoading ->
                        if (isLoading)
                            binding.searchToolbar.binding.progressBar.visible()
                        else
                            binding.searchToolbar.binding.progressBar.invisible()
                    }
                }
            }
        }
    }

    abstract fun onCompleted()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUnsplashBinding = FragmentUnsplashBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.object.cover.unsplash.ctx"
    }
}

class ObjectUnsplashFragment : UnsplashBaseFragment() {
    override fun injectDependencies() {
        componentManager().objectUnsplashComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectUnsplashComponent.release(ctx)
    }

    override fun onCompleted() {
        findNavController().popBackStack(
            R.id.pageScreen,
            false
        )
    }
}

class ObjectSetUnsplashFragment : UnsplashBaseFragment() {

    override fun onCompleted() {
        findNavController().popBackStack(
            R.id.objectSetScreen,
            false
        )
    }

    override fun injectDependencies() {
        componentManager().objectSetUnsplashComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetUnsplashComponent.release(ctx)
    }
}
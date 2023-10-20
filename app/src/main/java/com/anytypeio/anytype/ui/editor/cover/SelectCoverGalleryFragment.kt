package com.anytypeio.anytype.ui.editor.cover

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.editor.modal.DocCoverGalleryAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.GetImageContract
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentDocCoverGalleryBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectSetViewModel
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectViewModel
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

abstract class SelectCoverGalleryFragment :
    BaseBottomSheetFragment<FragmentDocCoverGalleryBinding>() {

    abstract val ctx: String
    abstract val vm: SelectCoverViewModel

    private val docCoverGalleryAdapter by lazy {
        DocCoverGalleryAdapter(
            onSolidColorClicked = { color -> vm.onSolidColorSelected(ctx, color) },
            onGradientClicked = { gradient -> vm.onGradientColorSelected(ctx, gradient) },
            onImageClicked = { hash -> vm.onImageSelected(ctx, hash) }
        )
    }

    private val getContent = try { getContentLauncher() } catch (e: Exception) {
        null
    }

    private fun getContentLauncher() = registerForActivityResult(GetImageContract()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val path = uri.parseImagePath(requireContext())
                    vm.onImagePicked(ctx, path)
                } catch (e: Exception) {
                    toast("Error while parsing path for cover image")
                    Timber.d(e, "Error while parsing path for cover image")
                }
            } else {
                Timber.e("Error while upload cover image, URI is null")
            }
    }

    private val permissionReadStorage = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = grantResults[Manifest.permission.READ_EXTERNAL_STORAGE]
            if (readResult == true) {
                openGallery()
            } else {
                binding.root.showSnackbar(R.string.permission_read_denied, Snackbar.LENGTH_SHORT)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRemove.clicks()
            .onEach { vm.onRemoveCover(ctx) }
            .launchIn(lifecycleScope)

        binding.btnUnsplash.clicks()
            .onEach {
                findNavController().navigate(
                    R.id.objectCoverUnsplashScreen,
                    bundleOf(
                        UnsplashBaseFragment.CTX_KEY to ctx
                    )
                )
            }
            .launchIn(lifecycleScope)

        binding.btnUpload.clicks()
            .onEach { proceedWithImagePick() }
            .launchIn(lifecycleScope)

        val spacing = requireContext().dimen(R.dimen.cover_gallery_item_spacing).toInt()

        binding.docCoverGalleryRecycler.apply {
            adapter = docCoverGalleryAdapter
            layoutManager = GridLayoutManager(context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (docCoverGalleryAdapter.getItemViewType(position)) {
                            R.layout.item_doc_cover_gallery_header -> 2
                            else -> 1
                        }
                    }
                }
            }
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

        skipCollapsed()
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.views) { docCoverGalleryAdapter.views = it }
            jobs += subscribe(vm.isDismissed) { if (it) findNavController().popBackStack() }
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.isLoading) { isLoading ->
                //todo Add progress bar
            }
        }
        super.onStart()
        expand()
    }

    private fun proceedWithImagePick() {
        if (!hasReadStoragePermission())
            permissionReadStorage.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        else
            openGallery()
    }

    private fun openGallery() {
        try {
            getContent?.launch(SELECT_IMAGE_CODE)
        } catch (e: Exception) {
            Timber.e(e, "Error while opening gallery")
            toast("Error while opening gallery: ${e.message}")
        }
    }

    private fun hasReadStoragePermission(): Boolean = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ).let { result ->
        result == PackageManager.PERMISSION_GRANTED
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDocCoverGalleryBinding = FragmentDocCoverGalleryBinding.inflate(
        inflater, container, false
    )

    abstract fun onUnsplashClicked()

    companion object {
        private const val SELECT_IMAGE_CODE = 1
        private const val REQUEST_PERMISSION_CODE = 2
    }
}

class SelectCoverObjectFragment : SelectCoverGalleryFragment() {

    override val ctx get() = arg<String>(CTX_KEY)

    @Inject
    lateinit var factory: SelectCoverObjectViewModel.Factory
    override val vm by viewModels<SelectCoverObjectViewModel> { factory }

    override fun onUnsplashClicked() {
        findNavController().navigate(
            R.id.objectCoverUnsplashScreen,
            bundleOf(
                UnsplashBaseFragment.CTX_KEY to ctx
            )
        )
    }

    override fun injectDependencies() {
        componentManager().objectCoverComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectCoverComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id) = SelectCoverObjectFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.object-cover-gallery.ctx"
    }
}

class SelectCoverObjectSetFragment : SelectCoverGalleryFragment() {

    override val ctx get() = arg<String>(CTX_KEY)

    @Inject
    lateinit var factory: SelectCoverObjectSetViewModel.Factory
    override val vm by viewModels<SelectCoverObjectSetViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectSetCoverComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetCoverComponent.release(ctx)
    }

    override fun onUnsplashClicked() {
        findNavController().navigate(
            R.id.objectCoverUnsplashScreen,
            bundleOf(
                UnsplashBaseFragment.CTX_KEY to ctx
            )
        )
    }

    companion object {
        fun new(ctx: Id) = SelectCoverObjectSetFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.object-set-cover-gallery.ctx"
    }
}
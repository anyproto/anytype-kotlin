package com.anytypeio.anytype.ui.editor.cover

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
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
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentDocCoverGalleryBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectSetViewModel
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverObjectViewModel
import com.anytypeio.anytype.presentation.editor.cover.SelectCoverViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

abstract class SelectCoverGalleryFragment : BaseBottomSheetFragment<FragmentDocCoverGalleryBinding>() {

    abstract val ctx: String
    abstract val vm: SelectCoverViewModel

    private val docCoverGalleryAdapter by lazy {
        DocCoverGalleryAdapter(
            onSolidColorClicked = { color -> vm.onSolidColorSelected(ctx, color) },
            onGradientClicked = { gradient -> vm.onGradientColorSelected(ctx, gradient) },
            onImageClicked = { hash -> vm.onImageSelected(ctx, hash) }
        )
    }

    val getContent = registerForActivityResult(GetImageContract()) { uri: Uri? ->
        if (uri != null) {
            try {
                val path = uri.parsePath(requireContext())
                vm.onImagePicked(ctx, path)
            } catch (e: Exception) {
                toast("Error while parsing path for cover image")
                Timber.d(e, "Error while parsing path for cover image")
            }
        } else {
            toast("Error while upload cover image, URI is null")
            Timber.e("Error while upload cover image, URI is null")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRemove.clicks()
            .onEach { vm.onRemoveCover(ctx) }
            .launchIn(lifecycleScope)

        binding.btnUpload.clicks()
            .onEach { proceedWithImagePick() }
            .launchIn(lifecycleScope)

        val spacing = requireContext().dimen(R.dimen.cover_gallery_item_spacing).toInt() / 2

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
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
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
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.views) { docCoverGalleryAdapter.views = it }
            jobs += subscribe(vm.isDismissed) { if (it) findNavController().popBackStack() }
            jobs += subscribe(vm.toasts) { toast(it) }
        }
        super.onStart()
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission())
            requestExternalStoragePermission()
        else
            openGallery()
    }

    private fun requestExternalStoragePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun openGallery() {
        getContent.launch(SELECT_IMAGE_CODE)
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDocCoverGalleryBinding = FragmentDocCoverGalleryBinding.inflate(
        inflater, container, false
    )

    companion object {
        private const val SELECT_IMAGE_CODE = 1
        private const val REQUEST_PERMISSION_CODE = 2
    }
}

class GetImageContract : ActivityResultContract<Int, Uri?>() {
    override fun createIntent(context: Context, input: Int?): Intent {
        return Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.data
        }
        return null
    }
}

class SelectCoverObjectFragment : SelectCoverGalleryFragment() {

    override val ctx get() = arg<String>(CTX_KEY)

    @Inject
    lateinit var factory: SelectCoverObjectViewModel.Factory
    override val vm by viewModels<SelectCoverObjectViewModel> { factory }

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

    companion object {
        fun new(ctx: Id) = SelectCoverObjectSetFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.object-set-cover-gallery.ctx"
    }
}
package com.anytypeio.anytype.ui.page.cover

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.parsePath
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.common.Id
import kotlinx.android.synthetic.main.fragment_upload_cover_image.*
import timber.log.Timber

class UploadCoverImageFragment : BaseFragment(R.layout.fragment_upload_cover_image) {

    private val ctx get() = arg<String>(CTX_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.subscribe(btnChooseImage.clicks()) {
            proceedWithImagePick()
        }
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

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_IMAGE_CODE) {
            data?.data?.let { uri ->
                try {
                    val path = uri.parsePath(requireContext())
                    withParent<DocCoverAction> { onImagePicked(path) }
                } catch (e: Exception) {
                    Timber.d(e, "Error while parsing path for cover image")
                }
            }
        }
    }

    private fun openGallery() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        ).let { intent ->
            startActivityForResult(intent, SELECT_IMAGE_CODE)
        }
    }

    override fun injectDependencies() {
        componentManager().uploadDocCoverImageComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().uploadDocCoverImageComponent.release(ctx)
    }

    companion object {

        fun new(ctx: Id): UploadCoverImageFragment = UploadCoverImageFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        private const val SELECT_IMAGE_CODE = 1
        private const val REQUEST_PERMISSION_CODE = 2

        const val CTX_KEY = "arg.upload-cover-image.ctx"
    }
}
package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentRelationFileValueActionBinding

class FileActionsFragment : BaseBottomSheetFragment<FragmentRelationFileValueActionBinding>() {

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheet.apply {
            this?.setBackgroundResource(android.R.color.transparent)
            this?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                setMargins(
                    resources.getDimensionPixelSize(R.dimen.dp_8).toInt(),
                    0,
                    resources.getDimensionPixelSize(R.dimen.dp_8),
                    resources.getDimensionPixelSize(R.dimen.dp_102)
                )
            }
        }
        binding.btnAdd.setOnClickListener {
            withParent<FileActionReceiver> { onAddAction() }
            dismiss()
        }
            binding.btnUploadFromGallery.setOnClickListener {
            withParent<FileActionReceiver> { onUploadFromGalleryAction() }
            dismiss()
        }
        binding.btnUploadFromStorage.setOnClickListener {
            withParent<FileActionReceiver> { onUploadFromStorageAction() }
            dismiss()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationFileValueActionBinding = FragmentRelationFileValueActionBinding.inflate(
        inflater, container, false
    )

    interface FileActionReceiver {
        fun onAddAction()
        fun onUploadFromGalleryAction()
        fun onUploadFromStorageAction()
    }
}
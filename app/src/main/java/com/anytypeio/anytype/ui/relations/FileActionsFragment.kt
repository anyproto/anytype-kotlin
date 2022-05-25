package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.databinding.FragmentRelationFileValueActionBinding

class FileActionsFragment : BaseDialogFragment<FragmentRelationFileValueActionBinding>() {

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAdd.setOnClickListener {
            withParent<FileActionReceiver> { onFileValueActionAdd() }
            dismiss()
        }
        binding.btnUploadFromGallery.setOnClickListener {
            withParent<FileActionReceiver> { onFileValueActionUploadFromGallery() }
            dismiss()
        }
        binding.btnUploadFromStorage.setOnClickListener {
            withParent<FileActionReceiver> { onFileValueActionUploadFromStorage() }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setupAppearance()
    }

    private fun setupAppearance() {
        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.DefaultBottomDialogAnimation)
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationFileValueActionBinding = FragmentRelationFileValueActionBinding.inflate(
        inflater, container, false
    )

    interface FileActionReceiver {
        fun onFileValueActionAdd()
        fun onFileValueActionUploadFromGallery()
        fun onFileValueActionUploadFromStorage()
    }
}
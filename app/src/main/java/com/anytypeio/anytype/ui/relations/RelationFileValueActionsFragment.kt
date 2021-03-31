package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_relation_file_value_action.*

class RelationFileValueActionsFragment : BaseDialogFragment() {

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_file_value_action, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAdd.setOnClickListener {
            withParent<RelationFileValueActionReceiver> { onFileValueActionAdd() }
            dismiss()
        }
        btnUploadFromGallery.setOnClickListener {
            withParent<RelationFileValueActionReceiver> { onFileValueActionUploadFromGallery() }
            dismiss()
        }
        btnUploadFromStorage.setOnClickListener {
            withParent<RelationFileValueActionReceiver> { onFileValueActionUploadFromStorage() }
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

    interface RelationFileValueActionReceiver {
        fun onFileValueActionAdd()
        fun onFileValueActionUploadFromGallery()
        fun onFileValueActionUploadFromStorage()
    }
}
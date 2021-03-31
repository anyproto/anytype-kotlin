package com.anytypeio.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.page.modal.SelectProgrammingLanguageAdapter
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.library_syntax_highlighter.obtainLanguages
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_select_programming_language.*
import timber.log.Timber

class SelectProgrammingLanguageFragment : BaseBottomSheetFragment() {

    private val selectLangAdapter by lazy {
        SelectProgrammingLanguageAdapter(
            items = requireContext().obtainLanguages()
        ) { lang ->
            val parent = parentFragment
            check(parent is SelectProgrammingLanguageReceiver)
            parent.onLanguageSelected(target, lang)
            dismiss()
        }
    }

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_select_programming_language, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        dialog?.setOnShowListener { dg ->
            val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(requireContext().color(android.R.color.transparent))
        }
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = selectLangAdapter
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(target: Id) = SelectProgrammingLanguageFragment().apply {
            arguments = bundleOf(ARG_TARGET to target)
        }

        private const val ARG_TARGET = "arg.select_language.target"
        private const val MISSING_TARGET_ERROR = "Target missing in args"
    }
}

interface SelectProgrammingLanguageReceiver {
    fun onLanguageSelected(target: Id, key: String)
}
package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import kotlinx.android.synthetic.main.fragment_viewer_relation_option.*
import java.util.*

class ViewerRelationOptionFragment : BaseDialogFragment() {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)
    private val format: ColumnView.Format? get() = requireArguments().getParcelable(FORMAT_KEY)
    private val title: String get() = arg(TITLE_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_relation_option, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle.text = title
        format?.let {
            tvFormat.text = it.name.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
            iconFormat.setBackgroundResource(it.relationIcon())
        }
        with(lifecycleScope) {
            subscribe(openToEditViewContainer.clicks()) { toast("Not implemented yet") }
            subscribe(removeViewContainer.clicks()) { toast("Not implemented yet") }
        }
    }

    override fun onStart() {
        super.onStart()
        setupAppearance()
    }

    override fun injectDependencies() {
    }

    override fun releaseDependencies() {
    }

    private fun setupAppearance() {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.DefaultBottomDialogAnimation)
        }
    }

    companion object {
        fun new(ctx: Id, title: String, relation: Id, format: ColumnView.Format) =
            ViewerRelationOptionFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to ctx,
                    TITLE_KEY to title,
                    RELATION_KEY to relation,
                    FORMAT_KEY to format
                )
            }

        const val CTX_KEY = "arg.dialog.relation-option.ctx"
        const val RELATION_KEY = "arg.dialog.relation-option.relation"
        const val FORMAT_KEY = "arg.dialog.relation-option.format"
        const val TITLE_KEY = "arg.dialog.relation-option.title"
    }
}
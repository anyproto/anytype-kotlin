package com.anytypeio.anytype.ui.`object`

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.`object`.ObjectTypeBaseAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.`object`.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.`object`.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.presentation.`object`.ObjectTypeView
import com.anytypeio.anytype.ui.page.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_object_type_change.*
import javax.inject.Inject

class ObjectTypeChangeFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(ARG_CTX)

    private val vm by viewModels<ObjectTypeChangeViewModel> { factory }

    @Inject
    lateinit var factory: ObjectTypeChangeViewModelFactory

    private val objectTypeAdapter by lazy {
        ObjectTypeBaseAdapter(
            onItemClick = { id ->
                withParent<OnFragmentInteractionListener> {
                    onObjectTypePicked(id)
                }
                view?.rootView?.hideKeyboard()
                dismiss()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_object_type_change, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            adapter = objectTypeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViews(views: List<ObjectTypeView.Item>) {
        objectTypeAdapter.submitList(views)
    }

    override fun onStart() {
        with(lifecycleScope) {
            jobs += subscribe(vm.results) { observeViews(it) }
            jobs += subscribe(searchObjectTypeInput.textChanges()) {
                vm.onQueryChanged(it.toString())
            }
        }
        super.onStart()
    }

    override fun injectDependencies() {
        componentManager().objectTypeChangeComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release(ctx)
    }

    companion object {
        fun new(ctx: String) = ObjectTypeChangeFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx
            )
        }

        const val ARG_CTX = "arg.object-type.ctx"
    }
}
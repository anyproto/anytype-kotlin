package com.anytypeio.anytype.ui.objects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_ui.features.objects.ObjectTypeBaseAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_object_type_change.*
import javax.inject.Inject

class ObjectTypeChangeFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(ARG_CTX)
    private val smartBlockType: SmartBlockType get() = arg(ARG_SMART_BLOCK_TYPE)

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
        vm.onStart(smartBlockType = smartBlockType)
    }

    override fun injectDependencies() {
        componentManager().objectTypeChangeComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectTypeChangeComponent.release(ctx)
    }

    companion object {
        fun new(ctx: String, smartBlockType: SmartBlockType) = ObjectTypeChangeFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_SMART_BLOCK_TYPE to smartBlockType
            )
        }

        const val ARG_CTX = "arg.object-type.ctx"
        const val ARG_SMART_BLOCK_TYPE = "arg.object-type.smart-block-type"
    }
}
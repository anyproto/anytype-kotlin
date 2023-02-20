package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.ObjectTypeAddAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentObjectTypeChangeBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.LimitObjectTypeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class LimitObjectTypeFragment : BaseBottomSheetFragment<FragmentObjectTypeChangeBinding>() {

    private val ctx get() = arg<Id>(CTX_KEY)
    private val flow get() = arg<Id>(FLOW_TYPE)

    private val objectTypeAdapter = ObjectTypeAddAdapter {
        vm.onObjectTypeClicked(it)
    }

    private val vm by viewModels<LimitObjectTypeViewModel> { factory }

    @Inject
    lateinit var factory: LimitObjectTypeViewModel.Factory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.apply {
            adapter = objectTypeAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.bottomButton.visible()
        binding.btnBottomAction.setOnClickListener {
            vm.onAddClicked()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.views.collectLatest {
                        objectTypeAdapter.submitList(it)
                    }
                }
                launch {
                    vm.count.collectLatest {
                        binding.tvCount.text = it.toString()
                    }
                }
                launch {
                    vm.isDismissed.collectLatest { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
            }
        }
        expand()
    }

    override fun injectDependencies() {
        when(flow) {
            FLOW_OBJECT -> {
                componentManager().limitObjectTypeComponent.get(ctx).inject(this)
            }
            FLOW_DV -> {
                componentManager().limitObjectTypeDataViewComponent.get(ctx).inject(this)
            }
            FLOW_BLOCK -> {
                componentManager().limitObjectTypeBlockComponent.get(ctx).inject(this)
            }
            FLOW_LIBRARY -> {
                componentManager().limitObjectTypeLibraryComponent.get(ctx).inject(this)
            }
        }
    }

    override fun releaseDependencies() {
        when(flow) {
            FLOW_OBJECT -> {
                componentManager().limitObjectTypeComponent.release(ctx)
            }
            FLOW_DV -> {
                componentManager().limitObjectTypeDataViewComponent.release(ctx)
            }
            FLOW_BLOCK -> {
                componentManager().limitObjectTypeBlockComponent.release(ctx)
            }
            FLOW_LIBRARY -> {
                componentManager().limitObjectTypeLibraryComponent.release(ctx)
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectTypeChangeBinding = FragmentObjectTypeChangeBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.limit-object-type.ctx"
        const val FLOW_TYPE = "arg.limit-object-type.flow"
        const val FLOW_OBJECT = "arg.limit-object-type.flow-object"
        const val FLOW_LIBRARY = "arg.limit-object-type.flow-library"
        const val FLOW_DV = "arg.limit-object-type.flow-dv"
        const val FLOW_BLOCK = "arg.limit-object-type.flow-block"
    }
}
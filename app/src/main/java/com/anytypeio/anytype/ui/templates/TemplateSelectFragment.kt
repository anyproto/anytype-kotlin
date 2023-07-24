package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentTemplateSelectBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import javax.inject.Inject

class TemplateSelectFragment :
    BaseFragment<FragmentTemplateSelectBinding>(R.layout.fragment_template_select) {

    private val vm by viewModels<TemplateSelectViewModel> { factory }

    @Inject
    lateinit var factory: TemplateSelectViewModel.Factory

    private val type: Id get() = arg(OBJECT_TYPE_KEY)
    private val ctx: Id get() = arg(CTX_KEY)
    private val withoutEmptyTemplate: Boolean get() = argBoolean(WITH_EMPTY_TEMPLATE_KEY)

    private lateinit var templatesAdapter: TemplateSelectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPagerAndTabs()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                setupClickEventHandlers()
            }
        }
    }

    private fun setupViewPagerAndTabs() {
        templatesAdapter = TemplateSelectAdapter(mutableListOf(), this)
        binding.templateViewPager.adapter = templatesAdapter
        TabLayoutMediator(binding.tabs, binding.templateViewPager) { _, _ -> }.attach()
    }

    private suspend fun setupClickEventHandlers() {
        setupUseTemplateClicks()
        setupCancelClicks()
        setupDismissStatusObserver()
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewState) { render(it) }
        super.onStart()
        vm.onStart(type = type, withoutBlankTemplate = withoutEmptyTemplate)
    }

    private fun render(viewState: TemplateSelectViewModel.ViewState) {
        when (viewState) {
            TemplateSelectViewModel.ViewState.ErrorGettingType -> TODO()
            TemplateSelectViewModel.ViewState.Init -> {
                binding.tvTemplateCountOrTutorial.text = null
                binding.btnCancel.isEnabled = true
                binding.btnUseTemplate.isEnabled = false
            }

            is TemplateSelectViewModel.ViewState.Success -> {
                binding.tvTemplateCountOrTutorial.text = getString(
                    R.string.this_type_has_templates,
                    viewState.objectTypeName,
                    viewState.templates.size
                )
                binding.btnUseTemplate.isEnabled = true
                templatesAdapter.update(viewState.templates)
            }
        }
    }

    private suspend fun setupUseTemplateClicks() {
        binding.btnUseTemplate.clicks().collect {
            vm.onUseTemplateButtonPressed(
                currentItem = binding.templateViewPager.currentItem,
                ctx = ctx
            )
        }
    }

    private suspend fun setupCancelClicks() {
        binding.btnCancel.clicks().collect { exit() }
    }

    private suspend fun setupDismissStatusObserver() {
        vm.isDismissed.collect { isDismissed -> if (isDismissed) exit() }
    }

    private fun exit() {
        findNavController().popBackStack()
    }

    override fun injectDependencies() {
        componentManager().templateSelectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().templateSelectComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTemplateSelectBinding = FragmentTemplateSelectBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val WITH_EMPTY_TEMPLATE_KEY = "arg.template.with_empty_template"
        const val OBJECT_TYPE_KEY = "arg.template.object_type"
        const val CTX_KEY = "arg.template.ctx"
    }
}
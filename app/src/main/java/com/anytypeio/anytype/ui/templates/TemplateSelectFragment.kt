package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import javax.inject.Inject

class TemplateSelectFragment :
    BaseFragment<FragmentTemplateSelectBinding>(R.layout.fragment_template_select) {

    private val vm by viewModels<TemplateSelectViewModel> { factory }

    @Inject
    lateinit var factory: TemplateSelectViewModel.Factory

    private val type: Id get() = arg(OBJECT_TYPE_KEY)
    private val ctx: Id get() = arg(CTX_KEY)
    private val withoutBlankTemplate: Boolean get() = argBoolean(WITH_BLANK_TEMPLATE_KEY)

    private lateinit var templatesAdapter: TemplateSelectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPagerAndTabs()
        with(lifecycleScope) {
            subscribe(binding.btnSkip.clicks()) {
                vm.onSkipButtonClicked()
            }
            subscribe(binding.btnUseTemplate.clicks()) {
                vm.onUseTemplateButtonPressed(
                    currentItem = binding.templateViewPager.currentItem,
                    ctx = ctx
                )
            }
        }
    }

    private fun setupViewPagerAndTabs() {
        templatesAdapter = TemplateSelectAdapter(mutableListOf(), this)
        binding.templateViewPager.adapter = templatesAdapter
        TabLayoutMediator(binding.tabs, binding.templateViewPager) { _, _ -> }.attach()
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.viewState) { render(it) }
        jobs += lifecycleScope.subscribe(vm.isDismissed) { if (it) exit() }
        super.onStart()
        vm.onStart(type = type, withoutBlankTemplate = withoutBlankTemplate)
    }

    private fun render(viewState: TemplateSelectViewModel.ViewState) {
        when (viewState) {
            TemplateSelectViewModel.ViewState.Init -> {
                binding.tvTemplateCountOrTutorial.text = null
                binding.btnSkip.isEnabled = true
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
        const val WITH_BLANK_TEMPLATE_KEY = "arg.template.with_empty_template"
        const val OBJECT_TYPE_KEY = "arg.template.object_type"
        const val CTX_KEY = "arg.template.ctx"
    }
}
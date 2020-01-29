package com.agileburo.anytype.ui.database.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.ui.database.modals.ModalsNavFragment.Companion.ARGS_DB_ID
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.di.feature.DetailEditModule
import com.agileburo.anytype.presentation.databaseview.modals.DetailEditViewModel
import com.agileburo.anytype.presentation.databaseview.modals.DetailEditViewModelFactory
import com.agileburo.anytype.presentation.databaseview.modals.DetailEditViewState
import com.agileburo.anytype.presentation.databaseview.models.ColumnView
import kotlinx.android.synthetic.main.modals_properties_edit.*
import javax.inject.Inject

class DetailEditFragment : BaseBottomSheetFragment() {

    companion object {
        const val ARGS_DETAIL_ID = "args.detail.id"

        fun newInstance(propertyId: String, databaseId: String): DetailEditFragment =
            DetailEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_DETAIL_ID, propertyId)
                    putString(ARGS_DB_ID, databaseId)
                }
            }
    }

    @Inject
    lateinit var factory: DetailEditViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(DetailEditViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modals_properties_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.onViewCreated()
    }

    private fun render(state: DetailEditViewState) {
        when (state) {
            is DetailEditViewState.Init -> {
                iconBack.setOnClickListener {
                    vm.onBackClick()
                }
                deleteContainer.setOnClickListener {
                    vm.onDeleteClick()
                }
                duplicateContainer.setOnClickListener {
                    vm.onDuplicateClick()
                }
                hideContainer.setOnClickListener {
                    vm.onHideClick()
                }
                val detail = state.detail
                when (detail) {
                    is ColumnView.Title -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_title)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_title))
                    }
                    is ColumnView.Text -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_text)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_text))
                    }
                    is ColumnView.Number -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_number)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_number))
                    }
                    is ColumnView.Date -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_date)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_date))
                    }
                    is ColumnView.Select -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_select)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_select))
                    }
                    is ColumnView.Multiple -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_multiple)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_multiselect))
                    }
                    is ColumnView.Person -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_person)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_person))
                    }
                    is ColumnView.File -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_file)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_file))
                    }
                    is ColumnView.Checkbox -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_checkbox)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_checkbox))
                    }
                    is ColumnView.URL -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_url)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_url))
                    }
                    is ColumnView.Email -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_email)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_email))
                    }
                    is ColumnView.Phone -> {
                        changeShowHideState(detail.show)
                        propName.text = detail.name
                        propType.text = getString(R.string.detail_phone)
                        iconPropType.setImageDrawable(requireContext().drawable(R.drawable.ic_phone))
                    }
                    ColumnView.AddNew -> TODO()
                }
            }
            is DetailEditViewState.NavigateToDetails -> {
                (parentFragment as ModalNavigation).showDetailsScreen()
            }
            is DetailEditViewState.Error -> {
                requireContext().toast(state.msg)
            }
        }
    }

    override fun injectDependencies() {
        componentManager()
            .mainComponent
            .detailEditBuilder()
            .propertyModule(
                DetailEditModule(
                    detailId = arguments?.getString(ARGS_DETAIL_ID) as String,
                    databaseId = arguments?.getString(ARGS_DB_ID) as String
                )
            )
            .build()
            .inject(this)
    }

    override fun releaseDependencies() {
    }

    private fun changeShowHideState(show: Boolean) {
        if (show) {
            iconHide.visible()
            iconShow.invisible()
            hide.text = getString(R.string.modal_hide)
        } else {
            iconHide.invisible()
            iconShow.visible()
            hide.text = getString(R.string.modal_show)
        }
    }
}
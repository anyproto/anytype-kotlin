package com.agileburo.anytype.ui.page.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.BaseBottomSheetFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel.ViewState
import kotlinx.android.synthetic.main.dialog_create_bookmark.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CreateBookmarkFragment : BaseBottomSheetFragment(), Observer<ViewState> {

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    private val context: String
        get() = requireArguments()
            .getString(ARG_CONTEXT)
            ?: throw IllegalStateException(MISSING_CONTEXT_ERROR)

    @Inject
    lateinit var factory: CreateBookmarkViewModel.Factory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateBookmarkViewModel::class.java)
    }

    companion object {

        private const val ARG_CONTEXT = "arg.create.bookmark.context"
        private const val ARG_TARGET = "arg.create.bookmark.target"

        private const val MISSING_TARGET_ERROR = "Target missing in args"
        private const val MISSING_CONTEXT_ERROR = "Context missing in args"

        fun newInstance(
            context: String,
            target: String
        ): CreateBookmarkFragment = CreateBookmarkFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT to context,
                ARG_TARGET to target
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_bookmark, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createBookmarkButton
            .clicks()
            .onEach {
                vm.onCreateBookmarkClicked(
                    context = context,
                    target = target,
                    url = urlInput.text.toString()
                )
            }
            .launchIn(lifecycleScope)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onChanged(state: ViewState) {
        if (state is ViewState.Exit)
            dismiss()
        else if (state is ViewState.Error)
            toast(state.message)
    }

    override fun injectDependencies() {
        componentManager().createBookmarkSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createBookmarkSubComponent.release()
    }
}
package com.anytypeio.anytype.ui.editor.modals

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.parsePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.DocumentEmojiIconPickerAdapter
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_CATEGORY_HEADER
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView.Companion.HOLDER_EMOJI_ITEM
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerBaseViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerBaseViewModel.ViewState
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModel
import com.anytypeio.anytype.presentation.editor.picker.ObjectIconPickerViewModelFactory
import kotlinx.android.synthetic.main.fragment_page_icon_picker.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

abstract class ObjectIconPickerBaseFragment : BaseBottomSheetFragment() {

    protected val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    protected val context: String
        get() = requireArguments()
            .getString(ARG_CONTEXT_ID_KEY)
            ?: throw IllegalStateException(MISSING_CONTEXT_ERROR)

    abstract val vm : ObjectIconPickerBaseViewModel

    private val emojiPickerAdapter by lazy {
        DocumentEmojiIconPickerAdapter(
            views = emptyList(),
            onEmojiClicked = { unicode ->
                vm.onEmojiClicked(
                    unicode = unicode,
                    target = target,
                    context = context
                )
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_page_icon_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        clearSearchText.setOnClickListener {
            filterInputField.setText(EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        filterInputField.doAfterTextChanged { vm.onQueryChanged(it.toString()) }
        btnRemoveIcon.setOnClickListener { vm.onRemoveClicked(context) }
        tvTabRandom.setOnClickListener { vm.onRandomEmoji(ctx = context, target = target) }
        tvTabUpload.setOnClickListener { proceedWithImagePick() }
        expand()
    }

    private fun setupRecycler() {
        pickerRecycler.apply {
            setItemViewCacheSize(EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when (val type = emojiPickerAdapter.getItemViewType(position)) {
                            HOLDER_EMOJI_ITEM -> 1
                            HOLDER_EMOJI_CATEGORY_HEADER -> PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT
                            else -> throw IllegalStateException("$UNEXPECTED_VIEW_TYPE_MESSAGE: $type")
                        }
                }
            }
            adapter = emojiPickerAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state().onEach { state ->
            when (state) {
                is ViewState.Success -> {
                    if (filterInputField.text.isNotEmpty())
                        clearSearchText.visible()
                    else
                        clearSearchText.invisible()
                    emojiPickerAdapter.update(state.views)
                    progressBar.invisible()
                }
                is ViewState.Loading -> {
                    clearSearchText.invisible()
                    progressBar.visible()
                }
                is ViewState.Init -> {
                    clearSearchText.visible()
                    progressBar.invisible()
                }
                is ViewState.Exit -> dismiss()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        dialog?.setOnShowListener(null)
        super.onDestroyView()
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission())
            requestExternalStoragePermission()
        else
            openGallery()
    }

    private fun requestExternalStoragePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_IMAGE_CODE) {
            data?.data?.let { uri ->
                try {
                    val path = uri.parsePath(requireContext())
                    vm.onPickedFromDevice(
                        ctx = context,
                        path = path
                    )
                } catch (e: Exception) {
                    Timber.e(COULD_NOT_PARSE_PATH_ERROR)
                    toast(COULD_NOT_PARSE_PATH_ERROR)
                }
            }
        }
    }

    private fun openGallery() {
        try {
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).let { intent ->
                startActivityForResult(intent, SELECT_IMAGE_CODE)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open gallery")
            toast("Failed to open gallery. Please, try again later.")
        }
    }

    companion object {
        private const val EMPTY_FILTER_TEXT = ""
        private const val PAGE_ICON_PICKER_DEFAULT_SPAN_COUNT = 6
        private const val EMOJI_RECYCLER_ITEM_VIEW_CACHE_SIZE = 2000
        private const val MISSING_TARGET_ERROR = "Missing target id"
        private const val MISSING_CONTEXT_ERROR = "Missing context id"
        private const val UNEXPECTED_VIEW_TYPE_MESSAGE = "Unexpected view type"

        const val ARG_CONTEXT_ID_KEY = "arg.picker.context.id"
        const val ARG_TARGET_ID_KEY = "arg.picker.target.id"

        private const val SELECT_IMAGE_CODE = 1
        private const val COULD_NOT_PARSE_PATH_ERROR = "Could not parse path to your image"
        private const val REQUEST_PERMISSION_CODE = 2
    }
}

open class ObjectIconPickerFragment : ObjectIconPickerBaseFragment() {

    @Inject
    lateinit var factory: ObjectIconPickerViewModelFactory
    override val vm by viewModels<ObjectIconPickerViewModel> { factory }

    override fun injectDependencies() {
        componentManager().objectIconPickerComponent.get(context).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectIconPickerComponent.release(context)
    }

    companion object {
        fun new(context: String, target: String) = ObjectIconPickerFragment().apply {
            arguments = bundleOf(
                ARG_CONTEXT_ID_KEY to context,
                ARG_TARGET_ID_KEY to target
            )
        }
    }
}
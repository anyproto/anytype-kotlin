package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.anytypeio.anytype.core_utils.ext.argLong
import com.anytypeio.anytype.core_utils.ext.timeInSeconds
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.databinding.FragmentDatePickerBinding
import java.util.*

class DatePickerFragment : BaseDialogFragment<FragmentDatePickerBinding>() {

    private val mTimeInSeconds get() = argLong(TIMESTAMP_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        initializePicker()
        binding.saveButton.setOnClickListener {
            dispatchResultAndDismiss()
        }
    }

    private fun dispatchResultAndDismiss() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.picker.year)
            set(Calendar.MONTH, binding.picker.month)
            set(Calendar.DAY_OF_MONTH, binding.picker.dayOfMonth)
        }
        withParent<DatePickerReceiver> { onPickDate(calendar.timeInSeconds()) }
        dismiss()
    }

    private fun initializePicker() {
        val calendar = Calendar.getInstance().apply {
            time = if (mTimeInSeconds > 0) {
                Date(mTimeInSeconds * 1000)
            } else {
                Date(System.currentTimeMillis())
            }
        }
        binding.picker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )
    }

    private fun setTransparentBackground() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDatePickerBinding = FragmentDatePickerBinding.inflate(
        inflater, container, false
    )

    companion object {

        fun new(timeInSeconds: Long?) = DatePickerFragment().apply {
            arguments = bundleOf(TIMESTAMP_KEY to timeInSeconds)
        }

        private const val TIMESTAMP_KEY = "arg.date-picker.timestamp"
    }

    interface DatePickerReceiver {
        fun onPickDate(timeInSeconds: Long)
    }
}
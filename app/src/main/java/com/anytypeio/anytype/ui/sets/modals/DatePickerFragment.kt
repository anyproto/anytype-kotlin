package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argLong
import com.anytypeio.anytype.core_utils.ext.timeInSeconds
import com.anytypeio.anytype.core_utils.ext.withParent
import kotlinx.android.synthetic.main.fragment_date_picker.*
import timber.log.Timber
import java.util.*

class DatePickerFragment : DialogFragment() {

    private val mTimeInSeconds get() = argLong(TIMESTAMP_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_date_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        initializePicker()
        saveButton.setOnClickListener {
            dispatchResultAndDismiss()
        }
    }

    private fun dispatchResultAndDismiss() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, picker.year)
            set(Calendar.MONTH, picker.month)
            set(Calendar.DAY_OF_MONTH, picker.dayOfMonth)
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
        picker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )
    }

    private fun setTransparentBackground() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

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
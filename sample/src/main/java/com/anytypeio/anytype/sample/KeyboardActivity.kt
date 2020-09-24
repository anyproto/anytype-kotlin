package com.anytypeio.anytype.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.anytypeio.anytype.core_ui.widgets.BlockActionBarItem
import com.anytypeio.anytype.core_ui.widgets.ActionItemType
import com.anytypeio.anytype.core_ui.widgets.dialog.AboveDialog
import kotlinx.android.synthetic.main.above_fragment.*
import kotlinx.android.synthetic.main.activity_keyboard.*
import timber.log.Timber

class KeyboardActivity : AppCompatActivity(), UpdateValues {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)

        tvFirst.text = "111"
        tvSecond.text = "222"
        tvThree.text = "333"

        btnTest.setOnClickListener {
            AboveDialogFragment().apply {
                //setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
            }
                .show(supportFragmentManager, null)
        }

        edtTest.setOnFocusChangeListener { v, hasFocus ->
            Timber.d("EditText1 hasFocus:$hasFocus")
        }

        edtTest.setOnClickListener {
            Timber.d("EditText1 : OnClick, hasFocus:${it.hasFocus()}")
        }

        edtTest.setOnLongClickListener {
            Timber.d("EditText1 : OnLongClick, hasFocus: ${it.hasFocus()} , hasOtherBlocksInFocus : ${it.hasWindowFocus()}")
            val focus = it.hasFocus()
            if (!focus) {
                TestDialog().show(supportFragmentManager, "TAG")
            }
            return@setOnLongClickListener !it.hasFocus()
        }

        with(edtTest2) {
            setOnLongClickListener {
                Timber.d("EditText2 : OnLongClick, hasFocus: ${it.hasFocus()},  hasOtherBlocksInFocus : ${it.hasWindowFocus()}")
                val focus = it.hasFocus()
                if (!focus) {
                    TestDialog().show(supportFragmentManager, "TAG")
                }
                return@setOnLongClickListener !it.hasFocus()
            }
        }
    }

    override fun update1() {
        tvFirst.text = incText(tvFirst.text.toString())
    }

    override fun update2() {
        tvSecond.text = incText(tvSecond.text.toString())
    }

    override fun update3() {
        tvThree.text = incText(tvThree.text.toString())
    }

    fun incText(value: String): String =
        value.toInt().inc().toString()
}

interface UpdateValues {
    fun update1()
    fun update2()
    fun update3()
}

class TestDialog : AboveDialog() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        with(action_container) {
            addView(BlockActionBarItem(context = requireContext()).apply {
                setTypeAndClick(
                    ActionItemType.Replace,
                    {})
            })
            addView(BlockActionBarItem(context = requireContext()).apply {
                setTypeAndClick(
                    ActionItemType.TurnInto,
                    {})
            })
            addView(BlockActionBarItem(context = requireContext()).apply {
                setTypeAndClick(
                    ActionItemType.Delete,
                    {})
            })
        }


    }

    override fun layout(): Int {
        return R.layout.above_fragment
    }

    override fun title(): String? {
        return null
    }
}
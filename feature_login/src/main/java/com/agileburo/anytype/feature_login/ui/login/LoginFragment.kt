package com.agileburo.anytype.feature_login.ui.login

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.agileburo.anytype.feature_login.R
import kotlinx.android.synthetic.main.login_fragment.*
import timber.log.Timber

class LoginFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText.setOnFocusChangeListener { v, hasFocus ->
            Timber.d("View:${v.id} , hasFocus:$hasFocus")
        }
        editText2.setOnFocusChangeListener { v, hasFocus ->
            Timber.d("View:${v.id} , hasFocus:$hasFocus")
            if (hasFocus) (v as? EditText)?.isCursorVisible = true
        }
        button.setOnClickListener { v: View? ->
            editText.clearFocus()
            editText2.clearFocus()
            hideSoftKeyBoard(requireActivity(), v)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun hideSoftKeyBoard(activity: Activity, view: View?) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.applicationWindowToken, 0)
    }

}

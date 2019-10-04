package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin

import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.ViewStateViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class ChoosePinCodeViewModel : ViewStateViewModel<ViewState<Boolean>>(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    private val reducer = BiFunction<PinCodeState, PinCodeAction, PinCodeState> { prev, action ->
        when (action) {
            is PinCodeAction.AddDigit -> {
                if (prev.completed)
                    prev.copy()
                else {
                    prev.copy(
                        digits = mutableListOf<Int>().apply {
                            addAll(prev.digits)
                            add(action.digit)
                        }
                    )
                }
            }
            is PinCodeAction.Removed -> {
                if (prev.digits.isNotEmpty())
                    prev.copy(digits = prev.digits.dropLast(1))
                else
                    prev.copy()
            }
        }
    }

    private val actions = PublishRelay.create<PinCodeAction>()

    val pin: Observable<PinCodeState> = actions
        .scan<PinCodeState>(
            PinCodeState(digits = emptyList()),
            reducer
        )
        .replay(1)
        .autoConnect()

    init {
        pin.map { state -> ViewState.Success(state.completed) }.subscribe(state).disposedBy(subscriptions)
        pin.subscribe { state -> checkPinCodeCompletion(state) }.disposedBy(subscriptions)
    }

    override fun observeNavigation(): Observable<NavigationCommand> = navigation

    fun onNumPadClicked(entry: Int) {
        actions.accept(PinCodeAction.AddDigit(entry))
    }

    fun onRemovedDigitClicked() {
        actions.accept(PinCodeAction.Removed)
    }

    fun onDoItLaterClicked() {
        navigation.accept(NavigationCommand.CongratulationScreen)
    }

    private fun checkPinCodeCompletion(state: PinCodeState) {
        if (state.completed) navigation.accept(NavigationCommand.ConfirmPinCodeScreen(state.digits.toString()))
    }

    sealed class PinCodeAction {
        data class AddDigit(val digit: Int) : PinCodeAction()
        object Removed : PinCodeAction()
    }
}
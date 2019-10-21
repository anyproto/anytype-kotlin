package com.agileburo.anytype.presentation.auth.pin

import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.presentation.auth.congratulation.ViewState

class ChoosePinCodeViewModel : ViewStateViewModel<ViewState<Boolean>>() {

    /*
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

    */
}
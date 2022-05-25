package com.anytypeio.anytype.presentation.auth.pin

import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.presentation.common.ViewState

class ConfirmPinCodeViewModel : ViewStateViewModel<ViewState<Boolean>>() {

    /*

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    var code: String = ""

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
        pin
            .filter { it.completed }
            .map { state -> ViewState.Success(state.digits.toString() == code) }
            .subscribe(state)
            .disposedBy(subscriptions)

        pin
            .filter { it.completed && it.digits.toString() == code }
            .subscribe {
                navigation.accept(NavigationCommand.CongratulationScreen)
            }.disposedBy(subscriptions)
    }

    override fun observeNavigation(): Observable<NavigationCommand> = navigation

    fun onNumPadClicked(entry: Int) {
        actions.accept(PinCodeAction.AddDigit(entry))
    }

    fun onRemovedDigitClicked() {
        actions.accept(PinCodeAction.Removed)
    }

    sealed class PinCodeAction {
        data class AddDigit(val digit: Int) : PinCodeAction()
        object Removed : PinCodeAction()
    }

    */

}
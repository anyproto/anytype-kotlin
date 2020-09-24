package com.anytypeio.anytype.presentation.auth

@Deprecated("Refactoring needed")
class ChoosePinCodeViewModelTest {

    /*

    private val vm by lazy { ChoosePinCodeViewModel() }

    @Test
    fun `one click increments length`() {

        val testObserver = vm.pin.test()

        testObserver.apply {
            assertValue { value -> value.digits.isEmpty() }
        }

        vm.onNumPadClicked(2)

        testObserver.apply {
            assertValueAt(1) { value ->
                value.digits.size == 1
            }
        }
    }

    @Test
    fun `when pin length equals max pin length, then new values are appended to the list`() {
        val expected = listOf(1, 1, 1, 1, 1, 1)

        val testObserver = vm.pin.test()

        vm.onNumPadClicked(1)
        vm.onNumPadClicked(1)
        vm.onNumPadClicked(1)
        vm.onNumPadClicked(1)
        vm.onNumPadClicked(1)
        vm.onNumPadClicked(1)

        testObserver.apply {
            assertValueCount(7)
            assertValueAt(6) { value -> value.digits == expected }
        }

        vm.onNumPadClicked(1)

        testObserver.apply {
            assertValueCount(8)
            assertValueAt(7) { value -> value.digits == expected }
        }
    }

    @Test
    fun `should mirror the same order as entered by user`() {

        val expected = listOf(1, 2, 3, 4, 5, 6)

        val testObserver = vm.pin.test()

        vm.onNumPadClicked(1)
        vm.onNumPadClicked(2)
        vm.onNumPadClicked(3)
        vm.onNumPadClicked(4)
        vm.onNumPadClicked(5)
        vm.onNumPadClicked(6)

        testObserver.apply {
            assertValueCount(7)
            assertValueAt(6) { value -> value.digits == expected }
        }
    }

    */

}
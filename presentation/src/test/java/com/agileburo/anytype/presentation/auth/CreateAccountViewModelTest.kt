package com.agileburo.anytype.presentation.auth

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.presentation.auth.account.CreateAccountViewModel
import com.agileburo.anytype.presentation.auth.model.Session
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotEquals

class CreateAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: CreateAccountViewModel

    private val session = Session()

    @Before
    fun setup() {
        vm = CreateAccountViewModel(session)
    }

    @Test
    fun `when button clicked, should save name in session object and navigate to next screen`() {

        val navigationObserver = vm.observeNavigation().test()

        navigationObserver.assertNoValue()

        val name = session.name

        val input = MockDataFactory.randomString()

        vm.onCreateProfileClicked(input)

        assertNotEquals(name, session.name)

        navigationObserver.assertValue { value ->
            value.peekContent() is AppNavigation.Command.SetupNewAccountScreen
        }
    }

    @Test
    fun `should emit an error message if input is empty`() {

        val navigationObserver = vm.observeNavigation().test()
        val errorObserver = vm.error.test()

        val emptyInput = ""

        vm.onCreateProfileClicked(emptyInput)

        navigationObserver.assertNoValue()
        errorObserver.assertValue(CreateAccountViewModel.EMPTY_USERNAME_ERROR_MESSAGE)
    }
}
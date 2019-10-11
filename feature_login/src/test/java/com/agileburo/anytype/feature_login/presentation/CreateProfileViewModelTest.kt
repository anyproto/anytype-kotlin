package com.agileburo.anytype.feature_login.presentation

import com.agileburo.anytype.feature_login.common.DataFactory
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals

class CreateProfileViewModelTest {

    lateinit var vm: CreateAccountViewModel

    private val session = Session()

    @Before
    fun setup() {
        vm = CreateAccountViewModel(session)
    }

    @Test
    fun `when button clicked, should save name in session object and navigate to next screen`() {

        val navigationObserver = vm.observeNavigation().test()

        navigationObserver.assertNotComplete().assertNoValues()

        val name = session.name

        val input = DataFactory.randomString()

        vm.onCreateProfileClicked(input)

        assertNotEquals(name, session.name)

        navigationObserver.assertValue(NavigationCommand.SetupNewAccountScreen)
    }
}
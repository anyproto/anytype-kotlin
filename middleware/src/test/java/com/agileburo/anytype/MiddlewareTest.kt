package com.agileburo.anytype

import anytype.Commands.Rpc.Account
import com.agileburo.anytype.middleware.interactor.Middleware
import com.agileburo.anytype.middleware.service.MiddlewareService
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MiddlewareTest {

    @Mock
    lateinit var service: MiddlewareService

    private lateinit var middleware: Middleware

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        middleware = Middleware(service)
    }

    @Test
    fun `should call account-stop method when logging out`() {
        middleware.logout()

        val request = Account.Stop.Request.newBuilder().build()

        verify(service, times(1)).accountStop(request)
        verifyNoMoreInteractions(service)
    }
}
package com.anytypeio.anytype.test_utils

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

class ValueClassAnswer(private val value: Any) : Answer<Any> {
    override fun answer(invocation: InvocationOnMock?): Any = value
}
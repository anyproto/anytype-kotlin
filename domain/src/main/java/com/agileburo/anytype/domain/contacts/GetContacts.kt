package com.agileburo.anytype.domain.contacts

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

class GetContacts : BaseUseCase<List<Contact>, BaseUseCase.None>() {

    override suspend fun run(params: None): Either<Throwable, List<Contact>> = try {
        Either.Right(ContactsMock.CONTACTS)
    } catch (e: Throwable) {
        Either.Left(e)
    }
}
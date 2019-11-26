package com.agileburo.anytype.domain.contacts

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

class GetContact : BaseUseCase<Contact, GetContact.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Contact> = try {
        ContactsMock.CONTACTS.find {
            it.id == params.contactId
        }.let {
            if (it == null) {
                Either.Left(Throwable("Error getting contact by id=${params.contactId}"))
            } else {
                Either.Right(it)
            }
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(
        val contactId: String
    )
}
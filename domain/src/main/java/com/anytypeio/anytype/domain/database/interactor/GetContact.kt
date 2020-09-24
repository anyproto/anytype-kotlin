package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.model.Contact
import com.anytypeio.anytype.domain.database.ContactsMock

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
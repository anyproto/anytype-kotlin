package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.misc.UrlBuilder

/** Use case for getting currently selected user account.
 */
class GetCurrentAccount(
    private val repo: BlockRepository,
    private val builder: UrlBuilder
) : BaseUseCase<Account, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {
        Either.Right(execute())
    } catch (t: Throwable) {
        Either.Left(t)
    }

    private suspend fun execute(): Account {
        val config = repo.getConfig()

        val payload = repo.openProfile(config.profile)

        val event = payload.events.first { event -> event is Event.Command.ShowBlock }

        val details = (event as Event.Command.ShowBlock).details.details[config.profile]

        val name = details?.name ?: throw IllegalStateException(MISSING_NAME_ERROR)

        val image = details.iconImage

        return Account(
            id = config.profile,
            name = name,
            avatar = image?.let { builder.image(it) },
            color = null
        )
    }

    companion object {
        const val MISSING_NAME_ERROR = "Profile name is missing"
    }
}
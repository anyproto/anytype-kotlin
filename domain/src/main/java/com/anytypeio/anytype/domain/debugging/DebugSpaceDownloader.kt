package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class DebugSpaceShareDownloader @Inject constructor(
    private val debugSpace: DebugSpace,
    private val debugSpaceContentSaver: DebugSpaceContentSaver,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<Unit, Filepath>(dispatchers.io) {
    override suspend fun doWork(params: Unit): Filepath {
        val content = debugSpace.run(Unit)
        val file = debugSpaceContentSaver.save(content = content)
        return file.path
    }
}
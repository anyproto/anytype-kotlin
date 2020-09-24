package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.common.Url
import com.anytypeio.anytype.domain.download.DownloadFile.Params
import kotlin.coroutines.CoroutineContext

/**
 * Use-case for starting downloading files.
 * @see Params
 */
class DownloadFile(
    private val downloader: Downloader,
    context: CoroutineContext
) : BaseUseCase<Unit, Params>(context) {

    override suspend fun run(params: Params) = try {
        downloader.download(
            url = params.url,
            name = params.name
        ).let { Either.Right(it) }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for downloading file.
     * @property name file name
     * @property url url of the file to download
     */
    data class Params(
        val name: String,
        val url: Url
    )
}
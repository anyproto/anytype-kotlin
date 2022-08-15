package com.anytypeio.anytype.presentation.common

import com.anytypeio.anytype.core_models.Url

sealed class SystemAction {
    data class OpenUrl(val url: Url) : SystemAction()
    data class CopyToClipboard(val plain: String) : SystemAction()
    data class MailTo(val mail: String) : SystemAction()
}
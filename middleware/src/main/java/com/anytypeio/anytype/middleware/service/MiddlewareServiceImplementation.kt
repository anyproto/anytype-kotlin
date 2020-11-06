package com.anytypeio.anytype.middleware.service

import anytype.Rpc.*
import anytype.Rpc.Config
import service.Service

class MiddlewareServiceImplementation : MiddlewareService {

    override fun configGet(request: Config.Get.Request): Config.Get.Response {
        val encoded = Service.configGet(Config.Get.Request.ADAPTER.encode(request))
        val response = Config.Get.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Config.Get.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletCreate(request: Wallet.Create.Request): Wallet.Create.Response {
        val encoded = Service.walletCreate(Wallet.Create.Request.ADAPTER.encode(request))
        val response = Wallet.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletConvert(request: Wallet.Convert.Request): Wallet.Convert.Response {
        val encoded = Service.walletConvert(Wallet.Convert.Request.ADAPTER.encode(request))
        val response = Wallet.Convert.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Convert.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun walletRecover(request: Wallet.Recover.Request): Wallet.Recover.Response {
        val encoded = Service.walletRecover(Wallet.Recover.Request.ADAPTER.encode(request))
        val response = Wallet.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Wallet.Recover.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountCreate(request: Account.Create.Request): Account.Create.Response {
        val encoded = Service.accountCreate(Account.Create.Request.ADAPTER.encode(request))
        val response = Account.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountSelect(request: Account.Select.Request): Account.Select.Response {
        val encoded = Service.accountSelect(Account.Select.Request.ADAPTER.encode(request))
        val response = Account.Select.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Select.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountRecover(request: Account.Recover.Request): Account.Recover.Response {
        val encoded = Service.accountRecover(Account.Recover.Request.ADAPTER.encode(request))
        val response = Account.Recover.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Recover.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun accountStop(request: Account.Stop.Request): Account.Stop.Response {
        val encoded = Service.accountStop(Account.Stop.Request.ADAPTER.encode(request))
        val response = Account.Stop.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Account.Stop.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockOpen(request: Block.Open.Request): Block.Open.Response {
        val encoded = Service.blockOpen(Block.Open.Request.ADAPTER.encode(request))
        val response = Block.Open.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Open.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockClose(request: Block.Close.Request): Block.Close.Response {
        val encoded = Service.blockClose(Block.Close.Request.ADAPTER.encode(request))
        val response = Block.Close.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Close.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreate(request: Block.Create.Request): Block.Create.Response {
        val encoded = Service.blockCreate(Block.Create.Request.ADAPTER.encode(request))
        val response = Block.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCreatePage(request: Block.CreatePage.Request): Block.CreatePage.Response {
        val encoded = Service.blockCreatePage(Block.CreatePage.Request.ADAPTER.encode(request))
        val response = Block.CreatePage.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.CreatePage.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextText(request: Block.Set.Text.TText.Request): Block.Set.Text.TText.Response {
        val encoded = Service.blockSetTextText(Block.Set.Text.TText.Request.ADAPTER.encode(request))
        val response = Block.Set.Text.TText.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Text.TText.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextChecked(request: Block.Set.Text.Checked.Request): Block.Set.Text.Checked.Response {
        val encoded = Service.blockSetTextChecked(
            Block.Set.Text.Checked.Request.ADAPTER.encode(request)
        )
        val response = Block.Set.Text.Checked.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Text.Checked.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetTextColor(request: Block.Set.Text.Color.Request): Block.Set.Text.Color.Response {
        val encoded = Service.blockSetTextColor(
            Block.Set.Text.Color.Request.ADAPTER.encode(request)
        )
        val response = Block.Set.Text.Color.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Text.Color.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetBackgroundColor(request: BlockList.Set.BackgroundColor.Request): BlockList.Set.BackgroundColor.Response {
        val encoded = Service.blockListSetBackgroundColor(
            BlockList.Set.BackgroundColor.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.BackgroundColor.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.BackgroundColor.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetAlign(request: BlockList.Set.Align.Request): BlockList.Set.Align.Response {
        val encoded = Service.blockListSetAlign(BlockList.Set.Align.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Align.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Align.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetTextStyle(request: BlockList.Set.Text.Style.Request): BlockList.Set.Text.Style.Response {
        val encoded =
            Service.blockListSetTextStyle(BlockList.Set.Text.Style.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Text.Style.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Text.Style.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetDivStyle(request: BlockList.Set.Div.Style.Request): BlockList.Set.Div.Style.Response {
        val encoded =
            Service.blockListSetDivStyle(BlockList.Set.Div.Style.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Div.Style.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Div.Style.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListMove(request: BlockList.Move.Request): BlockList.Move.Response {
        val encoded = Service.blockListMove(BlockList.Move.Request.ADAPTER.encode(request))
        val response = BlockList.Move.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Move.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUnlink(request: Block.Unlink.Request): Block.Unlink.Response {
        val encoded = Service.blockUnlink(Block.Unlink.Request.ADAPTER.encode(request))
        val response = Block.Unlink.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Unlink.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockMerge(request: Block.Merge.Request): Block.Merge.Response {
        val encoded = Service.blockMerge(Block.Merge.Request.ADAPTER.encode(request))
        val response = Block.Merge.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Merge.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSplit(request: Block.Split.Request): Block.Split.Response {
        val encoded = Service.blockSplit(Block.Split.Request.ADAPTER.encode(request))
        val response = Block.Split.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Split.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListDuplicate(request: BlockList.Duplicate.Request): BlockList.Duplicate.Response {
        val encoded = Service.blockListDuplicate(
            BlockList.Duplicate.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Duplicate.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Duplicate.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun convertChildrenToPages(request: BlockList.ConvertChildrenToPages.Request): BlockList.ConvertChildrenToPages.Response {
        val encoded = Service.blockListConvertChildrenToPages(
            BlockList.ConvertChildrenToPages.Request.ADAPTER.encode(request)
        )
        val response = BlockList.ConvertChildrenToPages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.ConvertChildrenToPages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockBookmarkFetch(request: Block.Bookmark.Fetch.Request): Block.Bookmark.Fetch.Response {
        val encoded = Service.blockBookmarkFetch(
            Block.Bookmark.Fetch.Request.ADAPTER.encode(request)
        )
        val response = Block.Bookmark.Fetch.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Bookmark.Fetch.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUpload(request: Block.Upload.Request): Block.Upload.Response {
        val encoded = Service.blockUpload(Block.Upload.Request.ADAPTER.encode(request))
        val response = Block.Upload.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Upload.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockUndo(request: Block.Undo.Request): Block.Undo.Response {
        val encoded = Service.blockUndo(Block.Undo.Request.ADAPTER.encode(request))
        val response = Block.Undo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Undo.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockRedo(request: Block.Redo.Request): Block.Redo.Response {
        val encoded = Service.blockRedo(Block.Redo.Request.ADAPTER.encode(request))
        val response = Block.Redo.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Redo.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetPageIsArchived(request: BlockList.Set.Page.IsArchived.Request): BlockList.Set.Page.IsArchived.Response {
        val encoded = Service.blockListSetPageIsArchived(
            BlockList.Set.Page.IsArchived.Request.ADAPTER.encode(request)
        )
        val response = BlockList.Set.Page.IsArchived.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Page.IsArchived.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockSetDetails(request: Block.Set.Details.Request): Block.Set.Details.Response {
        val encoded = Service.blockSetDetails(Block.Set.Details.Request.ADAPTER.encode(request))
        val response = Block.Set.Details.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Set.Details.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockPaste(request: Block.Paste.Request): Block.Paste.Response {
        val encoded = Service.blockPaste(Block.Paste.Request.ADAPTER.encode(request))
        val response = Block.Paste.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Paste.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockCopy(request: Block.Copy.Request): Block.Copy.Response {
        val encoded = Service.blockCopy(Block.Copy.Request.ADAPTER.encode(request))
        val response = Block.Copy.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Block.Copy.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun uploadFile(request: UploadFile.Request): UploadFile.Response {
        val encoded = Service.uploadFile(UploadFile.Request.ADAPTER.encode(request))
        val response = UploadFile.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != UploadFile.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun pageInfoWithLinks(request: Navigation.GetPageInfoWithLinks.Request): Navigation.GetPageInfoWithLinks.Response {
        val encoded = Service.navigationGetPageInfoWithLinks(
            Navigation.GetPageInfoWithLinks.Request.ADAPTER.encode(request)
        )
        val response = Navigation.GetPageInfoWithLinks.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Navigation.GetPageInfoWithLinks.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun listPages(request: Navigation.ListPages.Request): Navigation.ListPages.Response {
        val encoded = Service.navigationListPages(
            Navigation.ListPages.Request.ADAPTER.encode(request)
        )
        val response = Navigation.ListPages.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Navigation.ListPages.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun pageCreate(request: Page.Create.Request): Page.Create.Response {
        val encoded = Service.pageCreate(Page.Create.Request.ADAPTER.encode(request))
        val response = Page.Create.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Page.Create.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun versionGet(request: Version.Get.Request): Version.Get.Response {
        val encoded = Service.versionGet(Version.Get.Request.ADAPTER.encode(request))
        val response = Version.Get.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Version.Get.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }

    override fun blockListSetFields(request: BlockList.Set.Fields.Request): BlockList.Set.Fields.Response {
        val encoded =
            Service.blockListSetFields(BlockList.Set.Fields.Request.ADAPTER.encode(request))
        val response = BlockList.Set.Fields.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != BlockList.Set.Fields.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }
}
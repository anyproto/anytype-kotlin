package com.anytypeio.anytype.middleware.service

import anytype.Rpc.*
import kotlin.jvm.Throws

/**
 * Service for interacting with the backend.
 */
interface MiddlewareService {
    @Throws(Exception::class)
    fun configGet(request: Config.Get.Request): Config.Get.Response

    @Throws(Exception::class)
    fun walletCreate(request: Wallet.Create.Request): Wallet.Create.Response

    @Throws(Exception::class)
    fun walletConvert(request: Wallet.Convert.Request): Wallet.Convert.Response

    @Throws(Exception::class)
    fun walletRecover(request: Wallet.Recover.Request): Wallet.Recover.Response

    @Throws(Exception::class)
    fun accountCreate(request: Account.Create.Request): Account.Create.Response

    @Throws(Exception::class)
    fun accountSelect(request: Account.Select.Request): Account.Select.Response

    @Throws(Exception::class)
    fun accountRecover(request: Account.Recover.Request): Account.Recover.Response

    @Throws(Exception::class)
    fun accountStop(request: Account.Stop.Request): Account.Stop.Response

    @Throws(Exception::class)
    fun blockOpen(request: Block.Open.Request): Block.Open.Response

    @Throws(Exception::class)
    fun blockClose(request: Block.Close.Request): Block.Close.Response

    @Throws(Exception::class)
    fun blockCreate(request: Block.Create.Request): Block.Create.Response

    @Throws(Exception::class)
    fun blockCreatePage(request: Block.CreatePage.Request): Block.CreatePage.Response

    @Throws(Exception::class)
    fun blockSetTextText(request: Block.Set.Text.TText.Request): Block.Set.Text.TText.Response

    @Throws(Exception::class)
    fun blockSetTextChecked(request: Block.Set.Text.Checked.Request): Block.Set.Text.Checked.Response

    @Throws(Exception::class)
    fun blockSetTextColor(request: Block.Set.Text.Color.Request): Block.Set.Text.Color.Response

    @Throws(Exception::class)
    fun blockListSetBackgroundColor(request: BlockList.Set.BackgroundColor.Request): BlockList.Set.BackgroundColor.Response

    @Throws(Exception::class)
    fun blockListSetAlign(request: BlockList.Set.Align.Request): BlockList.Set.Align.Response

    @Throws(Exception::class)
    fun blockListSetTextStyle(request: BlockList.Set.Text.Style.Request): BlockList.Set.Text.Style.Response

    @Throws(Exception::class)
    fun blockListSetDivStyle(request: BlockList.Set.Div.Style.Request): BlockList.Set.Div.Style.Response

    @Throws(Exception::class)
    fun blockListMove(request: BlockList.Move.Request): BlockList.Move.Response

    @Throws(Exception::class)
    fun blockUnlink(request: Block.Unlink.Request): Block.Unlink.Response

    @Throws(Exception::class)
    fun blockMerge(request: Block.Merge.Request): Block.Merge.Response

    @Throws(Exception::class)
    fun blockSplit(request: Block.Split.Request): Block.Split.Response

    @Throws(Exception::class)
    fun blockListDuplicate(request: BlockList.Duplicate.Request): BlockList.Duplicate.Response

    @Throws(Exception::class)
    fun convertChildrenToPages(request: BlockList.ConvertChildrenToPages.Request): BlockList.ConvertChildrenToPages.Response

    @Throws(Exception::class)
    fun blockBookmarkFetch(request: Block.Bookmark.Fetch.Request): Block.Bookmark.Fetch.Response

    @Throws(Exception::class)
    fun blockUpload(request: Block.Upload.Request): Block.Upload.Response

    @Throws(Exception::class)
    fun blockUndo(request: Block.Undo.Request): Block.Undo.Response

    @Throws(Exception::class)
    fun blockRedo(request: Block.Redo.Request): Block.Redo.Response

    @Throws(Exception::class)
    fun blockListSetPageIsArchived(request: BlockList.Set.Page.IsArchived.Request): BlockList.Set.Page.IsArchived.Response

    @Throws(Exception::class)
    fun blockSetDetails(request: Block.Set.Details.Request): Block.Set.Details.Response

    @Throws(Exception::class)
    fun blockPaste(request: Block.Paste.Request): Block.Paste.Response

    @Throws(Exception::class)
    fun blockCopy(request: Block.Copy.Request): Block.Copy.Response

    @Throws(Exception::class)
    fun uploadFile(request: UploadFile.Request): UploadFile.Response

    @Throws(Exception::class)
    fun pageInfoWithLinks(request: Navigation.GetPageInfoWithLinks.Request): Navigation.GetPageInfoWithLinks.Response

    @Throws(Exception::class)
    fun listPages(request: Navigation.ListPages.Request): Navigation.ListPages.Response

    @Throws(Exception::class)
    fun pageCreate(request: Page.Create.Request): Page.Create.Response

    @Throws(Exception::class)
    fun versionGet(request: Version.Get.Request): Version.Get.Response

    @Throws(Exception::class)
    fun blockListSetFields(request: BlockList.Set.Fields.Request): BlockList.Set.Fields.Response
}
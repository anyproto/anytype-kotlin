package com.anytypeio.anytype.middleware.service;

import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException;

import anytype.Commands;
import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Navigation;
import anytype.Commands.Rpc.UploadFile;
import anytype.Commands.Rpc.Wallet;
import service.Service;

public class DefaultMiddlewareService implements MiddlewareService {

    @Override
    public Config.Get.Response configGet(Config.Get.Request request) throws Exception {
        byte[] encoded = Service.configGet(request.toByteArray());
        Config.Get.Response response = Config.Get.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Config.Get.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Wallet.Create.Response walletCreate(Wallet.Create.Request request) throws Exception {
        byte[] encoded = Service.walletCreate(request.toByteArray());
        Wallet.Create.Response response = Wallet.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Wallet.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Create.Response accountCreate(Account.Create.Request request) throws Exception {
        byte[] encoded = Service.accountCreate(request.toByteArray());
        Account.Create.Response response = Account.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getCode().name() + "," + response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Wallet.Recover.Response walletRecover(Wallet.Recover.Request request) throws Exception {
        byte[] encoded = Service.walletRecover(request.toByteArray());
        Wallet.Recover.Response response = Wallet.Recover.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Wallet.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Recover.Response accountRecover(Account.Recover.Request request) throws Exception {
        byte[] encoded = Service.accountRecover(request.toByteArray());
        Account.Recover.Response response = Account.Recover.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Stop.Response accountStop(Account.Stop.Request request) throws Exception {
        byte[] encoded = Service.accountStop(request.toByteArray());
        Account.Stop.Response response = Account.Stop.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Stop.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Select.Response accountSelect(Account.Select.Request request) throws Exception {
        byte[] encoded = Service.accountSelect(request.toByteArray());
        Account.Select.Response response = Account.Select.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Select.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Open.Response blockOpen(Block.Open.Request request) throws Exception {
        byte[] encoded = Service.blockOpen(request.toByteArray());
        Block.Open.Response response = Block.Open.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            if (response.getError().getCode() == Block.Open.Response.Error.Code.ANYTYPE_NEEDS_UPGRADE) {
                throw new BackwardCompatilityNotSupportedException();
            } else {
                throw new Exception(response.getError().getDescription());
            }
        } else {
            return response;
        }
    }

    @Override
    public Block.Close.Response blockClose(Block.Close.Request request) throws Exception {
        byte[] encoded = Service.blockClose(request.toByteArray());
        Block.Close.Response response = Block.Close.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Close.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Create.Response blockCreate(Block.Create.Request request) throws Exception {
        byte[] encoded = Service.blockCreate(request.toByteArray());
        Block.Create.Response response = Block.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.CreatePage.Response blockCreatePage(Block.CreatePage.Request request) throws Exception {
        byte[] encoded = Service.blockCreatePage(request.toByteArray());
        Block.CreatePage.Response response = Block.CreatePage.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.CreatePage.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.TText.Response blockSetTextText(Block.Set.Text.TText.Request request) throws Exception {
        byte[] encoded = Service.blockSetTextText(request.toByteArray());
        Block.Set.Text.TText.Response response = Block.Set.Text.TText.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.TText.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.Checked.Response blockSetTextChecked(Block.Set.Text.Checked.Request request) throws Exception {
        byte[] encoded = Service.blockSetTextChecked(request.toByteArray());
        Block.Set.Text.Checked.Response response = Block.Set.Text.Checked.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Checked.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.Text.Style.Response blockSetTextStyle(BlockList.Set.Text.Style.Request request) throws Exception {
        byte[] encoded = Service.blockListSetTextStyle(request.toByteArray());
        BlockList.Set.Text.Style.Response response = BlockList.Set.Text.Style.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.Text.Style.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.Color.Response blockSetTextColor(Block.Set.Text.Color.Request request) throws Exception {
        byte[] encoded = Service.blockSetTextColor(request.toByteArray());
        Block.Set.Text.Color.Response response = Block.Set.Text.Color.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Color.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.BackgroundColor.Response blockSetTextBackgroundColor(BlockList.Set.BackgroundColor.Request request) throws Exception {
        byte[] encoded = Service.blockListSetBackgroundColor(request.toByteArray());
        BlockList.Set.BackgroundColor.Response response = BlockList.Set.BackgroundColor.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.BackgroundColor.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.Align.Response blockSetAlignment(BlockList.Set.Align.Request request) throws Exception {
        byte[] encoded = Service.blockListSetAlign(request.toByteArray());
        BlockList.Set.Align.Response response = BlockList.Set.Align.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.Align.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Move.Response blockListMove(BlockList.Move.Request request) throws Exception {
        byte[] encoded = Service.blockListMove(request.toByteArray());
        BlockList.Move.Response response = BlockList.Move.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Move.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Unlink.Response blockUnlink(Block.Unlink.Request request) throws Exception {
        byte[] encoded = Service.blockUnlink(request.toByteArray());
        Block.Unlink.Response response = Block.Unlink.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Unlink.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Merge.Response blockMerge(Block.Merge.Request request) throws Exception {
        byte[] encoded = Service.blockMerge(request.toByteArray());
        Block.Merge.Response response = Block.Merge.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Merge.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Split.Response blockSplit(Block.Split.Request request) throws Exception {
        byte[] encoded = Service.blockSplit(request.toByteArray());
        Block.Split.Response response = Block.Split.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Split.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Duplicate.Response blockListDuplicate(BlockList.Duplicate.Request request) throws Exception {
        byte[] encoded = Service.blockListDuplicate(request.toByteArray());
        BlockList.Duplicate.Response response = BlockList.Duplicate.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Duplicate.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.ConvertChildrenToPages.Response convertChildrenToPages(BlockList.ConvertChildrenToPages.Request request) throws Exception {
        byte[] encoded = Service.blockListConvertChildrenToPages(request.toByteArray());
        BlockList.ConvertChildrenToPages.Response response = BlockList.ConvertChildrenToPages.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.ConvertChildrenToPages.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Upload.Response blockUpload(Block.Upload.Request request) throws Exception {
        byte[] encoded = Service.blockUpload(request.toByteArray());
        Block.Upload.Response response = Block.Upload.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Upload.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Bookmark.Fetch.Response blockBookmarkFetch(Block.Bookmark.Fetch.Request request) throws Exception {
        byte[] encoded = Service.blockBookmarkFetch(request.toByteArray());
        Block.Bookmark.Fetch.Response response = Block.Bookmark.Fetch.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Bookmark.Fetch.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Undo.Response blockUndo(Block.Undo.Request request) throws Exception {
        byte[] encoded = Service.blockUndo(request.toByteArray());
        Block.Undo.Response response = Block.Undo.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Undo.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Redo.Response blockRedo(Block.Redo.Request request) throws Exception {
        byte[] encoded = Service.blockRedo(request.toByteArray());
        Block.Redo.Response response = Block.Redo.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Redo.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.Page.IsArchived.Response blockListSetPageIsArchived(BlockList.Set.Page.IsArchived.Request request) throws Exception {
        byte[] encoded = Service.blockListSetPageIsArchived(request.toByteArray());
        BlockList.Set.Page.IsArchived.Response response = BlockList.Set.Page.IsArchived.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.Page.IsArchived.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Details.Response blockSetDetails(Block.Set.Details.Request request) throws Exception {
        byte[] encoded = Service.blockSetDetails(request.toByteArray());
        Block.Set.Details.Response response = Block.Set.Details.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Details.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Paste.Response blockPaste(Block.Paste.Request request) throws Exception {
        byte[] encoded = Service.blockPaste(request.toByteArray());
        Block.Paste.Response response = Block.Paste.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Paste.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Copy.Response blockCopy(Block.Copy.Request request) throws Exception {
        byte[] encoded = Service.blockCopy(request.toByteArray());
        Block.Copy.Response response = Block.Copy.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Copy.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public UploadFile.Response uploadFile(UploadFile.Request request) throws Exception {
        byte[] encoded = Service.uploadFile(request.toByteArray());
        UploadFile.Response response = UploadFile.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != UploadFile.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Navigation.GetPageInfoWithLinks.Response pageInfoWithLinks(Navigation.GetPageInfoWithLinks.Request request) throws Exception {
        byte[] encoded = Service.navigationGetPageInfoWithLinks(request.toByteArray());
        Navigation.GetPageInfoWithLinks.Response response = Navigation.GetPageInfoWithLinks.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Navigation.GetPageInfoWithLinks.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Navigation.ListPages.Response listPages(Navigation.ListPages.Request request) throws Exception {
        byte[] encoded = Service.navigationListPages(request.toByteArray());
        Navigation.ListPages.Response response = Navigation.ListPages.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Navigation.ListPages.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Commands.Rpc.Page.Create.Response pageCreate(Commands.Rpc.Page.Create.Request request) throws Exception {
        byte[] encoded = Service.pageCreate(request.toByteArray());
        Commands.Rpc.Page.Create.Response response = Commands.Rpc.Page.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Commands.Rpc.Page.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Commands.Rpc.Version.Get.Response getVersion(Commands.Rpc.Version.Get.Request request) throws Exception {
        byte[] encoded = Service.versionGet(request.toByteArray());
        Commands.Rpc.Version.Get.Response response = Commands.Rpc.Version.Get.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Commands.Rpc.Version.Get.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.Div.Style.Response blockListSetDivStyle(BlockList.Set.Div.Style.Request request) throws Exception {
        byte[] encoded = Service.blockListSetDivStyle(request.toByteArray());
        BlockList.Set.Div.Style.Response response = BlockList.Set.Div.Style.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.Div.Style.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Set.Fields.Response blockListSetFields(BlockList.Set.Fields.Request request) throws Exception {
        byte[] encoded = Service.blockListSetFields(request.toByteArray());
        BlockList.Set.Fields.Response response = BlockList.Set.Fields.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Set.Fields.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }
}

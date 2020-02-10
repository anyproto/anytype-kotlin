package com.agileburo.anytype.middleware.service;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Ipfs.Image;
import anytype.Commands.Rpc.Wallet;
import lib.Lib;

public class DefaultMiddlewareService implements MiddlewareService {

    @Override
    public Config.Get.Response configGet(Config.Get.Request request) throws Exception {
        byte[] encoded = Lib.configGet(request.toByteArray());
        Config.Get.Response response = Config.Get.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Config.Get.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Wallet.Create.Response walletCreate(Wallet.Create.Request request) throws Exception {
        byte[] encoded = Lib.walletCreate(request.toByteArray());
        Wallet.Create.Response response = Wallet.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Wallet.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Create.Response accountCreate(Account.Create.Request request) throws Exception {
        byte[] encoded = Lib.accountCreate(request.toByteArray());
        Account.Create.Response response = Account.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Wallet.Recover.Response walletRecover(Wallet.Recover.Request request) throws Exception {
        byte[] encoded = Lib.walletRecover(request.toByteArray());
        Wallet.Recover.Response response = Wallet.Recover.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Wallet.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Recover.Response accountRecover(Account.Recover.Request request) throws Exception {
        byte[] encoded = Lib.accountRecover(request.toByteArray());
        Account.Recover.Response response = Account.Recover.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Stop.Response accountStop(Account.Stop.Request request) throws Exception {
        byte[] encoded = Lib.accountStop(request.toByteArray());
        Account.Stop.Response response = Account.Stop.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Stop.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Account.Select.Response accountSelect(Account.Select.Request request) throws Exception {
        byte[] encoded = Lib.accountSelect(request.toByteArray());
        Account.Select.Response response = Account.Select.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Account.Select.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Image.Get.Blob.Response imageGet(Image.Get.Blob.Request request) throws Exception {
        byte[] encoded = Lib.imageGetBlob(request.toByteArray());
        Image.Get.Blob.Response response = Image.Get.Blob.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Image.Get.Blob.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Open.Response blockOpen(Block.Open.Request request) throws Exception {
        byte[] encoded = Lib.blockOpen(request.toByteArray());
        Block.Open.Response response = Block.Open.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Close.Response blockClose(Block.Close.Request request) throws Exception {
        byte[] encoded = Lib.blockClose(request.toByteArray());
        Block.Close.Response response = Block.Close.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Close.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Create.Response blockCreate(Block.Create.Request request) throws Exception {
        byte[] encoded = Lib.blockCreate(request.toByteArray());
        Block.Create.Response response = Block.Create.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.CreatePage.Response blockCreatePage(Block.CreatePage.Request request) throws Exception {
        byte[] encoded = Lib.blockCreatePage(request.toByteArray());
        Block.CreatePage.Response response = Block.CreatePage.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.CreatePage.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.TText.Response blockSetTextText(Block.Set.Text.TText.Request request) throws Exception {
        byte[] encoded = Lib.blockSetTextText(request.toByteArray());
        Block.Set.Text.TText.Response response = Block.Set.Text.TText.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.TText.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.Checked.Response blockSetTextChecked(Block.Set.Text.Checked.Request request) throws Exception {
        byte[] encoded = Lib.blockSetTextChecked(request.toByteArray());
        Block.Set.Text.Checked.Response response = Block.Set.Text.Checked.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Checked.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.Style.Response blockSetTextStyle(Block.Set.Text.Style.Request request) throws Exception {
        byte[] encoded = Lib.blockSetTextStyle(request.toByteArray());
        Block.Set.Text.Style.Response response = Block.Set.Text.Style.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Style.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Set.Text.Color.Response blockSetTextColor(Block.Set.Text.Color.Request request) throws Exception {
        byte[] encoded = Lib.blockSetTextColor(request.toByteArray());
        Block.Set.Text.Color.Response response = Block.Set.Text.Color.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Color.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Move.Response blockListMove(BlockList.Move.Request request) throws Exception {
        byte[] encoded = Lib.blockListMove(request.toByteArray());
        BlockList.Move.Response response = BlockList.Move.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Move.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Unlink.Response blockUnlink(Block.Unlink.Request request) throws Exception {
        byte[] encoded = Lib.blockUnlink(request.toByteArray());
        Block.Unlink.Response response = Block.Unlink.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Unlink.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public Block.Merge.Response blockMerge(Block.Merge.Request request) throws Exception {
        byte[] encoded = Lib.blockMerge(request.toByteArray());
        Block.Merge.Response response = Block.Merge.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != Block.Merge.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }

    @Override
    public BlockList.Duplicate.Response blockListDuplicate(BlockList.Duplicate.Request request) throws Exception {
        byte[] encoded = Lib.blockListDuplicate(request.toByteArray());
        BlockList.Duplicate.Response response = BlockList.Duplicate.Response.parseFrom(encoded);
        if (response.getError() != null && response.getError().getCode() != BlockList.Duplicate.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response;
        }
    }
}

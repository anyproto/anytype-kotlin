package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.data.auth.model.BlockEntity;
import com.agileburo.anytype.data.auth.model.PositionEntity;
import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;

import java.util.List;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Ipfs.Image;
import anytype.Commands.Rpc.Wallet;
import anytype.model.Models;
import lib.Lib;
import timber.log.Timber;

public class Middleware {

    public String provideHomeDashboardId() throws Exception {

        Config.Get.Request request = Config.Get.Request
                .newBuilder()
                .build();

        byte[] encodedResponse = Lib.configGet(request.toByteArray());

        Config.Get.Response response = Config.Get.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Config.Get.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response.getHomeBlockId();
        }
    }

    public CreateWalletResponse createWallet(String path) throws Exception {

        Wallet.Create.Request request = Wallet.Create.Request
                .newBuilder()
                .setRootPath(path)
                .build();

        byte[] encodedResponse = Lib.walletCreate(request.toByteArray());

        Wallet.Create.Response response = Wallet.Create.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Wallet.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return new CreateWalletResponse(response.getMnemonic());
        }
    }

    public CreateAccountResponse createAccount(String name, String path) throws Exception {

        Account.Create.Request request;

        if (path != null) {
            request = Account.Create.Request
                    .newBuilder()
                    .setName(name)
                    .setAvatarLocalPath(path)
                    .build();
        } else {
            request = Account.Create.Request
                    .newBuilder()
                    .setName(name)
                    .build();
        }

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountCreate(encodedRequest);

        Account.Create.Response response = Account.Create.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Account.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return new CreateAccountResponse(
                    response.getAccount().getId(),
                    response.getAccount().getName(),
                    response.getAccount().getAvatar()
            );
        }
    }

    public void recoverWallet(String path, String mnemonic) throws Exception {

        Wallet.Recover.Request request = Wallet.Recover.Request
                .newBuilder()
                .setMnemonic(mnemonic)
                .setRootPath(path)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.walletRecover(encodedRequest);

        // TODO remove.
        if (encodedResponse == null)
            return;

        Wallet.Recover.Response response = Wallet.Recover.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Wallet.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void recoverAccount() throws Exception {

        Account.Recover.Request request = Account.Recover.Request
                .newBuilder()
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountRecover(encodedRequest);

        // TODO remove.
        if (encodedResponse == null)
            return;

        Account.Recover.Response response = Account.Recover.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Account.Recover.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public SelectAccountResponse selectAccount(String id, String path) throws Exception {

        Account.Select.Request request = Account.Select.Request
                .newBuilder()
                .setId(id)
                .setRootPath(path)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountSelect(encodedRequest);

        Account.Select.Response response = Account.Select.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Account.Select.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return new SelectAccountResponse(
                    response.getAccount().getId(),
                    response.getAccount().getName(),
                    response.getAccount().getAvatar()
            );
        }
    }

    public byte[] loadImage(String id, Models.Image.Size size) throws Exception {

        Image.Get.Blob.Request request = Image.Get.Blob.Request
                .newBuilder()
                .setId(id)
                .setSize(size)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.imageGetBlob(encodedRequest);

        Image.Get.Blob.Response response = Image.Get.Blob.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Image.Get.Blob.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response.getBlob().toByteArray();
        }
    }

    public void openDashboard(String contextId, String id) throws Exception {

        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockOpen(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        Timber.d(response.toString());

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void openBlock(String id) throws Exception {

        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockOpen(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public String createPage(String parentId) throws Exception {

        Models.Block.Content.Page page = Models.Block.Content.Page
                .newBuilder()
                .setStyle(Models.Block.Content.Page.Style.Empty)
                .build();

        Models.Block block = Models.Block
                .newBuilder()
                .setPage(page)
                .build();

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(parentId)
                .setParentId(parentId)
                .setBlock(block)
                .setPosition(Models.Block.Position.After)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockCreate(encodedRequest);

        Block.Create.Response response = Block.Create.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response.getBlockId();
        }
    }

    public void closePage(String id) throws Exception {

        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockClose(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void closeDashboard(String id) throws Exception {

        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockClose(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void updateText(
            String contextId,
            String blockId,
            String text,
            List<Models.Block.Content.Text.Mark> marks
    ) throws Exception {

        Timber.d("Updating block with the follwing text:" + text);

        Models.Block.Content.Text.Marks markup = Models.Block.Content.Text.Marks
                .newBuilder()
                .addAllMarks(marks)
                .build();

        Block.Set.Text.Editable.Request request = Block.Set.Text.Editable.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(blockId)
                .setMarks(markup)
                .setText(text)
                .build();

        Timber.d("Updating block with the following request:\n" + request.toString());

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockSetTextText(encodedRequest);

        Block.Set.Text.Editable.Response response = Block.Set.Text.Editable.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Editable.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void createBlock(
            String contextId,
            String targetId,
            PositionEntity position,
            BlockEntity prototype
    ) throws Exception {

        Models.Block.Content.Text content = Models.Block.Content.Text
                .newBuilder()
                .build();

        Models.Block block = Models.Block
                .newBuilder()
                .setText(content)
                .build();

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(contextId)
                .setTargetId(targetId)
                .setParentId(contextId)
                .setPosition(Models.Block.Position.After)
                .setBlock(block)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockCreate(encodedRequest);

        Block.Create.Response response = Block.Create.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }
}

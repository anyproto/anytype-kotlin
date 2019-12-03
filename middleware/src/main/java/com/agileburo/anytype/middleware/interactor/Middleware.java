package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.Ipfs.Image;
import anytype.Commands.Rpc.Wallet;
import anytype.model.Models;
import lib.Lib;
import timber.log.Timber;

public class Middleware {

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
                .setId(id)
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
                .setId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockOpen(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void closePage(String id) throws Exception {

        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setId(id)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockClose(encodedRequest);

        Block.Open.Response response = Block.Open.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Open.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }
}

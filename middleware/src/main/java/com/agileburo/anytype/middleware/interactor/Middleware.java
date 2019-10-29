package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;

import anytype.Commands.AccountCreateRequest;
import anytype.Commands.AccountCreateResponse;
import anytype.Commands.AccountRecoverRequest;
import anytype.Commands.AccountRecoverResponse;
import anytype.Commands.AccountSelectRequest;
import anytype.Commands.AccountSelectResponse;
import anytype.Commands.ImageGetBlobRequest;
import anytype.Commands.ImageGetBlobResponse;
import anytype.Commands.WalletCreateRequest;
import anytype.Commands.WalletCreateResponse;
import anytype.Commands.WalletRecoverRequest;
import anytype.Commands.WalletRecoverResponse;
import anytype.Models;
import lib.Lib;

public class Middleware {

    public CreateWalletResponse createWallet(String path) throws Exception {

        WalletCreateRequest request = WalletCreateRequest
                .newBuilder()
                .setRootPath(path)
                .build();

        byte[] encodedResponse = Lib.walletCreate(request.toByteArray());

        WalletCreateResponse response = WalletCreateResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != WalletCreateResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return new CreateWalletResponse(response.getMnemonic());
        }
    }

    public CreateAccountResponse createAccount(String name, String path) throws Exception {

        AccountCreateRequest request;

        if (path != null) {
            request = AccountCreateRequest
                    .newBuilder()
                    .setUsername(name)
                    .setAvatarLocalPath(path)
                    .build();
        } else {
            request = AccountCreateRequest
                    .newBuilder()
                    .setUsername(name)
                    .build();
        }

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountCreate(encodedRequest);

        AccountCreateResponse response = AccountCreateResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != AccountCreateResponse.Error.Code.NULL) {
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

        WalletRecoverRequest request = WalletRecoverRequest
                .newBuilder()
                .setMnemonic(mnemonic)
                .setRootPath(path)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.walletRecover(encodedRequest);

        // TODO remove.
        if (encodedResponse == null)
            return;

        WalletRecoverResponse response = WalletRecoverResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != WalletRecoverResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void recoverAccount() throws Exception {

        AccountRecoverRequest request = AccountRecoverRequest
                .newBuilder()
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountRecover(encodedRequest);

        // TODO remove.
        if (encodedResponse == null)
            return;

        AccountRecoverResponse response = AccountRecoverResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != AccountRecoverResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public SelectAccountResponse selectAccount(String id, String path) throws Exception {

        AccountSelectRequest request = AccountSelectRequest
                .newBuilder()
                .setId(id)
                .setRootPath(path)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountSelect(encodedRequest);

        AccountSelectResponse response = AccountSelectResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != AccountSelectResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return new SelectAccountResponse(
                    response.getAccount().getId(),
                    response.getAccount().getName()
            );
        }
    }

    public byte[] loadImage(String id, Models.ImageSize size) throws Exception {

        ImageGetBlobRequest request = ImageGetBlobRequest
                .newBuilder()
                .setId(id)
                .setSize(size)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.imageGetBlob(encodedRequest);

        ImageGetBlobResponse response = ImageGetBlobResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != ImageGetBlobResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        } else {
            return response.getBlob().toByteArray();
        }
    }
}

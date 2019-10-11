package com.agileburo.anytype.middleware.interactor;

import anytype.Commands.*;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;
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

    public void createAccount(String name) throws Exception {

        AccountCreateRequest request = AccountCreateRequest
                .newBuilder()
                .setUsername(name)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.accountCreate(encodedRequest);

        // TODO remove.
        if (encodedResponse == null)
            return;

        AccountCreateResponse response = AccountCreateResponse.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != AccountCreateResponse.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
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
}

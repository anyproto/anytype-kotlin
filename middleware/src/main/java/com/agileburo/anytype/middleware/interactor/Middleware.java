package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.data.auth.model.BlockEntity;
import com.agileburo.anytype.data.auth.model.CommandEntity;
import com.agileburo.anytype.data.auth.model.PositionEntity;
import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;

import java.util.List;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
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
                .setBlock(block)
                .setPosition(Models.Block.Position.Inner)
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

        Block.Set.Text.TText.Request request = Block.Set.Text.TText.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(blockId)
                .setMarks(markup)
                .setText(text)
                .build();

        Timber.d("Updating block with the following request:\n" + request.toString());

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockSetTextText(encodedRequest);

        Block.Set.Text.TText.Response response = Block.Set.Text.TText.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.TText.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void updateCheckbox(
            String context,
            String target,
            boolean isChecked
    ) throws Exception {

        Block.Set.Text.Checked.Request request = Block.Set.Text.Checked.Request
                .newBuilder()
                .setContextId(context)
                .setBlockId(target)
                .setChecked(isChecked)
                .build();

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockSetTextChecked(encodedRequest);

        Block.Set.Text.Checked.Response response = Block.Set.Text.Checked.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Set.Text.Checked.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void createBlock(
            String contextId,
            String targetId,
            PositionEntity position,
            BlockEntity.Prototype prototype
    ) throws Exception {

        Models.Block.Content.Text contentModel = null;

        if (prototype instanceof BlockEntity.Prototype.Text) {

            BlockEntity.Content.Text.Style style = ((BlockEntity.Prototype.Text) prototype).getStyle();

            switch (style) {
                case P:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Paragraph)
                            .build();
                    break;
                case H1:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header1)
                            .build();
                    break;
                case H2:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header2)
                            .build();
                    break;
                case H3:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header3)
                            .build();
                    break;
                case H4:
                    throw new IllegalStateException("Unexpected prototype text style");
                case TITLE:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header4)
                            .build();
                    break;
                case QUOTE:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Quote)
                            .build();
                    break;
                case CODE_SNIPPET:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Code)
                            .build();
                    break;
                case BULLET:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Marked)
                            .build();
                    break;
                case CHECKBOX:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Checkbox)
                            .build();
                    break;
                case NUMBERED:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Numbered)
                            .build();
                    break;
                case TOGGLE:
                    contentModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Toggle)
                            .build();
                    break;
            }
        }

        if (contentModel == null) {
            throw new IllegalStateException("Could not create content from the given prototype");
        }

        Models.Block.Position positionModel = null;

        switch (position) {
            case NONE:
                positionModel = Models.Block.Position.None;
                break;
            case TOP:
                positionModel = Models.Block.Position.Top;
                break;
            case BOTTOM:
                positionModel = Models.Block.Position.Bottom;
                break;
            case LEFT:
                positionModel = Models.Block.Position.Left;
                break;
            case RIGHT:
                positionModel = Models.Block.Position.Right;
                break;
            case INNER:
                positionModel = Models.Block.Position.Inner;
                break;
        }

        Models.Block blockModel = Models.Block
                .newBuilder()
                .setText(contentModel)
                .build();

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(contextId)
                .setTargetId(targetId)
                .setPosition(positionModel)
                .setBlock(blockModel)
                .build();

        Timber.d("Creating block with the following request:\n" + request.toString());

        byte[] encodedRequest = request.toByteArray();

        byte[] encodedResponse = Lib.blockCreate(encodedRequest);

        Block.Create.Response response = Block.Create.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != Block.Create.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }

    public void dnd(CommandEntity.Dnd command) throws Exception {

        Models.Block.Position positionModel = null;

        switch (command.getPosition()) {
            case NONE:
                positionModel = Models.Block.Position.None;
                break;
            case TOP:
                positionModel = Models.Block.Position.Top;
                break;
            case BOTTOM:
                positionModel = Models.Block.Position.Bottom;
                break;
            case LEFT:
                positionModel = Models.Block.Position.Left;
                break;
            case RIGHT:
                positionModel = Models.Block.Position.Right;
                break;
            case INNER:
                positionModel = Models.Block.Position.Inner;
                break;
        }

        BlockList.Move.Request request = BlockList.Move.Request
                .newBuilder()
                .setContextId(command.getContextId())
                .setPosition(positionModel)
                .addAllBlockIds(command.getBlockIds())
                .setDropTargetId(command.getDropTargetId())
                .build();

        byte[] encodedResponse = Lib.blockListMove(request.toByteArray());

        BlockList.Move.Response response = BlockList.Move.Response.parseFrom(encodedResponse);

        if (response.getError() != null && response.getError().getCode() != BlockList.Move.Response.Error.Code.NULL) {
            throw new Exception(response.getError().getDescription());
        }
    }
}

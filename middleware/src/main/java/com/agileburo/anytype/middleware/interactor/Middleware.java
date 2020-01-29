package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.data.auth.model.BlockEntity;
import com.agileburo.anytype.data.auth.model.CommandEntity;
import com.agileburo.anytype.data.auth.model.PositionEntity;
import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;
import com.agileburo.anytype.middleware.service.MiddlewareService;

import java.util.List;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Ipfs.Image;
import anytype.Commands.Rpc.Wallet;
import anytype.model.Models;
import timber.log.Timber;

public class Middleware {

    private final MiddlewareService service;

    public Middleware(MiddlewareService service) {
        this.service = service;
    }

    public String provideHomeDashboardId() throws Exception {
        Config.Get.Request request = Config.Get.Request.newBuilder().build();
        Config.Get.Response response = service.configGet(request);
        return response.getHomeBlockId();
    }

    public CreateWalletResponse createWallet(String path) throws Exception {
        Wallet.Create.Request request = Wallet.Create.Request
                .newBuilder()
                .setRootPath(path)
                .build();

        Wallet.Create.Response response = service.walletCreate(request);

        return new CreateWalletResponse(response.getMnemonic());
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

        Account.Create.Response response = service.accountCreate(request);

        return new CreateAccountResponse(
                response.getAccount().getId(),
                response.getAccount().getName(),
                response.getAccount().getAvatar()
        );
    }

    public void recoverWallet(String path, String mnemonic) throws Exception {
        Wallet.Recover.Request request = Wallet.Recover.Request
                .newBuilder()
                .setMnemonic(mnemonic)
                .setRootPath(path)
                .build();

        service.walletRecover(request);
    }

    public void recoverAccount() throws Exception {
        Account.Recover.Request request = Account.Recover.Request.newBuilder().build();
        service.accountRecover(request);
    }

    public SelectAccountResponse selectAccount(String id, String path) throws Exception {
        Account.Select.Request request = Account.Select.Request
                .newBuilder()
                .setId(id)
                .setRootPath(path)
                .build();

        Account.Select.Response response = service.accountSelect(request);

        return new SelectAccountResponse(
                response.getAccount().getId(),
                response.getAccount().getName(),
                response.getAccount().getAvatar()
        );
    }

    public byte[] loadImage(String id, Models.Image.Size size) throws Exception {
        Image.Get.Blob.Request request = Image.Get.Blob.Request
                .newBuilder()
                .setId(id)
                .setSize(size)
                .build();

        Image.Get.Blob.Response response = service.imageGet(request);

        return response.getBlob().toByteArray();
    }

    public void openDashboard(String contextId, String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(id)
                .build();

        service.blockOpen(request);
    }

    public void openBlock(String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        service.blockOpen(request);
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

        Block.Create.Response response = service.blockCreate(request);
        return response.getBlockId();
    }

    public void closePage(String id) throws Exception {
        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        service.blockClose(request);
    }

    public void closeDashboard(String id) throws Exception {
        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        service.blockClose(request);
    }

    public void updateText(
            String contextId,
            String blockId,
            String text,
            List<Models.Block.Content.Text.Mark> marks
    ) throws Exception {
        Timber.d("Updating block with the follwing text:\n%s", text);

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

        Timber.d("Updating block with the following request:\n%s", request.toString());

        service.blockSetTextText(request);
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

        service.blockSetTextChecked(request);
    }

    public void updateTextStyle(CommandEntity.UpdateStyle command) throws Exception {

        Models.Block.Content.Text.Style style = null;

        switch (command.getStyle()) {
            case P:
                style = Models.Block.Content.Text.Style.Paragraph;
                break;
            case H1:
                style = Models.Block.Content.Text.Style.Header1;
                break;
            case H2:
                style = Models.Block.Content.Text.Style.Header2;
                break;
            case H3:
                style = Models.Block.Content.Text.Style.Header3;
                break;
            case H4:
                style = Models.Block.Content.Text.Style.Header4;
                break;
            case TITLE:
                style = Models.Block.Content.Text.Style.Title;
                break;
            case QUOTE:
                style = Models.Block.Content.Text.Style.Quote;
                break;
            case CODE_SNIPPET:
                style = Models.Block.Content.Text.Style.Code;
                break;
            case BULLET:
                style = Models.Block.Content.Text.Style.Marked;
                break;
            case CHECKBOX:
                style = Models.Block.Content.Text.Style.Checkbox;
                break;
            case NUMBERED:
                style = Models.Block.Content.Text.Style.Numbered;
                break;
            case TOGGLE:
                style = Models.Block.Content.Text.Style.Toggle;
                break;
        }

        Block.Set.Text.Style.Request request = Block.Set.Text.Style.Request
                .newBuilder()
                .setStyle(style)
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .build();

        service.blockSetTextStyle(request);
    }

    public void updateTextColor(CommandEntity.UpdateTextColor command) throws Exception {

        Block.Set.Text.Color.Request request = Block.Set.Text.Color.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setBlockId(command.getTarget())
                .setColor(command.getColor())
                .build();

        Timber.d("Updating text color with the following request:\n%s", request.toString());

        service.blockSetTextColor(request);
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
                            .setStyle(Models.Block.Content.Text.Style.Title)
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

        Timber.d("Creating block with the following request:\n%s", request.toString());

        service.blockCreate(request);
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

        service.blockListMove(request);
    }

    public String duplicate(CommandEntity.Duplicate command) throws Exception {
        Block.Duplicate.Request request = Block.Duplicate.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setTargetId(command.getOriginal())
                .setBlockId(command.getOriginal())
                .setPosition(Models.Block.Position.Bottom)
                .build();

        Timber.d("Duplicating blocks with the following request:\n%s", request.toString());

        Block.Duplicate.Response response = service.blockDuplicate(request);

        return response.getBlockId();
    }

    public void unlink(CommandEntity.Unlink command) throws Exception {
        Block.Unlink.Request request = Block.Unlink.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTargets())
                .build();

        Timber.d("Unlinking blocks with the following request:\n%s", request.toString());

        service.blockUnlink(request);
    }
}

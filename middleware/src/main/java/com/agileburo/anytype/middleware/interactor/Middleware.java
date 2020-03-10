package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.data.auth.model.BlockEntity;
import com.agileburo.anytype.data.auth.model.CommandEntity;
import com.agileburo.anytype.data.auth.model.ConfigEntity;
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
import anytype.Commands.Rpc.Wallet;
import anytype.model.Models;
import timber.log.Timber;

public class Middleware {

    private final MiddlewareService service;

    public Middleware(MiddlewareService service) {
        this.service = service;
    }

    public ConfigEntity getConfig() throws Exception {
        Config.Get.Request request = Config.Get.Request.newBuilder().build();
        Config.Get.Response response = service.configGet(request);
        Timber.d("Got config:\n%s", response.toString());
        return new ConfigEntity(response.getHomeBlockId(), response.getGatewayUrl());
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

    public void logout() throws Exception {
        Account.Stop.Request request = Account.Stop.Request
                .newBuilder()
                .build();

        service.accountStop(request);
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

    public void openDashboard(String contextId, String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(id)
                .build();

        Timber.d("Opening home dashboard with the following request:\n%s", request.toString());

        service.blockOpen(request);
    }

    public void openBlock(String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        Timber.d("Opening page with the following request:\n%s", request.toString());

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

        Block.CreatePage.Request request = Block.CreatePage.Request
                .newBuilder()
                .setContextId(parentId)
                .setBlock(block)
                .setPosition(Models.Block.Position.Inner)
                .build();

        Timber.d("Creating page with the following request:\n%s", request.toString());

        Block.CreatePage.Response response = service.blockCreatePage(request);
        return response.getTargetId();
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

        Timber.d("Closing dashboard with the following request:\n%s", request.toString());

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

    public void updateBackgroundColor(CommandEntity.UpdateBackgroundColor command) throws Exception {
        Block.Set.Text.BackgroundColor.Request request = Block.Set.Text.BackgroundColor.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setBlockId(command.getTarget())
                .setColor(command.getColor())
                .build();

        Timber.d("Updating background color with the following request:\n%s", request.toString());

        service.blockSetTextBackgroundColor(request);
    }

    public String createBlock(
            String contextId,
            String targetId,
            PositionEntity position,
            BlockEntity.Prototype prototype
    ) throws Exception {

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

        Models.Block.Content.Text textBlockModel = null;
        Models.Block.Content.Page pageBlockModel = null;
        Models.Block.Content.Div dividerBlockModel = null;

        if (prototype instanceof BlockEntity.Prototype.Text) {

            BlockEntity.Content.Text.Style style = ((BlockEntity.Prototype.Text) prototype).getStyle();

            switch (style) {
                case P:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Paragraph)
                            .build();
                    break;
                case H1:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header1)
                            .build();
                    break;
                case H2:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header2)
                            .build();
                    break;
                case H3:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Header3)
                            .build();
                    break;
                case H4:
                    throw new IllegalStateException("Unexpected prototype text style");
                case TITLE:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Title)
                            .build();
                    break;
                case QUOTE:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Quote)
                            .build();
                    break;
                case CODE_SNIPPET:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Code)
                            .build();
                    break;
                case BULLET:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Marked)
                            .build();
                    break;
                case CHECKBOX:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Checkbox)
                            .build();
                    break;
                case NUMBERED:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Numbered)
                            .build();
                    break;
                case TOGGLE:
                    textBlockModel = Models.Block.Content.Text
                            .newBuilder()
                            .setStyle(Models.Block.Content.Text.Style.Toggle)
                            .build();
                    break;
            }
        } else if (prototype instanceof BlockEntity.Prototype.Page) {
            pageBlockModel = Models.Block.Content.Page
                    .newBuilder()
                    .setStyle(Models.Block.Content.Page.Style.Empty)
                    .build();
        } else if (prototype instanceof BlockEntity.Prototype.Divider) {
            dividerBlockModel = Models.Block.Content.Div
                    .newBuilder()
                    .setStyle(Models.Block.Content.Div.Style.Line)
                    .build();
        }

        Models.Block blockModel = null;

        if (textBlockModel != null) {
            blockModel = Models.Block
                    .newBuilder()
                    .setText(textBlockModel)
                    .build();
        } else if (pageBlockModel != null) {
            blockModel = Models.Block
                    .newBuilder()
                    .setPage(pageBlockModel)
                    .build();
        } else if (dividerBlockModel != null) {
            blockModel = Models.Block
                    .newBuilder()
                    .setDiv(dividerBlockModel)
                    .build();
        }

        if (blockModel == null) {
            throw new IllegalStateException("Could not create content from the following prototype: " + prototype.toString());
        }

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(contextId)
                .setTargetId(targetId)
                .setPosition(positionModel)
                .setBlock(blockModel)
                .build();

        Timber.d("Creating block with the following request:\n%s", request.toString());

        Block.Create.Response response = service.blockCreate(request);

        return response.getBlockId();
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
        BlockList.Duplicate.Request request = BlockList.Duplicate.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setTargetId(command.getOriginal())
                .addBlockIds(command.getOriginal())
                .setPosition(Models.Block.Position.Bottom)
                .build();

        Timber.d("Duplicating blocks with the following request:\n%s", request.toString());

        BlockList.Duplicate.Response response = service.blockListDuplicate(request);

        return response.getBlockIds(0);
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

    public void merge(CommandEntity.Merge command) throws Exception {
        Block.Merge.Request request = Block.Merge.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setFirstBlockId(command.getPair().getFirst())
                .setSecondBlockId(command.getPair().getSecond())
                .build();

        Timber.d("Merging blocks with the following request:\n%s", request.toString());

        service.blockMerge(request);
    }

    public String split(CommandEntity.Split command) throws Exception {
        Block.Split.Request request = Block.Split.Request
                .newBuilder()
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .setCursorPosition(command.getIndex())
                .build();

        Timber.d("Splitting the target block with the following request:\n%s", request.toString());

        Block.Split.Response response = service.blockSplit(request);

        return response.getBlockId();
    }

    public void setIconName(CommandEntity.SetIconName command) throws Exception {
        Block.Set.Icon.Name.Request request = Block.Set.Icon.Name.Request
                .newBuilder()
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .setName(command.getName())
                .build();

        Timber.d("Setting icon name with the following request:\n%s", request.toString());

        service.blockSetIconName(request);
    }
}

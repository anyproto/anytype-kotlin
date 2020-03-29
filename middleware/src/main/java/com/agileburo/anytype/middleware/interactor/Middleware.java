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
import kotlin.Pair;
import timber.log.Timber;

public class Middleware {

    private final MiddlewareService service;
    private final MiddlewareFactory factory;
    private final MiddlewareMapper mapper;

    public Middleware(
            MiddlewareService service,
            MiddlewareFactory factory,
            MiddlewareMapper mapper
    ) {
        this.service = service;
        this.factory = factory;
        this.mapper = mapper;
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

        Models.Block.Content.Text.Style style = mapper.toMiddleware(command.getStyle());

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

    public void uploadMediaBlockContent(CommandEntity.UploadBlock command) throws Exception {
        Block.Upload.Request request = Block.Upload.Request
                .newBuilder()
                .setFilePath(command.getFilePath())
                .setUrl(command.getUrl())
                .setContextId(command.getContextId())
                .setBlockId(command.getBlockId())
                .build();

        Timber.d("Upload video block url with the following request:\n%s", request.toString());

        service.blockUpload(request);
    }

    public String createBlock(
            String contextId,
            String targetId,
            PositionEntity position,
            BlockEntity.Prototype prototype
    ) throws Exception {

        Models.Block.Position positionModel = mapper.toMiddleware(position);

        Models.Block model = factory.create(prototype);

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(contextId)
                .setTargetId(targetId)
                .setPosition(positionModel)
                .setBlock(model)
                .build();

        Timber.d("Creating block with the following request:\n%s", request.toString());

        Block.Create.Response response = service.blockCreate(request);

        return response.getBlockId();
    }

    public Pair<String, String> createDocument(CommandEntity.CreateDocument command) throws Exception {

        Models.Block.Position position = mapper.toMiddleware(command.getPosition());

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
                .setContextId(command.getContext())
                .setTargetId(command.getTarget())
                .setPosition(position)
                .setBlock(block)
                .build();

        Timber.d("Creating new document with the following request:\n%s", request.toString());

        Block.CreatePage.Response response = service.blockCreatePage(request);

        return new Pair<>(response.getBlockId(), response.getTargetId());
    }

    public void dnd(CommandEntity.Dnd command) throws Exception {
        Models.Block.Position positionModel = mapper.toMiddleware(command.getPosition());

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

    public void setupBookmark(CommandEntity.SetupBookmark command) throws Exception {
        Block.Bookmark.Fetch.Request request = Block.Bookmark.Fetch.Request
                .newBuilder()
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .setUrl(command.getUrl())
                .build();

        Timber.d("Fetching bookmark with the following request:\n%s", request.toString());

        service.blockBookmarkFetch(request);
    }

    public void undo(CommandEntity.Undo command) throws Exception {
        Block.Undo.Request request = Block.Undo.Request
                .newBuilder()
                .setContextId(command.getContext())
                .build();

        Timber.d("Undoing changes with the following request:\n%s", request.toString());

        service.blockUndo(request);
    }

    public void redo(CommandEntity.Redo command) throws Exception {
        Block.Redo.Request request = Block.Redo.Request
                .newBuilder()
                .setContextId(command.getContext())
                .build();

        Timber.d("Redoing changes with the following request:\n%s", request.toString());

        service.blockRedo(request);
    }

    public void archiveDocument(CommandEntity.ArchiveDocument command) throws Exception {
        Block.Set.Page.IsArchived.Request request = Block.Set.Page.IsArchived.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setBlockId(command.getTarget())
                .setIsArchived(true)
                .build();

        Timber.d("Archiving document with the following request:\n%s", request.toString());

        service.blockSetPageIsArchived(request);
    }
}

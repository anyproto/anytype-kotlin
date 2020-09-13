package com.agileburo.anytype.middleware.interactor;

import com.agileburo.anytype.data.auth.model.BlockEntity;
import com.agileburo.anytype.data.auth.model.CommandEntity;
import com.agileburo.anytype.data.auth.model.ConfigEntity;
import com.agileburo.anytype.data.auth.model.PayloadEntity;
import com.agileburo.anytype.data.auth.model.PositionEntity;
import com.agileburo.anytype.data.auth.model.Response;
import com.agileburo.anytype.middleware.BuildConfig;
import com.agileburo.anytype.middleware.model.CreateAccountResponse;
import com.agileburo.anytype.middleware.model.CreateWalletResponse;
import com.agileburo.anytype.middleware.model.SelectAccountResponse;
import com.agileburo.anytype.middleware.service.MiddlewareService;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.List;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Navigation;
import anytype.Commands.Rpc.UploadFile;
import anytype.Commands.Rpc.Wallet;
import anytype.model.Localstore;
import anytype.model.Models;
import anytype.model.Models.Range;
import kotlin.Pair;
import kotlin.Triple;
import timber.log.Timber;

public class Middleware {

    private final String iconEmojiKey = "iconEmoji";
    private final String iconImageKey = "iconImage";
    private final String nameKey = "name";

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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Config.Get.Response response = service.configGet(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new ConfigEntity(
                response.getHomeBlockId(),
                response.getProfileBlockId(),
                response.getGatewayUrl()
        );
    }

    public CreateWalletResponse createWallet(String path) throws Exception {
        Wallet.Create.Request request = Wallet.Create.Request
                .newBuilder()
                .setRootPath(path)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Wallet.Create.Response response = service.walletCreate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new CreateWalletResponse(response.getMnemonic());
    }

    public CreateAccountResponse createAccount(String name, String path, String invitationCode) throws Exception {

        Account.Create.Request request;

        if (path != null) {
            request = Account.Create.Request
                    .newBuilder()
                    .setName(name)
                    .setAvatarLocalPath(path)
                    .setAlphaInviteCode(invitationCode)
                    .build();
        } else {
            request = Account.Create.Request
                    .newBuilder()
                    .setName(name)
                    .setAlphaInviteCode(invitationCode)
                    .build();
        }

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Account.Create.Response response = service.accountCreate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Wallet.Recover.Response response = service.walletRecover(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public void logout() throws Exception {
        Account.Stop.Request request = Account.Stop.Request
                .newBuilder()
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Account.Stop.Response response = service.accountStop(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Account.Select.Response response = service.accountSelect(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new SelectAccountResponse(
                response.getAccount().getId(),
                response.getAccount().getName(),
                response.getAccount().getAvatar()
        );
    }

    public PayloadEntity openDashboard(String contextId, String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setContextId(contextId)
                .setBlockId(id)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Open.Response response = service.blockOpen(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity openBlock(String id) throws Exception {
        Block.Open.Request request = Block.Open.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Open.Response response = service.blockOpen(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public String createPage(String parentId, String emoji) throws Exception {

        Value emojiValue = null;

        if (emoji != null) {
            emojiValue = Value.newBuilder().setStringValue(emoji).build();
        }

        Struct details;

        if (emojiValue != null) {
            details = Struct
                    .newBuilder()
                    .putFields(iconEmojiKey, emojiValue)
                    .build();
        } else {
            details = Struct.getDefaultInstance();
        }

        Block.CreatePage.Request request = Block.CreatePage.Request
                .newBuilder()
                .setContextId(parentId)
                .setDetails(details)
                .setPosition(Models.Block.Position.Inner)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.CreatePage.Response response = service.blockCreatePage(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return response.getTargetId();
    }

    public void closePage(String id) throws Exception {
        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Close.Response response = service.blockClose(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public void closeDashboard(String id) throws Exception {
        Block.Close.Request request = Block.Close.Request
                .newBuilder()
                .setBlockId(id)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Close.Response response = service.blockClose(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public void updateDocumentTitle(CommandEntity.UpdateTitle command) throws Exception {

        Value value = Value.newBuilder().setStringValue(command.getTitle()).build();

        Block.Set.Details.Detail details = Block.Set.Details.Detail
                .newBuilder()
                .setKey(nameKey)
                .setValue(value)
                .build();

        Block.Set.Details.Request request = Block.Set.Details.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addDetails(details)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Details.Response response = service.blockSetDetails(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public void updateText(
            String contextId,
            String blockId,
            String text,
            List<Models.Block.Content.Text.Mark> marks
    ) throws Exception {

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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Text.TText.Response response = service.blockSetTextText(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public PayloadEntity updateCheckbox(
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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Text.Checked.Response response = service.blockSetTextChecked(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity updateTextStyle(CommandEntity.UpdateStyle command) throws Exception {

        Models.Block.Content.Text.Style style = mapper.toMiddleware(command.getStyle());

        BlockList.Set.Text.Style.Request request = BlockList.Set.Text.Style.Request
                .newBuilder()
                .setStyle(style)
                .addAllBlockIds(command.getTargets())
                .setContextId(command.getContext())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Set.Text.Style.Response response = service.blockSetTextStyle(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity updateTextColor(CommandEntity.UpdateTextColor command) throws Exception {
        Block.Set.Text.Color.Request request = Block.Set.Text.Color.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setBlockId(command.getTarget())
                .setColor(command.getColor())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Text.Color.Response response = service.blockSetTextColor(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity updateBackgroundColor(CommandEntity.UpdateBackgroundColor command) throws Exception {
        BlockList.Set.BackgroundColor.Request request = BlockList.Set.BackgroundColor.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTargets())
                .setColor(command.getColor())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Set.BackgroundColor.Response response = service.blockSetTextBackgroundColor(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity updateAlignment(CommandEntity.UpdateAlignment command) throws Exception {

        Models.Block.Align align = mapper.toMiddleware(command.getAlignment());

        BlockList.Set.Align.Request request = BlockList.Set.Align.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTargets())
                .setAlignValue(align.getNumber())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Set.Align.Response response = service.blockSetAlignment(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity uploadBlock(CommandEntity.UploadBlock command) throws Exception {
        Block.Upload.Request request = Block.Upload.Request
                .newBuilder()
                .setFilePath(command.getFilePath())
                .setUrl(command.getUrl())
                .setContextId(command.getContextId())
                .setBlockId(command.getBlockId())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Upload.Response response = service.blockUpload(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public Pair<String, PayloadEntity> createBlock(
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

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Create.Response response = service.blockCreate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Pair<>(response.getBlockId(), mapper.toPayload(response.getEvent()));
    }

    public Pair<String, PayloadEntity> replace(CommandEntity.Replace command) throws Exception {
        Models.Block model = factory.create(command.getPrototype());

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setTargetId(command.getTarget())
                .setPosition(Models.Block.Position.Replace)
                .setBlock(model)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Create.Response response = service.blockCreate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Pair<>(response.getBlockId(), mapper.toPayload(response.getEvent()));
    }

    public Triple<String, String, PayloadEntity> createDocument(CommandEntity.CreateDocument command) throws Exception {

        Value emojiValue = null;

        if (command.getEmoji() != null) {
            emojiValue = Value.newBuilder().setStringValue(command.getEmoji()).build();
        }

        Struct details;

        if (emojiValue != null) {
            details = Struct
                    .newBuilder()
                    .putFields(iconEmojiKey, emojiValue)
                    .build();
        } else {
            details = Struct.getDefaultInstance();
        }

        Models.Block.Position position = mapper.toMiddleware(command.getPosition());

        Block.CreatePage.Request request = Block.CreatePage.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setTargetId(command.getTarget())
                .setPosition(position)
                .setDetails(details)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.CreatePage.Response response = service.blockCreatePage(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Triple<>(
                response.getBlockId(),
                response.getTargetId(),
                mapper.toPayload(response.getEvent())
        );
    }

    public PayloadEntity move(CommandEntity.Move command) throws Exception {
        Models.Block.Position position = mapper.toMiddleware(command.getPosition());

        BlockList.Move.Request request = BlockList.Move.Request
                .newBuilder()
                .setContextId(command.getContextId())
                .setTargetContextId(command.getDropTargetContextId())
                .setPosition(position)
                .addAllBlockIds(command.getBlockIds())
                .setDropTargetId(command.getDropTargetId())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Move.Response response = service.blockListMove(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public Pair<String, PayloadEntity> duplicate(CommandEntity.Duplicate command) throws Exception {
        BlockList.Duplicate.Request request = BlockList.Duplicate.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setTargetId(command.getOriginal())
                .addBlockIds(command.getOriginal())
                .setPosition(Models.Block.Position.Bottom)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Duplicate.Response response = service.blockListDuplicate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Pair<>(response.getBlockIds(0), mapper.toPayload(response.getEvent()));
    }

    public PayloadEntity unlink(CommandEntity.Unlink command) throws Exception {
        Block.Unlink.Request request = Block.Unlink.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTargets())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Unlink.Response response = service.blockUnlink(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity merge(CommandEntity.Merge command) throws Exception {
        Block.Merge.Request request = Block.Merge.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setFirstBlockId(command.getPair().getFirst())
                .setSecondBlockId(command.getPair().getSecond())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Merge.Response response = service.blockMerge(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public Pair<String, PayloadEntity> split(CommandEntity.Split command) throws Exception {

        Models.Block.Content.Text.Style style = mapper.toMiddleware(command.getStyle());

        Range range = Range
                .newBuilder()
                .setFrom(command.getIndex())
                .setTo(command.getIndex())
                .build();

        Block.Split.Request request = Block.Split.Request
                .newBuilder()
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .setStyle(style)
                .setRange(range)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Split.Response response = service.blockSplit(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Pair<>(response.getBlockId(), mapper.toPayload(response.getEvent()));
    }

    public void setDocumentEmojiIcon(CommandEntity.SetDocumentEmojiIcon command) throws Exception {

        Value emojiValue = Value.newBuilder().setStringValue(command.getEmoji()).build();
        Value imageValue = Value.newBuilder().setStringValue("").build();

        Block.Set.Details.Detail emojiDetail = Block.Set.Details.Detail
                .newBuilder()
                .setKey(iconEmojiKey)
                .setValue(emojiValue)
                .build();

        Block.Set.Details.Detail imageDetail = Block.Set.Details.Detail
                .newBuilder()
                .setKey(iconImageKey)
                .setValue(imageValue)
                .build();

        Block.Set.Details.Request request = Block.Set.Details.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addDetails(emojiDetail)
                .addDetails(imageDetail)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Details.Response response = service.blockSetDetails(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public void setDocumentImageIcon(CommandEntity.SetDocumentImageIcon command) throws Exception {

        Value imageValue = Value.newBuilder().setStringValue(command.getHash()).build();
        Value emojiValue = Value.newBuilder().setStringValue("").build();

        Block.Set.Details.Detail imageDetail = Block.Set.Details.Detail
                .newBuilder()
                .setKey(iconImageKey)
                .setValue(imageValue)
                .build();

        Block.Set.Details.Detail emojiDetail = Block.Set.Details.Detail
                .newBuilder()
                .setKey(iconEmojiKey)
                .setValue(emojiValue)
                .build();

        Block.Set.Details.Request request = Block.Set.Details.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addDetails(imageDetail)
                .addDetails(emojiDetail)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Set.Details.Response response = service.blockSetDetails(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public PayloadEntity setupBookmark(CommandEntity.SetupBookmark command) throws Exception {
        Block.Bookmark.Fetch.Request request = Block.Bookmark.Fetch.Request
                .newBuilder()
                .setBlockId(command.getTarget())
                .setContextId(command.getContext())
                .setUrl(command.getUrl())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Bookmark.Fetch.Response response = service.blockBookmarkFetch(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity undo(CommandEntity.Undo command) throws Exception {
        Block.Undo.Request request = Block.Undo.Request
                .newBuilder()
                .setContextId(command.getContext())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Undo.Response response = service.blockUndo(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public PayloadEntity redo(CommandEntity.Redo command) throws Exception {
        Block.Redo.Request request = Block.Redo.Request
                .newBuilder()
                .setContextId(command.getContext())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Redo.Response response = service.blockRedo(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }

    public void archiveDocument(CommandEntity.ArchiveDocument command) throws Exception {
        BlockList.Set.Page.IsArchived.Request request = BlockList.Set.Page.IsArchived.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTarget())
                .setIsArchived(command.isArchived())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.Set.Page.IsArchived.Response response = service.blockListSetPageIsArchived(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }
    }

    public List<String> turnIntoDocument(CommandEntity.TurnIntoDocument command) throws Exception {

        BlockList.ConvertChildrenToPages.Request request = BlockList.ConvertChildrenToPages.Request
                .newBuilder()
                .setContextId(command.getContext())
                .addAllBlockIds(command.getTargets())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        BlockList.ConvertChildrenToPages.Response response = service.convertChildrenToPages(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return response.getLinkIdsList();
    }

    public Response.Clipboard.Paste paste(CommandEntity.Paste command) throws Exception {

        Range range = Range
                .newBuilder()
                .setFrom(command.getRange().getFirst())
                .setTo(command.getRange().getLast())
                .build();

        String html = "";

        if (command.getHtml() != null) {
            html = command.getHtml();
        }

        List<Models.Block> blocks = mapper.toMiddleware(command.getBlocks());

        Block.Paste.Request request = Block.Paste.Request
                .newBuilder()
                .setContextId(command.getContext())
                .setFocusedBlockId(command.getFocus())
                .setTextSlot(command.getText())
                .setHtmlSlot(html)
                .setSelectedTextRange(range)
                .addAllAnySlot(blocks)
                .addAllSelectedBlockIds(command.getSelected())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Paste.Response response = service.blockPaste(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Response.Clipboard.Paste(
                response.getCaretPosition(),
                response.getIsSameBlockCaret(),
                response.getBlockIdsList(),
                mapper.toPayload(response.getEvent())
        );
    }

    public Response.Clipboard.Copy copy(CommandEntity.Copy command) throws Exception {

        Range range;

        if (command.getRange() != null) {
            range = Range.newBuilder()
                    .setFrom(command.getRange().getFirst())
                    .setTo(command.getRange().getLast())
                    .build();
        } else {
            range = Range.getDefaultInstance();
        }

        List<Models.Block> blocks = mapper.toMiddleware(command.getBlocks());

        Block.Copy.Request.Builder builder = Block.Copy.Request.newBuilder();

        if (range != null) {
            builder.setSelectedTextRange(range);
        }

        Block.Copy.Request request = builder
                .setContextId(command.getContext())
                .addAllBlocks(blocks)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Copy.Response response = service.blockCopy(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Response.Clipboard.Copy(
                response.getTextSlot(),
                response.getHtmlSlot(),
                mapper.toEntity(response.getAnySlotList())
        );
    }

    public Response.Media.Upload uploadFile(CommandEntity.UploadFile command) throws Exception {

        Models.Block.Content.File.Type type = null;

        switch (command.getType()) {
            case FILE:
                type = Models.Block.Content.File.Type.File;
                break;
            case IMAGE:
                type = Models.Block.Content.File.Type.Image;
                break;
            case VIDEO:
                type = Models.Block.Content.File.Type.Video;
                break;
            case NONE:
                break;
        }

        UploadFile.Request request = UploadFile.Request.newBuilder()
                .setLocalPath(command.getPath())
                .setType(type)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        UploadFile.Response response = service.uploadFile(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return new Response.Media.Upload(response.getHash());
    }

    public Localstore.PageInfoWithLinks getPageInfoWithLinks(String pageId) throws Exception {
        Navigation.GetPageInfoWithLinks.Request request = Navigation.GetPageInfoWithLinks.Request
                .newBuilder()
                .setPageId(pageId)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Navigation.GetPageInfoWithLinks.Response response = service.pageInfoWithLinks(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return response.getPage();
    }

    public List<Localstore.PageInfo> getListPages() throws Exception {
        Navigation.ListPages.Request request = Navigation.ListPages.Request
                .newBuilder()
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Navigation.ListPages.Response response = service.listPages(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return response.getPagesList();
    }

    public PayloadEntity linkToObject(
            String contextId,
            String targetId, String blockId,
            boolean replace,
            PositionEntity positionEntity
    ) throws Exception {

        Models.Block.Position position = null;

        if (replace) {
            position = Models.Block.Position.Replace;
        } else {
            position = mapper.toMiddleware(positionEntity);
        }

        Models.Block.Content.Link link = Models.Block.Content.Link
                .newBuilder()
                .setTargetBlockId(blockId)
                .build();

        Models.Block model = Models.Block.newBuilder()
                .setLink(link)
                .build();

        Block.Create.Request request = Block.Create.Request
                .newBuilder()
                .setContextId(contextId)
                .setTargetId(targetId)
                .setPosition(position)
                .setBlock(model)
                .build();

        if (BuildConfig.DEBUG) {
            Timber.d(request.getClass().getName() + "\n" + request.toString());
        }

        Block.Create.Response response = service.blockCreate(request);

        if (BuildConfig.DEBUG) {
            Timber.d(response.getClass().getName() + "\n" + response.toString());
        }

        return mapper.toPayload(response.getEvent());
    }
}

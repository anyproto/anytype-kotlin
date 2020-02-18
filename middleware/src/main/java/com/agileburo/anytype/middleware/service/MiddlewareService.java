package com.agileburo.anytype.middleware.service;

import anytype.Commands.Rpc.Account;
import anytype.Commands.Rpc.Block;
import anytype.Commands.Rpc.BlockList;
import anytype.Commands.Rpc.Config;
import anytype.Commands.Rpc.Ipfs.Image;
import anytype.Commands.Rpc.Wallet;

/**
 * Service for interacting with the backend.
 */
public interface MiddlewareService {
    Config.Get.Response configGet(Config.Get.Request request) throws Exception;

    Wallet.Create.Response walletCreate(Wallet.Create.Request request) throws Exception;

    Wallet.Recover.Response walletRecover(Wallet.Recover.Request request) throws Exception;

    Account.Create.Response accountCreate(Account.Create.Request request) throws Exception;

    Account.Select.Response accountSelect(Account.Select.Request request) throws Exception;

    Account.Recover.Response accountRecover(Account.Recover.Request request) throws Exception;

    Account.Stop.Response accountStop(Account.Stop.Request request) throws Exception;

    Image.Get.Blob.Response imageGet(Image.Get.Blob.Request request) throws Exception;

    Block.Open.Response blockOpen(Block.Open.Request request) throws Exception;

    Block.Close.Response blockClose(Block.Close.Request request) throws Exception;

    Block.Create.Response blockCreate(Block.Create.Request request) throws Exception;

    Block.CreatePage.Response blockCreatePage(Block.CreatePage.Request request) throws Exception;

    Block.Set.Text.TText.Response blockSetTextText(Block.Set.Text.TText.Request request) throws Exception;

    Block.Set.Text.Checked.Response blockSetTextChecked(Block.Set.Text.Checked.Request request) throws Exception;

    Block.Set.Text.Color.Response blockSetTextColor(Block.Set.Text.Color.Request request) throws Exception;

    Block.Set.Text.Style.Response blockSetTextStyle(Block.Set.Text.Style.Request request) throws Exception;

    BlockList.Move.Response blockListMove(BlockList.Move.Request request) throws Exception;

    Block.Unlink.Response blockUnlink(Block.Unlink.Request request) throws Exception;

    Block.Merge.Response blockMerge(Block.Merge.Request request) throws Exception;

    Block.Split.Response blockSplit(Block.Split.Request request) throws Exception;

    BlockList.Duplicate.Response blockListDuplicate(BlockList.Duplicate.Request request) throws Exception;
}

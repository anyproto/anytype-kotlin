package com.anytypeio.anytype.middleware.mappers

import anytype.Event.P2PStatus
import anytype.Event.Space
import com.anytypeio.anytype.core_models.chats.Chat


typealias MEvent = anytype.Event
typealias MEventMessage = anytype.Event.Message
typealias MAccount = anytype.model.Account
typealias MAccountStatus = anytype.model.Account.Status
typealias MAccountStatusType = anytype.model.Account.StatusType
typealias MBlock = anytype.model.Block
typealias MObjectView = anytype.model.ObjectView
typealias MBText = anytype.model.Block.Content.Text
typealias MBTextStyle = anytype.model.Block.Content.Text.Style
typealias MBMarks = anytype.model.Block.Content.Text.Marks
typealias MBMark = anytype.model.Block.Content.Text.Mark
typealias MBMarkType = anytype.model.Block.Content.Text.Mark.Type
typealias MBFile = anytype.model.Block.Content.File
typealias MBFileState = anytype.model.Block.Content.File.State
typealias MBFileType = anytype.model.Block.Content.File.Type
typealias MBLink = anytype.model.Block.Content.Link
typealias MBLatex = anytype.model.Block.Content.Latex
typealias MBLatexProcessor = anytype.model.Block.Content.Latex.Processor
typealias MBLinkIconSize = anytype.model.Block.Content.Link.IconSize
typealias MBLinkCardStyle = anytype.model.Block.Content.Link.CardStyle
typealias MBLinkDescription = anytype.model.Block.Content.Link.Description
typealias MBLinkStyle = anytype.model.Block.Content.Link.Style
typealias MBBookmark = anytype.model.Block.Content.Bookmark
typealias MBookmarkState = anytype.model.Block.Content.Bookmark.State
typealias MBLayout = anytype.model.Block.Content.Layout
typealias MBLayoutStyle = anytype.model.Block.Content.Layout.Style
typealias MBDiv = anytype.model.Block.Content.Div
typealias MBDivStyle = anytype.model.Block.Content.Div.Style
typealias MBRelation = anytype.model.Block.Content.Relation
typealias MBAlign = anytype.model.Block.Align
typealias MBPosition = anytype.model.Block.Position
typealias MBSplitMode = anytype.Rpc.Block.Split.Request.Mode
typealias MBTableOfContents = anytype.model.Block.Content.TableOfContents

typealias MChatMessage = anytype.model.ChatMessage
typealias MChatState = anytype.model.ChatState
typealias MChatMessageContent = anytype.model.ChatMessage.MessageContent
typealias MChatMessageAttachment = anytype.model.ChatMessage.Attachment
typealias MChatMessageAttachmentType = anytype.model.ChatMessage.Attachment.AttachmentType
typealias MChatMessageReactions = anytype.model.ChatMessage.Reactions
typealias MChatMessageReactionIdentity = anytype.model.ChatMessage.Reactions.IdentityList

typealias MDV = anytype.model.Block.Content.Dataview
typealias MDVView = anytype.model.Block.Content.Dataview.View
typealias MDVViewType = anytype.model.Block.Content.Dataview.View.Type
typealias MDVViewCardSize = anytype.model.Block.Content.Dataview.View.Size
typealias MDVSort = anytype.model.Block.Content.Dataview.Sort
typealias MDVSortType = anytype.model.Block.Content.Dataview.Sort.Type
typealias MDVSortEmptyType = anytype.model.Block.Content.Dataview.Sort.EmptyType
typealias MDVFilter = anytype.model.Block.Content.Dataview.Filter
typealias MDVFilterCondition = anytype.model.Block.Content.Dataview.Filter.Condition
typealias MDVFilterQuickOption = anytype.model.Block.Content.Dataview.Filter.QuickOption
typealias MDVFilterOperator = anytype.model.Block.Content.Dataview.Filter.Operator
typealias MDVRelation = anytype.model.Block.Content.Dataview.Relation
typealias MDVDateFormat = anytype.model.Block.Content.Dataview.Relation.DateFormat
typealias MDVTimeFormat = anytype.model.Block.Content.Dataview.Relation.TimeFormat
typealias MDVFilterUpdate = anytype.Event.Block.Dataview.ViewUpdate.Filter
typealias MDVSortUpdate = anytype.Event.Block.Dataview.ViewUpdate.Sort
typealias MDVRelationUpdate = anytype.Event.Block.Dataview.ViewUpdate.Relation
typealias MDVViewFields = anytype.Event.Block.Dataview.ViewUpdate.Fields
typealias MDVObjectOrder = anytype.model.Block.Content.Dataview.ObjectOrder

typealias MObjectType = anytype.model.ObjectType
typealias MSmartBlockType = anytype.model.SmartBlockType
typealias MOTypeLayout = anytype.model.ObjectType.Layout
typealias MRelationFormat = anytype.model.RelationFormat
typealias MRelationDataSource = anytype.model.Relation.DataSource
typealias MRelation = anytype.model.Relation
typealias MRelationLink = anytype.model.RelationLink
typealias MRelationOption = anytype.model.Relation.Option
typealias MObjectRestriction = anytype.model.Restrictions.ObjectRestriction
typealias MDVRestrictions = anytype.model.Restrictions.DataviewRestrictions
typealias MDVRestriction = anytype.model.Restrictions.DataviewRestriction

typealias MWidget = anytype.model.Block.Content.Widget
typealias MWidgetLayout = anytype.model.Block.Content.Widget.Layout
typealias MNetworkMode = anytype.Rpc.Account.NetworkMode

typealias MParticipantPermission = anytype.model.ParticipantPermissions

typealias MManifestInfo = anytype.model.ManifestInfo

typealias MProcess = anytype.Model.Process
typealias MProcessState = anytype.Model.Process.State
typealias MProcessProgress = anytype.Model.Process.Progress

typealias MNotification = anytype.model.Notification
typealias MNotificationActionType = anytype.model.Notification.ActionType
typealias MNotificationStatus = anytype.model.Notification.Status
typealias MImportErrorCode = anytype.model.Import.ErrorCode

typealias MMembership = anytype.model.Membership
typealias MMembershipStatus = anytype.model.Membership.Status
typealias MMembershipPaymentMethod = anytype.model.Membership.PaymentMethod
typealias MMembershipTierData = anytype.model.MembershipTierData
typealias MMembershipTierDataPeriodType = anytype.model.MembershipTierData.PeriodType
typealias MNameServiceNameType = anytype.model.NameserviceNameType
typealias MEmailVerificationStatus = anytype.model.Membership.EmailVerificationStatus
typealias MDetail = anytype.model.Detail

typealias MSpaceSyncStatus = anytype.Event.Space.Status
typealias MSpaceSyncNetwork = anytype.Event.Space.Network
typealias MSpaceSyncError = anytype.Event.Space.SyncError

typealias MP2PStatus = anytype.Event.P2PStatus.Status
typealias MP2PStatusUpdate = P2PStatus.Update
typealias MSyncStatusUpdate = Space.SyncStatus.Update

typealias MDeviceNetworkType = anytype.model.DeviceNetworkType

typealias MLinkPreview = anytype.model.LinkPreview

typealias MInviteType = anytype.model.InviteType
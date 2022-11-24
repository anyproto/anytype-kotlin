package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubAccountSetup(
    account : Account = StubAccount(),
    config: Config = StubConfig(),
    features: FeaturesConfig = StubFeatureConfig(),
    status: AccountStatus = AccountStatus.Active
) : AccountSetup = AccountSetup(
    account = account,
    features = features,
    status = status,
    config = config
)

fun StubAccount(
    id : Id = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    avatar: Url? = null,
    color: String? = null
) : Account = Account(
    id = id,
    name = name,
    avatar = avatar,
    color = color
)

fun StubConfig(
    home: Id = MockDataFactory.randomUuid(),
    profile: Id = MockDataFactory.randomUuid(),
    gateway: Url = MockDataFactory.randomUuid(),
    workspace: Id = MockDataFactory.randomUuid()
) : Config = Config(
    home = home,
    profile = profile,
    gateway = gateway,
    workspace = workspace
)

fun StubFeatureConfig(
    enableDataView: Boolean? = MockDataFactory.randomBoolean(),
    enableDebug: Boolean? = MockDataFactory.randomBoolean(),
    enableChannelSwitch: Boolean? = MockDataFactory.randomBoolean(),
    enableSpaces: Boolean? = MockDataFactory.randomBoolean()
) : FeaturesConfig = FeaturesConfig(
    enableDataView = enableDataView,
    enableDebug = enableDebug,
    enablePrereleaseChannel = enableChannelSwitch,
    enableSpaces = enableSpaces
)
# Contributing to Anytype

First off, thank you for your desire to develop Anytype together!

All types of contributions are encouraged and valued. See the [Table of Contents](#table-of-contents) for different ways to help and details about how this project handles them. Please make sure to read the relevant section before making your contribution. It will make it a lot easier for us maintainers and smooth out the experience for all involved. The community looks forward to your contributions. 🎉

> And if you like the project, but just don't have time to contribute, that's fine. There are other easy ways to support the project and show your appreciation, which we would also be very happy about:
> - Follow us on Github
> - Star our repos
> - Tweet about [@AnytypeLabs](https://twitter.com/AnytypeLabs)
> - Mention Anytype at local meetups and tell your friends/colleagues

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Preferred Languages](#preferred-languages)
- [I Have a Question](#i-have-a-question)
- [I Want To Contribute](#i-want-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Requesting Features](#requesting-features)
  - [Reporting Security Issues](#reporting-security-issues)
  - [Contributing Code](#contributing-code)
  - [Translating Anytype](#translating-anytype)
- [Contributors Recognition](#contributors-recognition)

## Code of Conduct

This project and everyone participating in it is governed by the
[Code of Conduct](CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [support@anytype.io](mailto:support@anytype.io).

## Preferred Languages

We prefer all communications to be in English.

## I Have a Question

> Here on GitHub, we're discussing everything related to building Anytype. If you have any questions about how to use Anytype, please join our [community](https://community.anytype.io) or contact us via [support@anytype.io](mailto:support@anytype.io).
>
> If you want to ask a technical question, we assume that you have read the project's [README](../README.md) and our [tech documentation](https://tech.anytype.io).

Before you ask a question, it is best to look into our [Contributors Community](https://github.com/orgs/anyproto/discussions). In case you have found a suitable topic and still need clarification, you can write a comment there.

Feel free to create a new topic in [Contributors Community](https://github.com/orgs/anyproto/discussions) if the question is still unsolved :)

We recommend the following:

- Provide as much context as you can about what you're running into.
- Provide project and platform versions, depending on what seems relevant.

## I Want To Contribute

> ### Legal Notice 
> When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content and that the content you contribute may be provided under the project license. For PRs our bot will ask you to accept the [Contributor License Agreement](https://github.com/anyproto/open/blob/main/templates/CLA.md).

### Reporting Bugs

#### Before Submitting a Bug Report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we ask you to investigate carefully, collect information and describe the issue in detail in your report. Please complete the following steps in advance to help us fix any potential bug as fast as possible.

- Make sure that you are using the latest version.
- Determine if your bug is really a bug and not an error on your side e.g. using incompatible environment components/versions (If you are looking for support, you might want to check [this section](#i-have-a-question)).
- To see if other users have experienced (and potentially already solved) the same issue you are having, check if there is not already a bug report existing for your bug or error in the [bug tracker](issues?q=label%3Abug) or in [existing bug reports](https://community.anytype.io/c/bug-reports/l/latest?board=default) created by users.

#### How Do I Submit a Good Bug Report?

> You must never report security related issues, vulnerabilities or bugs including sensitive information to the issue tracker, or elsewhere in public. Instead sensitive bugs must be sent by email to [security@anytype.io](mailto:security@anytype.io) (please see [SECURITY.md](SECURITY.md) for details).

If you run into an issue with the project:

- Open an [Issue](/issues/new/choose)
- Click `Get started` next to `Bug report` section
- Explain the behavior you would expect and the actual behavior.
- Please provide as much context as possible and describe the *reproduction steps* that someone else can follow to recreate the issue on their own.
- Provide the information you collected in the previous section.

Once it's filed:

- A team member will try to reproduce the issue with your provided steps. If there are no reproduction steps or no obvious way to reproduce the issue, the team will ask you for those steps.
- If the team is able to reproduce the issue and the issue will be left to be addressed by the team or contributors.

### Requesting Features

#### Before Requesting a Feature

- Make sure that you are using the latest version.
- If you are looking for support, you might want to check [this section](#i-have-a-question).
- To see if other users already requested something similar you are suggesting, check the [tracker](issues?q=label%3Aenhancement) or [feature requests](https://community.anytype.io/c/feature-requests/l/latest?board=default) created by users.

#### How Do I Submit a Good Enhancement Suggestion?

Enhancement suggestions are tracked as [GitHub issues](/issues).

- Open an [Issue](/issues/new/choose)
- Click `Get started` next to `Feature request` section
- Follow the template and feel free to add more details

### Reporting Security Issues

You must never report security related issues, vulnerabilities or bugs including sensitive information to the issue tracker, or elsewhere in public. Instead sensitive bugs must be sent by email to [security@anytype.io](mailto:security@anytype.io) (please see [SECURITY.md](SECURITY.md) for details).

### Contributing Code
When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content and that the content you contribute may be provided under the project license. Our bot will ask you to accept the [Contributor License Agreement](https://github.com/anyproto/open/blob/main/templates/CLA.md) before we can accept the pull request.

Basic rules for pull requests:
- Pull requests generally need to be based on and opened against the `main` branch, unless by explicit agreement because the work is contributing to some more complex feature branch
- Please follow a suggested template for the pull request description
- When neccesary, changes are documented in [`README.md`](../README.md) or suggestions are also made to [`tech-docs`](https://github.com/anyproto/tech-docs)

All pull requests will be reviewed by the team.

### Translating Anytype

Help us make Anytype speak your language! Translations are handled by the community through **Crowdin**, so in most cases you don't need to touch the code or open a pull request.

#### How to translate

- The Android app is translated in the [Anytype Mobile project on Crowdin](https://crowdin.com/project/anytype-mobile).
- Sign in to Crowdin, pick your language, and translate or fix strings directly in the online editor.
- Approved translations are synced back into this repository **automatically** by a bot pull request (its title and target files are configured in [`crowdin.yml`](../crowdin.yml) — at the time of writing, `l10n | Enhancement`). You don't need to create a branch or open a PR yourself, and translations submitted via Crowdin don't require signing the CLA.
- Existing languages are shipped automatically with the next build. To request a **new** language that isn't listed yet, reach out via the [Contributors Community](https://github.com/orgs/anyproto/discussions) so the team can enable it.

#### Translation guidelines

To keep the app working correctly, please:

- Keep placeholders such as `%1$s` and `%2$d` unchanged (you may reposition them to fit your language's grammar, but don't remove or renumber them).
- Fill in **all** plural forms your language requires. Android supports `zero`, `one`, `two`, `few`, `many`, and `other` — for example, Ukrainian and Russian need `one`, `few`, `many`, and `other`.
- Preserve any markup, HTML tags, or CDATA that appears in the source string.

#### Fixing the English source text

The English source strings live in [`localization/src/main/res/values/strings.xml`](../localization/src/main/res/values/strings.xml). If you spot a mistake in the **English source itself** (a typo or unclear wording), don't fix it in Crowdin — open an issue or a pull request against that file instead. This counts as a code contribution, so the [CLA](https://github.com/anyproto/open/blob/main/templates/CLA.md) applies.

## Contributors Recognition

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind are welcome! 

You can check the list of contributors in a [dedicated repo](https://github.com/anyproto/contributors).

---

*This guide is based on the [`contributing.md`](https://contributing.md/example/).*

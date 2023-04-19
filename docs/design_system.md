Typography and buttons are separated into individual styles. The project utilizes styles from both XML and Jetpack Compose.

Typography link: https://www.figma.com/file/vgXV7x2v20vJajc7clYJ7a/Mobile-Design-System?node-id=1587%3A54&t=0D7rA21FfRyKOF1l-1

Buttons link: https://www.figma.com/file/vgXV7x2v20vJajc7clYJ7a/Mobile-Design-System?node-id=1588%3A59&t=0D7rA21FfRyKOF1l-1

Key points for non-Compose styles:

Text styles are located in the design_system file (android-anytype/core-ui/src/main/res/values/design_system.xml).
Example: TextView.ContentStyle.Headline.Title

Button styles are also located in the design_system file.
Example: Button.Primary.Medium

Custom buttons are defined in the DesignSystemButtons class within the core-ui module.
Example: ButtonPrimaryXSmall

Key points for Compose styles:

Typography is found in the TypographyCompose.kt class within the core-ui module.
Example: BodyCalloutMedium

Composable button templates are also located in the DesignSystemButtons class within the core-ui module.
Example: ButtonPrimary

All previews can be viewed through the Sample app, DesignSystemActivity.
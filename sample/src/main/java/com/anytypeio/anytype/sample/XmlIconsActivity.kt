package com.anytypeio.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_models.ui.MimeCategory
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon.TypeIcon.Default.Companion.DEFAULT_CUSTOM_ICON
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.sample.databinding.ActivityXmlIconsBinding

class XmlIconsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXmlIconsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXmlIconsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupIcons()
    }

    private fun setupIcons() {
        // Basic.Emoji
        val emojiIcon = ObjectIcon.Basic.Emoji("😀", ObjectIcon.TypeIcon.Fallback.DEFAULT)
        binding.iconEmoji20.setIcon(emojiIcon)
        binding.iconEmoji30.setIcon(emojiIcon)
        binding.iconEmoji32.setIcon(emojiIcon)
        binding.iconEmoji40.setIcon(emojiIcon)

        // Basic.Image
        val basicImageIcon = ObjectIcon.Basic.Image(
            "https://samplelib.com/lib/preview/png/sample-red-400x300.png",
            ObjectIcon.TypeIcon.Fallback.DEFAULT
        )
        binding.iconBasicImage20.setIcon(basicImageIcon)
        binding.iconBasicImage30.setIcon(basicImageIcon)
        binding.iconBasicImage32.setIcon(basicImageIcon)
        binding.iconBasicImage40.setIcon(basicImageIcon)

        // Profile.Avatar
        val avatarIcon = ObjectIcon.Profile.Avatar("John Doe")
        binding.iconAvatar20.setIcon(avatarIcon)
        binding.iconAvatar30.setIcon(avatarIcon)
        binding.iconAvatar32.setIcon(avatarIcon)
        binding.iconAvatar40.setIcon(avatarIcon)

        // Profile.Image
        val profileImageIcon = ObjectIcon.Profile.Image(
            "https://samplelib.com/lib/preview/png/sample-red-400x300.png",
            "John Doe"
        )
        binding.iconProfileImage20.setIcon(profileImageIcon)
        binding.iconProfileImage30.setIcon(profileImageIcon)
        binding.iconProfileImage32.setIcon(profileImageIcon)
        binding.iconProfileImage40.setIcon(profileImageIcon)

        // Task
        val taskCheckedIcon = ObjectIcon.Task(true)
        val taskUncheckedIcon = ObjectIcon.Task(false)
        binding.iconTaskChecked20.setIcon(taskCheckedIcon)
        binding.iconTaskChecked30.setIcon(taskCheckedIcon)
        binding.iconTaskChecked32.setIcon(taskCheckedIcon)
        binding.iconTaskChecked40.setIcon(taskCheckedIcon)
        binding.iconTaskUnchecked20.setIcon(taskUncheckedIcon)
        binding.iconTaskUnchecked30.setIcon(taskUncheckedIcon)
        binding.iconTaskUnchecked32.setIcon(taskUncheckedIcon)
        binding.iconTaskUnchecked40.setIcon(taskUncheckedIcon)
        
        // Checkbox
        val checkboxCheckedIcon = ObjectIcon.Checkbox(true)
        val checkboxUncheckedIcon = ObjectIcon.Checkbox(false)
        binding.iconCheckboxChecked20.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxChecked30.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxChecked32.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxChecked40.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxUnchecked20.setIcon(checkboxUncheckedIcon)
        binding.iconCheckboxUnchecked30.setIcon(checkboxUncheckedIcon)
        binding.iconCheckboxUnchecked32.setIcon(checkboxUncheckedIcon)
        binding.iconCheckboxUnchecked40.setIcon(checkboxUncheckedIcon)

        // Bookmark
        val bookmarkIcon = ObjectIcon.Bookmark(
            "https://samplelib.com/lib/preview/png/sample-red-400x300.png",
            ObjectIcon.TypeIcon.Fallback.DEFAULT
        )
        binding.iconBookmark20.setIcon(bookmarkIcon)
        binding.iconBookmark30.setIcon(bookmarkIcon)
        binding.iconBookmark32.setIcon(bookmarkIcon)
        binding.iconBookmark40.setIcon(bookmarkIcon)

        // File
        val fileIcon = ObjectIcon.File("image/png", "png")
        binding.iconFile20.setIcon(fileIcon)
        binding.iconFile30.setIcon(fileIcon)
        binding.iconFile32.setIcon(fileIcon)
        binding.iconFile40.setIcon(fileIcon)

        // FileDefault
        val fileDefaultIcon = ObjectIcon.FileDefault(MimeCategory.IMAGE)
        binding.iconFileDefault20.setIcon(fileDefaultIcon)
        binding.iconFileDefault30.setIcon(fileDefaultIcon)
        binding.iconFileDefault32.setIcon(fileDefaultIcon)
        binding.iconFileDefault40.setIcon(fileDefaultIcon)

        // Deleted
        val deletedIcon = ObjectIcon.Deleted
        binding.iconDeleted20.setIcon(deletedIcon)
        binding.iconDeleted30.setIcon(deletedIcon)
        binding.iconDeleted32.setIcon(deletedIcon)
        binding.iconDeleted40.setIcon(deletedIcon)

        // TypeIcon.Default
        val typeDefaultIcon = ObjectIcon.TypeIcon.Default.DEFAULT
        binding.iconTypeDefault20.setIcon(typeDefaultIcon)
        binding.iconTypeDefault30.setIcon(typeDefaultIcon)
        binding.iconTypeDefault32.setIcon(typeDefaultIcon)
        binding.iconTypeDefault40.setIcon(typeDefaultIcon)

        // TypeIcon.Fallback
        val typeFallbackIcon = ObjectIcon.TypeIcon.Fallback("hammer")
        binding.iconTypeFallback20.setIcon(typeFallbackIcon)
        binding.iconTypeFallback30.setIcon(typeFallbackIcon)
        binding.iconTypeFallback32.setIcon(typeFallbackIcon)
        binding.iconTypeFallback40.setIcon(typeFallbackIcon)

        // TypeIcon.Emoji
        val typeEmojiIcon = ObjectIcon.TypeIcon.Emoji("😀", DEFAULT_CUSTOM_ICON, CustomIconColor.DEFAULT)
        binding.iconTypeEmoji20.setIcon(typeEmojiIcon)
        binding.iconTypeEmoji30.setIcon(typeEmojiIcon)
        binding.iconTypeEmoji32.setIcon(typeEmojiIcon)
        binding.iconTypeEmoji40.setIcon(typeEmojiIcon)

        // TypeIcon.Deleted
        val typeDeletedIcon = ObjectIcon.TypeIcon.Deleted
        binding.iconTypeDeleted20.setIcon(typeDeletedIcon)
        binding.iconTypeDeleted30.setIcon(typeDeletedIcon)
        binding.iconTypeDeleted32.setIcon(typeDeletedIcon)
        binding.iconTypeDeleted40.setIcon(typeDeletedIcon)

        // None
        val noneIcon = ObjectIcon.None
        binding.iconNone20.setIcon(noneIcon)
        binding.iconNone30.setIcon(noneIcon)
        binding.iconNone32.setIcon(noneIcon)
        binding.iconNone40.setIcon(noneIcon)
    }
} 
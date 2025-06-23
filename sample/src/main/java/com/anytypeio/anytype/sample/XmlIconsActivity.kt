package com.anytypeio.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
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
        binding.iconEmoji32.setIcon(emojiIcon)

        // Basic.Image
        val basicImageIcon = ObjectIcon.Basic.Image(
            "android.resource://${packageName}/${R.drawable.ic_plus_18}",
            ObjectIcon.TypeIcon.Fallback.DEFAULT
        )
        binding.iconBasicImage20.setIcon(basicImageIcon)
        binding.iconBasicImage32.setIcon(basicImageIcon)

        // Profile.Avatar
        val avatarIcon = ObjectIcon.Profile.Avatar("John Doe")
        binding.iconAvatar20.setIcon(avatarIcon)
        binding.iconAvatar32.setIcon(avatarIcon)

        // Profile.Image
        val profileImageIcon = ObjectIcon.Profile.Image(
            "android.resource://${packageName}/${R.drawable.ic_plus_18}",
            "John Doe"
        )
        binding.iconProfileImage20.setIcon(profileImageIcon)
        binding.iconProfileImage32.setIcon(profileImageIcon)

        // Task
        val taskCheckedIcon = ObjectIcon.Task(true)
        val taskUncheckedIcon = ObjectIcon.Task(false)
        binding.iconTaskChecked20.setIcon(taskCheckedIcon)
        binding.iconTaskChecked32.setIcon(taskCheckedIcon)
        binding.iconTaskUnchecked20.setIcon(taskUncheckedIcon)
        binding.iconTaskUnchecked32.setIcon(taskUncheckedIcon)
        
        // Checkbox
        val checkboxCheckedIcon = ObjectIcon.Checkbox(true)
        val checkboxUncheckedIcon = ObjectIcon.Checkbox(false)
        binding.iconCheckboxChecked20.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxChecked32.setIcon(checkboxCheckedIcon)
        binding.iconCheckboxUnchecked20.setIcon(checkboxUncheckedIcon)
        binding.iconCheckboxUnchecked32.setIcon(checkboxUncheckedIcon)

        // Bookmark
        val bookmarkIcon = ObjectIcon.Bookmark(
            "android.resource://${packageName}/${R.drawable.ic_plus_18}",
            ObjectIcon.TypeIcon.Fallback.DEFAULT
        )
        binding.iconBookmark20.setIcon(bookmarkIcon)
        binding.iconBookmark32.setIcon(bookmarkIcon)

        // File
        val fileIcon = ObjectIcon.File("image/png", "png")
        binding.iconFile20.setIcon(fileIcon)
        binding.iconFile32.setIcon(fileIcon)

        // FileDefault
        val fileDefaultIcon = ObjectIcon.FileDefault(MimeTypes.Category.IMAGE)
        binding.iconFileDefault20.setIcon(fileDefaultIcon)
        binding.iconFileDefault32.setIcon(fileDefaultIcon)

        // Deleted
        val deletedIcon = ObjectIcon.Deleted
        binding.iconDeleted20.setIcon(deletedIcon)
        binding.iconDeleted32.setIcon(deletedIcon)

        // TypeIcon.Default
        val typeDefaultIcon = ObjectIcon.TypeIcon.Default("human", CustomIconColor.Green)
        binding.iconTypeDefault20.setIcon(typeDefaultIcon)
        binding.iconTypeDefault32.setIcon(typeDefaultIcon)

        // TypeIcon.Fallback
        val typeFallbackIcon = ObjectIcon.TypeIcon.Fallback("page")
        binding.iconTypeFallback20.setIcon(typeFallbackIcon)
        binding.iconTypeFallback32.setIcon(typeFallbackIcon)

        // TypeIcon.Emoji
        val typeEmojiIcon = ObjectIcon.TypeIcon.Emoji("😎", "human", CustomIconColor.DEFAULT)
        binding.iconTypeEmoji20.setIcon(typeEmojiIcon)
        binding.iconTypeEmoji32.setIcon(typeEmojiIcon)

        // TypeIcon.Deleted
        val typeDeletedIcon = ObjectIcon.TypeIcon.Deleted
        binding.iconTypeDeleted20.setIcon(typeDeletedIcon)
        binding.iconTypeDeleted32.setIcon(typeDeletedIcon)

        // None
        val noneIcon = ObjectIcon.None
        binding.iconNone20.setIcon(noneIcon)
        binding.iconNone32.setIcon(noneIcon)
    }
} 
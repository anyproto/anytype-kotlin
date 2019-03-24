package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType

class EditBlockToolbar : ConstraintLayout {

    private lateinit var btnText: ImageView
    private lateinit var btnHeader1: ImageView
    private lateinit var btnHeader2: ImageView
    private lateinit var btnHeader3: ImageView
    private lateinit var btnHightLight: ImageView
    private lateinit var btnBullet: ImageView

    private lateinit var buttons: List<View>

    private var block = Block("","",ContentType.P,"")

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    fun setBlock(block: Block) {
        this.block = block
        buttons.forEach { it.isSelected = false }
        when (block.contentType) {
            is ContentType.P -> btnText.isSelected = true
            is ContentType.H1 -> btnHeader1.isSelected = true
            is ContentType.H2 -> btnHeader2.isSelected = true
            is ContentType.H3 -> btnHeader3.isSelected = true
        }
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_edit_block_toolbar, this)

        btnText = findViewById(R.id.btnText)
        btnHeader1 = findViewById(R.id.btnHeader1)
        btnHeader2 = findViewById(R.id.btnHeader2)
        btnHeader3 = findViewById(R.id.btnHeader3)
        btnHightLight = findViewById(R.id.btnHighlighted)
        btnBullet = findViewById(R.id.btnBulleted)
        buttons = listOf(btnText, btnHeader1, btnHeader2, btnHeader3, btnHightLight, btnBullet)
    }

    fun setMainActions(
        textClick: (Block) -> Unit,
        header1Click: (Block) -> Unit,
        header2Click: (Block) -> Unit,
        header3Click: (Block) -> Unit,
        hightLitedClick: (Block) -> Unit,
        bulledClick: (Block) -> Unit
    ) {
        btnText.setOnClickListener {
            it.isSelected = !it.isSelected
            textClick(block)
        }
        btnHeader1.setOnClickListener {
            it.isSelected = !it.isSelected
            header1Click(block)
        }
        btnHeader2.setOnClickListener {
            it.isSelected = !it.isSelected
            header2Click(block)
        }
        btnHeader3.setOnClickListener {
            it.isSelected = !it.isSelected
            header3Click(block)
        }
        btnHightLight.setOnClickListener {
            it.isSelected = !it.isSelected
            hightLitedClick(block)
        }
        btnBullet.setOnClickListener {
            it.isSelected = !it.isSelected
            bulledClick(block)
        }

    }
}
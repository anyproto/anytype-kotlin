package com.agileburo.anytype.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SpanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_span)

//        Glide.with(this)
//            .asBitmap()
//            //.load("https://1.bp.blogspot.com/-lMDrhI7Qt7w/Wy36dkRulwI/AAAAAAAACF8/V-OyGUZvz24OFcs43b-61XsbHa2QVcXLwCPcBGAYYCw/s320/Android_Robot_social_media_corporate_logo_icon-icons.com_67679.png")
//            .load("http://127.0.0.1:47800/image/bafybeibbbrglxqsoqcf2hpbznofmdupjappw4gzkfgp63s2pgob3i72q3y")
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    //Timber.d("onResourceReady : ${ (System.nanoTime()/1000)}, size : ${resource.byteCount}")
//                    val spannableString4 = SpannableString("Строка смаркапом и болдом")
//                    spannableString4.setSpan(
//                        MentionSpan(
//                            context = this@SpanActivity,
//                            placeholder = this@SpanActivity.drawable(R.drawable.ic_baseline_4k_24),
//                            image = 16.px,
//                            imagePadding = 0,
//                            ""
//                        ),
//                        8,
//                        9,
//                        0
//                    )
//                    textView5.text = spannableString4
//                }
//
////                override fun onLoadStarted(placeholder: Drawable?) {
////                    super.onLoadStarted(placeholder)
////                    Timber.d("onLoadStarted : ${ (System.nanoTime()/1000)}")
////                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                }
//            })
//
//
//        val spannableString5 = SpannableString("Строка с маркапом и болдом")
//        textView6.text = spannableString5
//
//        val spannableString = SpannableString("Строка с маркапом и болдом")
//        spannableString.setSpan(
//            MentionSpan(
//                context = this,
//                mResourceId = R.drawable.ic_mention_deafult,
//                imageSize = 16.px,
//                imagePadding = 0,
//                param = ""
//            ),
//            0,
//            6,
//            0
//        )
//
//        //textView4.movementMethod = LinkMovementMethod.getInstance()
//        textView4.text = spannableString
//
//        val spannableString2 = SpannableString("Строка с маркапом и болдом")
//        spannableString2.setSpan(
//            MentionSpan(this, R.drawable.ic_baseline_4k_24, null, 16.px, 0, ""),
//            9,
//            10,
//            0
//        )
//        textView2.text = spannableString2
//
//        val spannableString3 = SpannableString("Строка с маркапом и болдом")
//        spannableString3.setSpan(
//            MentionSpan(this, R.drawable.ic_baseline_4k_24, null, 16.px, 0, ""),
//            9,
//            17,
//            0
//        )
//        val clickableSpan = object : ClickableSpan() {
//            override fun onClick(p0: View) {
//                Toast.makeText(this@SpanActivity, "1983 onClick", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//
//        spannableString3.setSpan(
//            clickableSpan,
//            9,
//            17,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        textView3.movementMethod = LinkMovementMethod.getInstance()
//        textView3.text = spannableString3
    }
}
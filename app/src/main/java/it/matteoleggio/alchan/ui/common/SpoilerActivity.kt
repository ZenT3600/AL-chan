package it.matteoleggio.alchan.ui.common

import android.os.Bundle
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_spoiler.*
import java.net.URLDecoder

class SpoilerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spoiler)

        if (!intent?.data?.toString().isNullOrBlank()) {
            val data = intent.data?.toString()!!.substring(intent.data?.toString()!!.indexOf("=") + 1)
            val decodedData = URLDecoder.decode(data, "utf-8")

            val showSpoilerRegex = "(~!|!~)".toRegex()
            val finalData = decodedData.replace(showSpoilerRegex, "")

            val maxWidth = AndroidUtility.getScreenWidth(this)
            val markwon = AndroidUtility.initMarkwon(this)
            AndroidUtility.convertMarkdown(this, spoilerText, finalData, maxWidth, markwon)

            spoilerLayout.setOnClickListener {
                finish()
            }
        } else {
            finish()
        }
    }
}

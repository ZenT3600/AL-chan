package it.matteoleggio.alchan.ui.social

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.localstorage.AppSettingsManager
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.EditorType
import it.matteoleggio.alchan.helper.pojo.AppSettings
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.common.ScheduledTextEditorActivity
import it.matteoleggio.alchan.ui.common.TextEditorActivity
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.activity_scheduled_posts.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.days


class AlreadyScheduledPostActivity : BaseActivity() {
    private var isLoading = false
    private val appSettingManager: AppSettingsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_posts)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))

        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            title = getString(R.string.scheduled_posts)
            setDisplayHomeAsUpEnabled(true)
        }
        initLayout()
    }

    fun initLayout() {
        scheduledLinearLayout.removeAllViews()
        val displayMetrics = DisplayMetrics()
        val windowsManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        val scheduledPosts = appSettingManager.appSettings.scheduledPosts
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
        for (post in scheduledPosts) {
            try {
                noScheduled.visibility = View.GONE
            } catch (e: Exception) {}
            val viewFirst = LinearLayoutCompat(this)
            viewFirst.orientation = LinearLayoutCompat.VERTICAL
            viewFirst.setPadding(12, 12, 12, 12)
            viewFirst.gravity = Gravity.CENTER_VERTICAL

            val viewSecondTop = LinearLayoutCompat(this)
            viewSecondTop.orientation = LinearLayoutCompat.HORIZONTAL
            viewSecondTop.setPadding(12, 12, 12, 12)
            viewSecondTop.gravity = Gravity.CENTER

            val textPreview = TextView(this)
            textPreview.typeface = Typeface.MONOSPACE
            textPreview.textSize = 12F
            textPreview.maxWidth = deviceWidth - 200
            textPreview.width = deviceWidth - 200
            textPreview.text = post[2].take(32).replace("\n", "") + (if (post[2].length > 32) "..." else "")

            val cancelButton = AppCompatImageView(this)
            cancelButton.setImageDrawable(getDrawable(R.drawable.ic_cancel))
            cancelButton.setPadding(12, 12, 12, 12)
            cancelButton.setOnClickListener {
                val copyAppSettings = appSettingManager.appSettings
                copyAppSettings.scheduledPosts.remove(post)
                appSettingManager.setAppSettings(copyAppSettings)
                scheduledLinearLayout.removeView(viewFirst)
            }

            val editButton = AppCompatImageView(this)
            editButton.setImageDrawable(getDrawable(R.drawable.ic_create))
            editButton.setPadding(12, 12, 12, 12)
            editButton.setOnClickListener {
                val intent = Intent(this, ScheduledTextEditorActivity::class.java)
                intent.putExtra(TextEditorActivity.TEXT_CONTENT, post[2])
                intent.putExtra(TextEditorActivity.SCHEDULE_DATE, post[1])
                intent.putExtra(TextEditorActivity.ORIGINAL_DATE, post[0])
                intent.putExtra(TextEditorActivity.EDITING, true)
                intent.putExtra(ScheduledTextEditorActivity.EDITOR_TYPE, EditorType.SCHEDULE)
                startActivityForResult(intent, EditorType.SCHEDULE.ordinal)
                initLayout()
                // TODO: reopening this page after editing a scheduled post crashes the app
            }

            val progress = LinearProgressIndicator(this)
            val start = sdf.parse(post[0])
            val end = sdf.parse(post[1])
            val current = Calendar.getInstance().time.time.toInt()
            progress.isIndeterminate = false
            progress.max = end.time.toInt() - start.time.toInt()
            progress.progress = current - start.time.toInt()
            progress.layoutParams = ViewGroup.LayoutParams(deviceWidth - 20, 10)

            val endArray = end.toString().split(" ")
            val startArray = start.toString().split(" ")
            val startEndText = TextView(this)
            startEndText.typeface = Typeface.MONOSPACE
            startEndText.textSize = 11F
            startEndText.text = "Scheduled ${startArray[1]} ${startArray[2]} ${startArray[5]} ${startArray[3]} for ${endArray[1]} ${endArray[2]} ${endArray[5]} ${endArray[3]}"

            viewSecondTop.addView(textPreview)
            viewSecondTop.addView(editButton)
            viewSecondTop.addView(cancelButton)

            viewFirst.addView(viewSecondTop)
            viewFirst.addView(startEndText)
            viewFirst.addView(progress)

            scheduledLinearLayout.addView(viewFirst)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}

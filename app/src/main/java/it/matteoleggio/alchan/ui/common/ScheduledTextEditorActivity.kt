package it.matteoleggio.alchan.ui.common

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.localstorage.AppSettingsManager
import it.matteoleggio.alchan.data.repository.SocialRepository
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.EditorType
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.pojo.AppSettings
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.common.TextEditorActivity.Companion.EDITING
import it.matteoleggio.alchan.ui.common.TextEditorActivity.Companion.ORIGINAL_DATE
import it.matteoleggio.alchan.ui.common.TextEditorActivity.Companion.SCHEDULE_DATE
import kotlinx.android.synthetic.main.activity_scheduled_posts.*
import kotlinx.android.synthetic.main.activity_text_editor.*
import kotlinx.android.synthetic.main.dialog_input.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_app_settings.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class PostWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        ScheduledTextEditorActivity().viewModel.post(inputData.getString("text")!!)
        return Result.success()
    }
}


class ScheduledTextEditorActivity : BaseActivity() {

    private lateinit var date: Calendar

    val viewModel by viewModel<ScheduledTextEditorViewModel>()
    private val appSettingManager: AppSettingsManager by inject()

    private lateinit var rangeMarkdownLayout: ArrayList<AppCompatImageView>
    private lateinit var onlyStartMarkdownLayout: ArrayList<AppCompatImageView>
    private lateinit var dialogMarkdownLayout: ArrayList<AppCompatImageView>

    private val rangeMarkdown = arrayListOf(
        "____", "__", "~~~~", "~!!~", "~~~~~~", "``"
    )

    private val onlyStartMarkdown = arrayListOf(
        "1. ", "- ", "# ", "> "
    )

    private val dialogMarkdown = arrayListOf(
        "[link ]", "img220", "youtube", "webm"
    )

    companion object {
        const val EDITOR_TYPE = "editorType" // always required
        const val ACTIVITY_ID = "activityId" // needed if edit activity or reply to activity
        const val REPLY_ID = "replyId" // needed if edit a reply
        const val TEXT_CONTENT = "textContent" // needed if edit
        const val RECIPIENT_ID = "recipientId" // needed if send a message
        const val RECIPIENT_NAME = "recipientName" // needed if send a message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_text_editor)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))

        editorLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
            view.updateSidePadding(windowInsets, initialPadding)
        }

        editorFormatLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        viewModel.editorType = EditorType.valueOf(intent.getStringExtra(EDITOR_TYPE) ?: EditorType.ACTIVITY.name)

        textLimit.visibility = View.GONE
        if (intent.getBooleanExtra(EDITING, false)) {
            viewModel.originalText = intent.getStringExtra(TEXT_CONTENT)
            viewModel.scheduleDate = intent.getStringExtra(SCHEDULE_DATE)!!
            viewModel.scheduleDate = intent.getStringExtra(ORIGINAL_DATE)!!
        }

        setSupportActionBar(toolbarLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.title = if (viewModel.originalText != null) "${getString(R.string.edit_message)} (Scheduled)" else "${getString(R.string.post_new_message)} (Scheduled)"
        rangeMarkdownLayout = arrayListOf(
            formatBoldIcon, formatItalicIcon, formatStrikeThroughIcon, formatSpoilerIcon, formatCenterIcon, formatCodeIcon
        )

        onlyStartMarkdownLayout = arrayListOf(
            formatOrderedListIcon, formatUnorderedListIcon, formatHeaderIcon, formatQuoteIcon
        )

        dialogMarkdownLayout = arrayListOf(
            formatLinkIcon, formatImageIcon, formatYoutubeIcon, formatVideoIcon
        )

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.postTextActivityResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, R.string.message_posted)

                    val intent = Intent()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })
    }

    private fun initLayout() {
        editorEditText.requestFocus()


        if (appSettingManager.appSettings.postsCustomClipboard.size == 0) {
            newClipboard.visibility = View.GONE
        }
        newClipboard.setOnClickListener {
            val dots = "..."
            val nothing = ""
            val size = 32
            val menu = PopupMenu(this, newClipboard)
            menu.menu.apply {
                for (clip in appSettingManager.appSettings.postsCustomClipboard) {
                    if (clip[1] != "true") continue
                    add("${clip.take(size)}${if (clip.size > size) dots else nothing}").setOnMenuItemClickListener {
                        val start = editorEditText.selectionStart
                        editorEditText.text?.insert(start, clip[0])
                        true
                    }
                }
            }

            menu.show()
        }

        if (!viewModel.originalText.isNullOrBlank() && !viewModel.isInit) {
            editorEditText.setText(viewModel.originalText)
            viewModel.isInit = true
        }

        rangeMarkdownLayout.forEachIndexed { index, appCompatImageView ->
            appCompatImageView.setOnClickListener {
                val start = editorEditText.selectionStart
                val end = editorEditText.selectionEnd
                val markdown = rangeMarkdown[index]
                if (start == end) {
                    editorEditText.text?.insert(start, markdown)
                } else {
                    editorEditText.text?.insert(end, markdown.substring(markdown.length / 2))
                    editorEditText.text?.insert(start, markdown.substring(0, markdown.length / 2))
                }
            }
        }

        onlyStartMarkdownLayout.forEachIndexed { index, appCompatImageView ->
            appCompatImageView.setOnClickListener {
                val start = editorEditText.selectionStart
                val markdown = onlyStartMarkdown[index]
                editorEditText.text?.insert(start, markdown)
            }
        }

        dialogMarkdownLayout.forEachIndexed { index, appCompatImageView ->
            appCompatImageView.setOnClickListener {
                val inputDialogView = layoutInflater.inflate(R.layout.dialog_input, inputDialogLayout, false)
                val title = when (index) {
                    0 -> R.string.please_input_a_url
                    1 -> R.string.please_input_an_image_url
                    2 -> R.string.please_input_a_youtube_video_url
                    3 -> R.string.please_input_a_webm_video_url
                    else -> R.string.please_input_a_url
                }
                DialogUtility.showCustomViewDialog(
                    this,
                    title,
                    inputDialogView,
                    R.string.add,
                    {
                        val newEntry = inputDialogView.inputField.text.toString().trim()
                        if (newEntry.isNotBlank()) {
                            val start = editorEditText.selectionStart
                            val markdown = dialogMarkdown[index]
                            editorEditText.text?.insert(start, "${markdown}(${newEntry})")
                        }
                    },
                    R.string.cancel,
                    { }
                )
            }
        }

        previewButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("alchan://spoiler?data=${URLEncoder.encode(editorEditText.text?.trim().toString(), "utf-8")}")
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemPost) {
            if (editorEditText.text?.trim().isNullOrBlank()) {
                DialogUtility.showToast(this, R.string.please_write_something)
                return false
            }
            showDateTimePicker()

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDateTimePicker() {
        date = Calendar.getInstance()
        if (intent.getBooleanExtra(EDITING, false)) {
            val currentDateString = intent.getStringExtra(SCHEDULE_DATE)
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
            date.time = sdf.parse(currentDateString)
        }
        val picker = DatePickerDialog(this,
            { view, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                TimePickerDialog(this,
                    { view, hourOfDay, minute ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)

                        val c = Calendar.getInstance()
                        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
                        val getCurrentDateTime = sdf.format(c.time)
                        val getSelectedTime = sdf.format(date.time)

                        if (getCurrentDateTime < getSelectedTime) runOnUiThread { showAreYouSure() }
                        else Toast.makeText(this, "This is not a time machine!", Toast.LENGTH_SHORT).show()
                    },
                    date.get(Calendar.HOUR_OF_DAY),
                    date.get(Calendar.MINUTE),
                    false
                ).show()
            },
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DATE)
        )
        picker.show()
    }

    fun scheduleWorkManager(post: ArrayList<String>) {
        val ogDate = LocalDateTime.parse(post[0], DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm aa"))
        val selectedDate = LocalDateTime.parse(post[1], DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm aa"))
        val text = post[2]
        Log.d("TIME", (ogDate.minusSeconds(selectedDate.second.toLong())).second.toString())
        val logWorkRequest = OneTimeWorkRequestBuilder<PostWorker>()
            .setInitialDelay((ogDate.minusSeconds(selectedDate.second.toLong())).second.toLong(), TimeUnit.SECONDS)
            .setInputData(workDataOf("text" to text))
            .build()
    }

    private fun showAreYouSure() {
        var title = R.string.post_this_message
        var message = getString(R.string.are_you_sure_you_want_to_post_this_message)
        var positiveText = R.string.post
        if (intent.getBooleanExtra(EDITING, false)) {
            title = R.string.edit_this_message
            message = getString(R.string.are_you_sure_you_want_to_edit_this_message)
            positiveText = R.string.edit
        }

        val builder = AlertDialog.Builder(this)
        val text = editorEditText.text?.trim().toString()
        if (text == "reset") { val copyAppSettings = appSettingManager.appSettings; copyAppSettings.scheduledPosts = arrayListOf<ArrayList<String>>(); appSettingManager.setAppSettings(copyAppSettings); finish() }
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveText) { _, _ ->
                val c = Calendar.getInstance()
                runOnUiThread {
                    if (!intent.getBooleanExtra(EDITING, false)) addScheduledPost(c, date, text)
                    else modifyScheduledPosts(viewModel.originalDate, viewModel.scheduleDate, viewModel.originalText!!, date, text)
                    Toast.makeText(context, "Post successfully scheduled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            if (viewModel.activityId == null && viewModel.recipientId != null) {
                val c = Calendar.getInstance()
                runOnUiThread {
                    if (!intent.getBooleanExtra(EDITING, false)) addScheduledPost(c, date, text)
                    else modifyScheduledPosts(viewModel.originalDate, viewModel.scheduleDate, viewModel.originalText!!, date, text)
                    Toast.makeText(context, "Post successfully scheduled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            setNegativeButton(R.string.cancel) { _, _ -> }
            show()
        }
    }

    private fun modifyScheduledPosts(creationDate: String, selectedDate: String, text: String, newSelectedDate: Calendar, newText: String) {
        val copyAppSettings = appSettingManager.appSettings
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
        val newSelectedDateTime = sdf.format(newSelectedDate.time)
        val i = copyAppSettings.scheduledPosts.indexOf(arrayListOf(creationDate, selectedDate, text))
        if (i < 0) return
        copyAppSettings.scheduledPosts.drop(i)
        copyAppSettings.scheduledPosts.add(i, arrayListOf(creationDate, newSelectedDateTime, newText))
        appSettingManager.setAppSettings(copyAppSettings)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    fun addScheduledPost(creationDate: Calendar, selectedDate: Calendar, text: String) {
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
        val creationDateTime = sdf.format(creationDate.time)
        val selectedDateTime = sdf.format(selectedDate.time)
        val scheduledPosts: ArrayList<ArrayList<String>> = appSettingManager.appSettings.scheduledPosts
        scheduledPosts.add(arrayListOf(creationDateTime, selectedDateTime, text))
        appSettingManager.setAppSettings(
            AppSettings(
                appSettingManager.appSettings.appTheme,
                appSettingManager.appSettings.circularAvatar!!,
                appSettingManager.appSettings.whiteBackgroundAvatar!!,
                appSettingManager.appSettings.showRecentReviews!!,
                appSettingManager.appSettings.showSocialTabAutomatically!!,
                appSettingManager.appSettings.showBioAutomatically!!,
                appSettingManager.appSettings.showStatsAutomatically!!,
                appSettingManager.appSettings.useRelativeDate!!,
                appSettingManager.appSettings.sendAiringPushNotification!!,
                appSettingManager.appSettings.sendActivityPushNotification!!,
                appSettingManager.appSettings.sendForumPushNotification!!,
                appSettingManager.appSettings.sendFollowsPushNotification!!,
                appSettingManager.appSettings.sendRelationsPushNotification!!,
                appSettingManager.appSettings.mergePushNotifications!!,
                appSettingManager.appSettings.pushNotificationMinimumHours!!,
                appSettingManager.appSettings.postsCustomClipboard,
                scheduledPosts
            )
        )
    }
}

package it.matteoleggio.alchan.ui.auth

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import it.matteoleggio.alchan.BuildConfig
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.Utility
import it.matteoleggio.alchan.notifications.PushNotificationsService
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.main.BroadcastReceiverNotifs
import it.matteoleggio.alchan.ui.main.MainActivity
import kotlinx.coroutines.Job
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {

    private val viewModel by viewModel<SplashViewModel>()

    private var job: Job? = null

    private val isTrue = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (viewModel.appSettings.showSocialTabAutomatically == null) {
            viewModel.setDefaultAppSetting(AndroidUtility.isLowOnMemory(this))
        }

        setupObserver()
    }

    private fun setupObserver() {
        viewModel.announcementResponse.observe(this, Observer {
            if (it.responseStatus == ResponseStatus.SUCCESS) {
                try {
                    if (it.data == null || it.data.id.isBlank()) {
                        moveToNextPage()
                        return@Observer
                    }

                    // Handle update announcement
                    // Show update dialog if app_version has a value
                    if (it.data.app_version.isNotBlank() && BuildConfig.VERSION_CODE < it.data.app_version.toInt()) {
                        AlertDialog.Builder(this).apply {
                            setTitle(R.string.new_update_is_available)
                            setMessage(it.data.message)
                            setCancelable(false)
                            setPositiveButton(R.string.go_to_play_store) { _, _ ->
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.PLAY_STORE_URL)))
                                finish()
                            }
                            // show later if required_update is not "1"
                            if (it.data.required_update != isTrue) {
                                setNegativeButton(R.string.later) { _, _ ->
                                    moveToNextPage()
                                }
                            }
                            show()
                        }
                        return@Observer
                    }

                    // Handle message announcement
                    // Show announcement if last saved announcement id is not the same as new id and if dates exist
                    if (viewModel.lastAnnouncementId == it.data.id.toInt()) {
                        moveToNextPage()
                        return@Observer
                    }

                    if (it.data.from_date.isNotBlank() &&
                        it.data.until_date.isNotBlank() &&
                        Utility.isBetweenTwoDates(it.data.from_date, it.data.until_date)
                    ) {
                        AlertDialog.Builder(this).apply {
                            setMessage(it.data.message)
                            setCancelable(false)
                            setPositiveButton(R.string.ok) { _, _ ->
                                moveToNextPage()
                            }
                            setNegativeButton(R.string.dont_show_again) { _, _ ->
                                viewModel.setNeverShowAgain(it.data.id.toInt())
                                moveToNextPage()
                            }
                            show()
                        }
                        return@Observer
                    }

                    moveToNextPage()
                } catch (e: Exception) {
                    moveToNextPage()
                }
            } else {
                moveToNextPage()
            }
        })

        viewModel.getAnnouncement()
    }

    private fun moveToNextPage() {
        if (viewModel.isLoggedIn) {
            val mIntent = Intent(this, BroadcastReceiverNotifs::class.java)
            PendingIntent.getBroadcast(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT).send()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}

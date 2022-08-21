package it.matteoleggio.alchan.ui.settings.app

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.pojo.AppSettings

class AppSettingsViewModel(private val appSettingsRepository: AppSettingsRepository) : ViewModel() {

    var isInit = false
    var selectedAppTheme: AppColorTheme? = null
    var pushNotificationsMinHours: Double? = null

    val appSettings: AppSettings
        get() = appSettingsRepository.appSettings

    fun setAppSettings(
        circularAvatar: Boolean = true,
        whiteBackgroundAvatar: Boolean = true,
        showRecentReviews: Boolean = true,
        showSocialTab: Boolean,
        showBio: Boolean,
        showStats: Boolean,
        useRelativeDate: Boolean = false,
        sendAiringPushNotifications: Boolean = true,
        sendActivityPushNotifications: Boolean = true,
        sendForumPushNotifications: Boolean = true,
        sendFollowsPushNotifications: Boolean = true,
        sendRelationsPushNotifications: Boolean = true,
        mergePushNotifications: Boolean = false,
        pushNotificationsMinHoursPassed: Double = 0.5,
        postsCustomClipboard: ArrayList<String> = arrayListOf(),
        fetchFromMal: Boolean,
        username: Int
    ) {
        appSettingsRepository.setAppSettings(AppSettings(
            appTheme = selectedAppTheme,
            circularAvatar = circularAvatar,
            whiteBackgroundAvatar = whiteBackgroundAvatar,
            showRecentReviews = showRecentReviews,
            showSocialTabAutomatically = showSocialTab,
            showBioAutomatically = showBio,
            showStatsAutomatically = showStats,
            useRelativeDate = useRelativeDate,
            sendAiringPushNotification = sendAiringPushNotifications,
            sendActivityPushNotification = sendActivityPushNotifications,
            sendForumPushNotification = sendForumPushNotifications,
            sendFollowsPushNotification = sendFollowsPushNotifications,
            sendRelationsPushNotification = sendRelationsPushNotifications,
            mergePushNotifications = mergePushNotifications,
            pushNotificationMinimumHours = pushNotificationsMinHoursPassed,
            postsCustomClipboard = postsCustomClipboard,
            fetchFromMal = fetchFromMal,
            userid = username
        ))
    }
}
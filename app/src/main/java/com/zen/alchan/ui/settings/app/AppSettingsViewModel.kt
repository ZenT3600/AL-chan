package com.zen.alchan.ui.settings.app

import androidx.lifecycle.ViewModel
import com.zen.alchan.data.repository.AppSettingsRepository
import com.zen.alchan.data.repository.OtherUserRepository
import com.zen.alchan.data.repository.UserRepository
import com.zen.alchan.data.response.User
import com.zen.alchan.helper.enums.AppColorTheme
import com.zen.alchan.helper.pojo.AppSettings
import type.StaffLanguage

class AppSettingsViewModel(private val appSettingsRepository: AppSettingsRepository,
                           private val userRepository: UserRepository
) : ViewModel() {

    var isInit = false
    var selectedAppTheme: AppColorTheme? = null
    var pushNotificationsMinHours: Int? = null
    var allAnimeListPosition: Int? = null
    var allMangaListPosition: Int? = null

    val appSettings: AppSettings
        get() = appSettingsRepository.appSettings

    val viewerData: User?
        get() = userRepository.currentUser

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
        mergePushNotifications: Boolean = false
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
            pushNotificationMinimumHours = pushNotificationsMinHours,
            allAnimeListPosition = allAnimeListPosition,
            allMangaListPosition = allMangaListPosition
        ))
    }
}
package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.helper.enums.AppColorTheme
import type.StaffLanguage

class AppSettings(
    var appTheme: AppColorTheme? = AppColorTheme.DEFAULT_THEME_YELLOW,
    var circularAvatar: Boolean? = true,
    var whiteBackgroundAvatar: Boolean? = true,
    var showRecentReviews: Boolean? = true,
    var showSocialTabAutomatically: Boolean? = null,
    var showBioAutomatically: Boolean? = null,
    var showStatsAutomatically: Boolean? = null,
    var useRelativeDate: Boolean? = null,
    var sendAiringPushNotification: Boolean? = null,
    var sendActivityPushNotification: Boolean? = null,
    var sendForumPushNotification: Boolean? = null,
    var sendFollowsPushNotification: Boolean? = null,
    var sendRelationsPushNotification: Boolean? = null,
    var mergePushNotifications: Boolean? = null,
    var pushNotificationMinimumHours: Double? = null,
    var postsCustomClipboard: ArrayList<ArrayList<String>> = arrayListOf(),
    var scheduledPosts: ArrayList<ArrayList<String>> = arrayListOf()
)
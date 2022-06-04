package it.matteoleggio.alchan.data.localstorage

import it.matteoleggio.alchan.data.response.MediaTagCollection
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.pojo.AppSettings
import it.matteoleggio.alchan.helper.pojo.BestFriend
import it.matteoleggio.alchan.helper.pojo.ListStyle
import it.matteoleggio.alchan.helper.pojo.UserPreferences
import type.StaffLanguage

interface LocalStorage {
    val scheduledPosts: ArrayList<ArrayList<String>>
    var bearerToken: String?

    var appSettings: AppSettings
    var userPreferences: UserPreferences

    var viewerData: User?
    var viewerDataLastRetrieved: Long?

    var followersCount: Int?
    var followersCountLastRetrieved: Long?

    var followingsCount: Int?
    var followingsCountLastRetrieved: Long?

    var genreList: List<String?>?
    var genreListLastRetrieved: Long?

    var tagList: List<MediaTagCollection>?
    var tagListLastRetrieved: Long?

    var mostTrendingAnimeBanner: String?

    var animeListStyle: ListStyle
    var mangaListStyle: ListStyle

    var lastAnnouncementId: Int?

    var bestFriends: List<BestFriend>?

    var latestNotification: Int?

    var lastPushNotificationTimestamp: Long?

    fun clearStorage()
}
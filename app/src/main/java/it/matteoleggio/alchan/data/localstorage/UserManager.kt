package it.matteoleggio.alchan.data.localstorage

import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.pojo.BestFriend

interface UserManager {
    val bearerToken: String?
    val viewerData: User?
    val viewerDataLastRetrieved: Long?
    val followersCount: Int
    val followersCountLastRetrieved: Long?
    val followingsCount: Int
    val followingsCountLastRetrieved: Long?
    val bestFriends: List<BestFriend>?
    val latestNotification: Int?
    val lastPushNotificationTimestamp: Long?

    fun setBearerToken(token: String?)
    fun setViewerData(user: User?)
    fun setFollowersCount(followers: Int)
    fun setFollowingsCount(followings: Int)
    fun setBestFriends(list: List<BestFriend>?)
    fun setLatestNotification(notificationId: Int)
    fun setLastPushNotificationTimestamp(timestamp: Long)
}
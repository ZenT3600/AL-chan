package it.matteoleggio.alchan.ui.social

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.repository.*
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.data.response.UserAvatar
import it.matteoleggio.alchan.helper.pojo.ActivityItem
import it.matteoleggio.alchan.helper.pojo.ActivityReply
import it.matteoleggio.alchan.helper.pojo.BestFriend
import it.matteoleggio.alchan.helper.pojo.SocialFilter
import type.ActivityType
import type.LikeableType

class SocialViewModel(private val mediaRepository: MediaRepository,
                      private val userRepository: UserRepository,
                      private val socialRepository: SocialRepository,
                      private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val textActivityText: String
        get() = socialRepository.textActivityText

    val listActivityText: String
        get() = socialRepository.listActivityText

    val messageActivityText: String
        get() = socialRepository.messageActivityText

    var isInit = false

    var socialFilter = SocialFilter(arrayListOf(), null, appSettingsRepository.userPreferences.socialActivityType, null)

    private val savedBestFriends: List<BestFriend>?
        get() = userRepository.bestFriends

    val activityList = ArrayList<ActivityItem>()

    val mostTrendingAnimeBannerLiveData by lazy {
        mediaRepository.mostTrendingAnimeBannerLivaData
    }

    val bestFriendChangedNotifier by lazy {
        userRepository.bestFriendChangedNotifier
    }

    val notifyFriendsActivity by lazy {
        socialRepository.notifyFriendsActivity
    }

    val friendsActivityResponse by lazy {
        socialRepository.friendsActivityResponse
    }

    val toggleLikeResponse by lazy {
        socialRepository.toggleLikeResponse
    }

    val toggleActivitySubscriptionResponse by lazy {
        socialRepository.toggleActivitySubscriptionResponse
    }

    val deleteActivityResponse by lazy {
        socialRepository.deleteActivityResponse
    }

    val currentUserId: Int?
        get() = userRepository.currentUser?.id

    val enableSocial: Boolean
        get() = appSettingsRepository.appSettings.showSocialTabAutomatically != false

    fun initData() {
        if (!isInit) {
            isInit = true
            reinitBestFriends()
            socialRepository.getFriendsActivity(socialFilter.selectedActivityType, if (socialFilter.selectedBestFriend != null) socialFilter.selectedBestFriend?.id!! else null)
        }
    }

    fun retrieveFriendsActivity() {
        socialRepository.getFriendsActivity(socialFilter.selectedActivityType, if (socialFilter.selectedBestFriend != null) socialFilter.selectedBestFriend?.id!! else null)
    }

    fun reinitBestFriends() {
        val bestFriendList = ArrayList<BestFriend>()
        bestFriendList.add(BestFriend(null, null, null))
        savedBestFriends?.forEach {
            bestFriendList.add(it)
        }
        socialFilter.bestFriends = bestFriendList
    }

    fun toggleLike(id: Int) {
        socialRepository.toggleLike(id, LikeableType.ACTIVITY)
    }

    fun toggleSubscription(id: Int, subscribe: Boolean) {
        socialRepository.toggleActivitySubscription(id, subscribe)
    }

    fun deleteActivity(id: Int) {
        socialRepository.deleteActivity(id)
    }

    fun changeActivityType(activityTypes: ArrayList<ActivityType>?) {
        socialFilter.selectedActivityType = activityTypes

        val savedUserPreferences = appSettingsRepository.userPreferences
        savedUserPreferences.socialActivityType = activityTypes
        appSettingsRepository.setUserPreferences(savedUserPreferences)
    }
}
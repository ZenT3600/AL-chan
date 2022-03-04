package it.matteoleggio.alchan.ui.browse.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.OtherUserRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.helper.enums.ProfileSection
import it.matteoleggio.alchan.helper.pojo.BestFriend
import fragment.MediaListScoreCollection
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.sqrt

class UserViewModel(private val otherUserRepository: OtherUserRepository,
                    private val userRepository: UserRepository,
                    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _currentSection = MutableLiveData<ProfileSection>()
    val currentSection: LiveData<ProfileSection>
        get() = _currentSection

    var userId: Int? = null
    var currentIsFollowing: Boolean? = null

    val currentUserId: Int
        get() = userRepository.currentUser?.id!!

    val userDataResponse by lazy {
        otherUserRepository.userDataResponse
    }

    val userData by lazy {
        otherUserRepository.userData
    }

    val followersCount by lazy {
        otherUserRepository.followersCount
    }

    val followingsCount by lazy {
        otherUserRepository.followingsCount
    }

    val toggleFollowingResponse by lazy {
        userRepository.toggleFollowResponse
    }

    val circularAvatar
        get() = appSettingsRepository.appSettings.circularAvatar == true

    val whiteBackgroundAvatar
        get() = appSettingsRepository.appSettings.whiteBackgroundAvatar == true

    val isBestFriend: Boolean
        get() = userRepository.bestFriends?.find { it.id == userId } != null

    val enableSocial: Boolean
        get() = appSettingsRepository.appSettings.showSocialTabAutomatically == true

    val animeScoreCollectionResponse by lazy {
        otherUserRepository.animeScoresCollectionResponse
    }

    val mangaScoreCollectionResponse by lazy {
        otherUserRepository.mangaScoresCollectionResponse
    }

    var animeAffinityText = ""
    var mangaAffinityText = ""

    fun initData() {
        if (userId == null) {
            return
        }

        otherUserRepository.retrieveUserData(userId!!)
        otherUserRepository.getFollowersCount(userId!!)
        otherUserRepository.getFollowingsCount(userId!!)

        if (currentUserId != userId) {
            otherUserRepository.getAnimeScoresCollection(currentUserId, userId!!)
            otherUserRepository.getMangaScoresCollection(currentUserId, userId!!)
        }

        if (currentSection.value == null) {
            _currentSection.postValue(ProfileSection.BIO)
        }
    }

    fun setProfileSection(section: ProfileSection) {
        _currentSection.postValue(section)
    }

    fun retrieveUserData() {
        if (userId == null) {
            return
        }

        otherUserRepository.retrieveUserData(userId!!)
        otherUserRepository.getFollowersCount(userId!!)
        otherUserRepository.getFollowingsCount(userId!!)
    }

    fun triggerRefreshChildFragments() {
        if (userId == null) {
            return
        }

        otherUserRepository.triggerRefreshProfilePageChild(userId!!)
    }

    fun toggleFollow() {
        if (userId == null) {
            return
        }

        userRepository.toggleFollow(userId!!)
    }

    fun refreshFollowingCount() {
        if (userId == null) {
            return
        }

        otherUserRepository.getFollowingsCount(userId!!)
        otherUserRepository.getFollowersCount(userId!!)
    }

    fun handleBestFriend(isEdit: Boolean = false) {
        if (userId != null) {
            userRepository.handleBestFriend(BestFriend(userId, userData.value?.user?.name, userData.value?.user?.avatar?.medium), isEdit)
        }
    }

    fun calculateAffinity(comparedScores: MediaListScoreCollectionQuery.Data?): Pair<Int, Double>? {
        if (comparedScores?.currentUser == null || comparedScores.otherUser == null) {
            return null
        }

        // maps are used to prevent duplicate entry (because of custome lists)
        val currentUserScoreMap = HashMap<Int, Double>()
        val otherUserScoreMap = HashMap<Int, Double>()

        val currentUserScoreList = ArrayList<Double>()
        val otherUserScoreList = ArrayList<Double>()

        comparedScores.currentUser.fragments.mediaListScoreCollection.lists?.forEach { list ->
            list?.entries?.forEach { entry ->
                if (entry?.media?.id != null &&
                    entry.score != null &&
                    entry.score != 0.0 &&
                    !currentUserScoreMap.containsKey(entry.media.id)
                ) {
                    currentUserScoreMap[entry.media.id] = entry.score
                }
            }
        }

        comparedScores.otherUser.fragments.mediaListScoreCollection.lists?.forEach { list ->
            list?.entries?.forEach { entry ->
                if (entry?.media?.id != null &&
                    entry.score != null &&
                    entry.score != 0.0 &&
                    currentUserScoreMap.containsKey(entry.media.id) &&
                    !otherUserScoreMap.containsKey(entry.media.id)
                ) {
                    otherUserScoreMap[entry.media.id] = entry.score

                    currentUserScoreList.add(currentUserScoreMap[entry.media.id] ?: 0.0)
                    otherUserScoreList.add(entry.score)
                }
            }
        }

        if (currentUserScoreList.size < 10) {
            return null
        } else {
            val pearson = calculatePearsonCorrelation(currentUserScoreList, otherUserScoreList) ?: return null
            return Pair(currentUserScoreList.size, pearson * 100)
        }
    }

    private fun calculatePearsonCorrelation(x: ArrayList<Double>, y: ArrayList<Double>): Double? {
        val mx = x.average()
        val my = y.average()

        val xm = x.map { it - mx }
        val ym = y.map { it - my }

        val sx = xm.map { it.pow(2) }
        val sy = ym.map { it.pow(2) }

        val num = xm.zip(ym).fold(0.0, { acc, pair -> acc + pair.first * pair.second })
        val den = sqrt(sx.sum() * sy.sum())

        return if (den == 0.0) null else num.toDouble() / den
    }
}
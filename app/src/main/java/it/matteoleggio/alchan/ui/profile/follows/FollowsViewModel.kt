package it.matteoleggio.alchan.ui.profile.follows

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.repository.OtherUserRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.helper.enums.FollowPage
import it.matteoleggio.alchan.helper.pojo.FollowsItem

class FollowsViewModel(private val userRepository: UserRepository,
                       private val otherUserRepository: OtherUserRepository
): ViewModel() {

    var otherUserId: Int? = null

    lateinit var followPage: FollowPage

    var page = 1
    var hasNextPage = true
    var isInit = false
    var followsList = ArrayList<FollowsItem?>()

    val userFollowingsResponse by lazy {
        userRepository.userFollowingsResponse
    }

    val userFollowersResponse by lazy {
        userRepository.userFollowersResponse
    }

    val toggleFollowingResponse by lazy {
        userRepository.toggleFollowingResponse
    }

    val toggleFollowerResponse by lazy {
        userRepository.toggleFollowerResponse
    }

    val otherUserFollowingsResponse by lazy {
        otherUserRepository.userFollowingsResponse
    }

    val otherUserFollowersResponse by lazy {
        otherUserRepository.userFollowersResponse
    }

    fun getItem() {
        if (!hasNextPage) {
            return
        }

        if (followPage == FollowPage.FOLLOWING) {
            if (otherUserId != null) {
                otherUserRepository.getUserFollowings(otherUserId!!, page)
            } else {
                userRepository.getUserFollowings(page)
            }
        } else {
            if (otherUserId != null) {
                otherUserRepository.getUserFollowers(otherUserId!!, page)
            } else {
                userRepository.getUserFollowers(page)
            }
        }
    }

    fun toggleFollow(userId: Int) {
        userRepository.toggleFollow(userId, followPage)
    }

    fun getFollowingsObserver(): LiveData<Resource<UserFollowingsQuery.Data>> {
        return if (otherUserId != null) {
            otherUserFollowingsResponse
        } else {
            userFollowingsResponse
        }
    }

    fun getFollowersObserver(): LiveData<Resource<UserFollowersQuery.Data>> {
        return if (otherUserId != null) {
            otherUserFollowersResponse
        } else {
            userFollowersResponse
        }
    }
}
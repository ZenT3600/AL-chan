package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.pojo.ActivityItem
import it.matteoleggio.alchan.helper.pojo.KeyValueItem
import type.ActivityType
import type.LikeableType

interface SocialRepository {

    val textActivityText: String
    val listActivityText: String
    val messageActivityText: String

    val notifyFriendsActivity: LiveData<Boolean>
    val notifyActivityList: LiveData<Boolean>
    val notifyGlobalActivity: LiveData<Boolean>

    val friendsActivityResponse: LiveData<Resource<ActivityQuery.Data>>
    val toggleLikeResponse: LiveData<Resource<ActivityItem>>
    val toggleActivitySubscriptionResponse: LiveData<Resource<ActivityItem>>
    val deleteActivityResponse: LiveData<Resource<Int>>

    val activityDetailResponse: LiveData<Resource<ActivityDetailQuery.Data>>
    val toggleLikeDetailResponse: LiveData<Resource<ActivityItem>>
    val toggleActivitySubscriptionDetailResponse: LiveData<Resource<ActivityItem>>
    val deleteActivityDetailResponse: LiveData<Resource<Boolean>>
    val deleteActivityReplyResponse: LiveData<Resource<Int>>

    val activityListResponse: LiveData<Resource<ActivityQuery.Data>>
    val globalActivityListResponse: LiveData<Resource<ActivityQuery.Data>>

    val postTextActivityResponse: LiveData<Resource<SaveTextActivityMutation.Data>>
    val postMessageActivityResponse: LiveData<Resource<SaveMessageActivityMutation.Data>>
    val postActivityReplyResponse: LiveData<Resource<SaveActivityReplyMutation.Data>>

    fun getFriendsActivity(typeIn: List<ActivityType>?, userId: Int?)
    fun getActivityDetail(id: Int)

    fun toggleLike(id: Int, type: LikeableType, fromDetail: Boolean = false)
    fun toggleActivitySubscription(activityId: Int, subscribe: Boolean, fromDetail: Boolean = false)
    fun deleteActivity(id: Int, fromDetail: Boolean = false)
    fun deleteActivityReply(id: Int)

    fun getActivityList(page: Int, typeIn: List<ActivityType>?, userId: Int)
    fun getGlobalActivityList(page: Int, typeIn: List<ActivityType>?, userId: Int?, following: Boolean?)

    fun postTextActivity(id: Int?, text: String)
    fun postMessageActivity(id: Int?, message: String, recipientId: Int, private: Boolean)
    fun postActivityReply(id: Int?, activityId: Int, text: String)

    fun notifyAllActivityList()
}
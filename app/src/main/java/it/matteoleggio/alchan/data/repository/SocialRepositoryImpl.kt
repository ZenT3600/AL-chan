package it.matteoleggio.alchan.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.api.Response
import it.matteoleggio.alchan.data.datasource.SocialDataSource
import it.matteoleggio.alchan.data.localstorage.UserManager
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.data.response.UserAvatar
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.pojo.ActivityItem
import it.matteoleggio.alchan.helper.pojo.KeyValueItem
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import type.ActivityType
import type.LikeableType

class SocialRepositoryImpl(private val socialDataSource: SocialDataSource,
                           private val userManager: UserManager) : SocialRepository {

    companion object {
        private const val TEXT_ACTIVITY = "TextActivity"
        private const val LIST_ACTIVITY = "ListActivity"
        private const val MESSAGE_ACTIVITY = "MessageActivity"
    }

    override val textActivityText: String
        get() = TEXT_ACTIVITY

    override val listActivityText: String
        get() = LIST_ACTIVITY

    override val messageActivityText: String
        get() = MESSAGE_ACTIVITY

    private val _notifyFriendsActivity = SingleLiveEvent<Boolean>()
    override val notifyFriendsActivity: LiveData<Boolean>
        get() = _notifyFriendsActivity

    private val _notifyActivityList = SingleLiveEvent<Boolean>()
    override val notifyActivityList: LiveData<Boolean>
        get() = _notifyActivityList

    private val _notifyGlobalActivity = SingleLiveEvent<Boolean>()
    override val notifyGlobalActivity: LiveData<Boolean>
        get() = _notifyGlobalActivity

    private val _friendsActivityResponse = SingleLiveEvent<Resource<ActivityQuery.Data>>()
    override val friendsActivityResponse: LiveData<Resource<ActivityQuery.Data>>
        get() = _friendsActivityResponse

    private val _toggleLikeResponse = MutableLiveData<Resource<ActivityItem>>()
    override val toggleLikeResponse: LiveData<Resource<ActivityItem>>
        get() = _toggleLikeResponse

    private val _toggleActivitySubscriptionResponse = MutableLiveData<Resource<ActivityItem>>()
    override val toggleActivitySubscriptionResponse: LiveData<Resource<ActivityItem>>
        get() = _toggleActivitySubscriptionResponse

    private val _deleteActivityResponse = MutableLiveData<Resource<Int>>()
    override val deleteActivityResponse: LiveData<Resource<Int>>
        get() = _deleteActivityResponse

    private val _activityDetailResponse = SingleLiveEvent<Resource<ActivityDetailQuery.Data>>()
    override val activityDetailResponse: LiveData<Resource<ActivityDetailQuery.Data>>
        get() = _activityDetailResponse

    private val _toggleLikeDetailResponse = SingleLiveEvent<Resource<ActivityItem>>()
    override val toggleLikeDetailResponse: LiveData<Resource<ActivityItem>>
        get() = _toggleLikeDetailResponse

    private val _toggleActivitySubscriptionDetailResponse = SingleLiveEvent<Resource<ActivityItem>>()
    override val toggleActivitySubscriptionDetailResponse: LiveData<Resource<ActivityItem>>
        get() = _toggleActivitySubscriptionDetailResponse

    private val _deleteActivityDetailResponse = SingleLiveEvent<Resource<Boolean>>()
    override val deleteActivityDetailResponse: LiveData<Resource<Boolean>>
        get() = _deleteActivityDetailResponse

    private val _deleteActivityReplyResponse = SingleLiveEvent<Resource<Int>>()
    override val deleteActivityReplyResponse: LiveData<Resource<Int>>
        get() = _deleteActivityReplyResponse

    private val _activityListResponse = SingleLiveEvent<Resource<ActivityQuery.Data>>()
    override val activityListResponse: LiveData<Resource<ActivityQuery.Data>>
        get() = _activityListResponse

    private val _globalActivityListResponse = SingleLiveEvent<Resource<ActivityQuery.Data>>()
    override val globalActivityListResponse: LiveData<Resource<ActivityQuery.Data>>
        get() = _globalActivityListResponse

    private val _postTextActivityResponse = SingleLiveEvent<Resource<SaveTextActivityMutation.Data>>()
    override val postTextActivityResponse: LiveData<Resource<SaveTextActivityMutation.Data>>
        get() = _postTextActivityResponse

    private val _postMessageActivityResponse = SingleLiveEvent<Resource<SaveMessageActivityMutation.Data>>()
    override val postMessageActivityResponse: LiveData<Resource<SaveMessageActivityMutation.Data>>
        get() = _postMessageActivityResponse

    private val _postActivityReplyResponse = SingleLiveEvent<Resource<SaveActivityReplyMutation.Data>>()
    override val postActivityReplyResponse: LiveData<Resource<SaveActivityReplyMutation.Data>>
        get() = _postActivityReplyResponse

    @SuppressLint("CheckResult")
    override fun getFriendsActivity(typeIn: List<ActivityType>?, userId: Int?) {
        _friendsActivityResponse.postValue(Resource.Loading())

        socialDataSource.getFriendsActivity(
            typeIn,
            userId,
            if (userId == null) { null } else { if (userManager.viewerData?.id != null) listOf(userManager.viewerData?.id!!) else null}
        ).subscribeWith(AndroidUtility.rxApolloCallback(_friendsActivityResponse))
    }

    @SuppressLint("CheckResult")
    override fun getActivityDetail(id: Int) {
        _activityDetailResponse.postValue(Resource.Loading())
        socialDataSource.getActivityDetail(id).subscribeWith(AndroidUtility.rxApolloCallback(_activityDetailResponse))
    }

    @SuppressLint("CheckResult")
    override fun toggleLike(id: Int, type: LikeableType, fromDetail: Boolean) {
        if (fromDetail) {
            _toggleLikeDetailResponse.postValue(Resource.Loading())
        } else {
            _toggleLikeResponse.postValue(Resource.Loading())
        }

        socialDataSource.toggleLike(id, type).subscribeWith(object : Observer<Response<ToggleLikeMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ToggleLikeMutation.Data>) {
                if (t.hasErrors()) {
                    if (fromDetail) {
                        _toggleLikeDetailResponse.postValue(Resource.Error(t.errors!![0].message))
                    } else {
                        _toggleLikeResponse.postValue(Resource.Error(t.errors!![0].message))
                    }
                } else {
                    val likeUsers = ArrayList<User>()
                    t.data?.toggleLike?.forEach { like ->
                        likeUsers.add(
                            User(
                                id = like?.id!!,
                                name = like.name,
                                avatar = UserAvatar(null, like.avatar?.medium)
                            )
                        )
                    }

                    val activityItem = ActivityItem(
                        id = id,
                        likeCount = t.data?.toggleLike?.size ?: 0,
                        isLiked = t.data?.toggleLike?.find { like -> like?.name == userManager.viewerData?.name } != null,
                        likes = likeUsers
                    )

                    if (fromDetail) {
                        _toggleLikeDetailResponse.postValue(Resource.Success(activityItem))
                    }

                    _toggleLikeResponse.postValue(Resource.Success(activityItem))
                }
            }

            override fun onError(e: Throwable) {
                if (fromDetail) {
                    AndroidUtility.rxApolloHandleError(_toggleLikeDetailResponse, e)
                } else {
                    AndroidUtility.rxApolloHandleError(_toggleLikeResponse, e)
                }
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun toggleActivitySubscription(activityId: Int, subscribe: Boolean, fromDetail: Boolean) {
        if (fromDetail) {
            _toggleActivitySubscriptionDetailResponse.postValue(Resource.Loading())
        } else {
            _toggleActivitySubscriptionResponse.postValue(Resource.Loading())
        }

        socialDataSource.toggleActivitySubscription(activityId, subscribe).subscribeWith(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onError(e: Throwable) {
                if (fromDetail) {
                    AndroidUtility.rxApolloHandleError(_toggleActivitySubscriptionDetailResponse, e)
                } else {
                    AndroidUtility.rxApolloHandleError(_toggleActivitySubscriptionResponse, e)
                }
            }

            override fun onComplete() {
                val activityItem = ActivityItem(id = activityId, isSubscribed = subscribe)

                if (fromDetail) {
                    _toggleActivitySubscriptionDetailResponse.postValue(Resource.Success(activityItem))
                }

                _toggleActivitySubscriptionResponse.postValue(Resource.Success(activityItem))
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun deleteActivity(id: Int, fromDetail: Boolean) {
        if (fromDetail) {
            _deleteActivityDetailResponse.postValue(Resource.Loading())
        } else {
            _deleteActivityResponse.postValue(Resource.Loading())
        }

        socialDataSource.deleteActivity(id).subscribeWith(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onError(e: Throwable) {
                if (fromDetail) {
                    AndroidUtility.rxApolloHandleError(_deleteActivityDetailResponse, e)
                } else {
                    AndroidUtility.rxApolloHandleError(_deleteActivityResponse, e)
                }
            }

            override fun onComplete() {
                if (fromDetail) {
                    _deleteActivityDetailResponse.postValue(Resource.Success(true))
                }

                _deleteActivityResponse.postValue(Resource.Success(id))
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun deleteActivityReply(id: Int) {
        _deleteActivityReplyResponse.postValue(Resource.Loading())
        socialDataSource.deleteActivityReply(id).subscribeWith(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_deleteActivityReplyResponse, e)
            }

            override fun onComplete() {
                _deleteActivityReplyResponse.postValue(Resource.Success(id))

                // this is to inform list that something has changed
                _notifyFriendsActivity.postValue(true)
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun getActivityList(page: Int, typeIn: List<ActivityType>?, userId: Int) {
        socialDataSource.getActivities(page, typeIn, userId, null).subscribeWith(AndroidUtility.rxApolloCallback(_activityListResponse))
    }

    @SuppressLint("CheckResult")
    override fun getGlobalActivityList(
        page: Int,
        typeIn: List<ActivityType>?,
        userId: Int?,
        following: Boolean?
    ) {
        socialDataSource.getActivities(page, typeIn, userId, following).subscribeWith(AndroidUtility.rxApolloCallback(_globalActivityListResponse))
    }

    @SuppressLint("CheckResult")
    override fun postTextActivity(id: Int?, text: String) {
        _postTextActivityResponse.postValue(Resource.Loading())
        socialDataSource.saveTextActivity(id, text).subscribeWith(AndroidUtility.rxApolloCallback(_postTextActivityResponse))
    }

    @SuppressLint("CheckResult")
    override fun postMessageActivity(id: Int?, message: String, recipientId: Int, private: Boolean) {
        _postMessageActivityResponse.postValue(Resource.Loading())
        socialDataSource.saveMessageActivity(id, message, recipientId, private).subscribeWith(AndroidUtility.rxApolloCallback(_postMessageActivityResponse))
    }

    @SuppressLint("CheckResult")
    override fun postActivityReply(id: Int?, activityId: Int, text: String) {
        _postActivityReplyResponse.postValue(Resource.Loading())
        socialDataSource.saveActivityReply(id, activityId, text).subscribeWith(AndroidUtility.rxApolloCallback(_postActivityReplyResponse))
    }

    override fun notifyAllActivityList() {
        _notifyFriendsActivity.postValue(true)
        _notifyActivityList.postValue(true)
        _notifyGlobalActivity.postValue(true)
    }
}
package it.matteoleggio.alchan.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import it.matteoleggio.alchan.data.datasource.UserDataSource
import it.matteoleggio.alchan.data.localstorage.UserManager
import it.matteoleggio.alchan.data.network.Converter
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.MediaListTypeOptions
import it.matteoleggio.alchan.data.response.NotificationOption
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.enums.FollowPage
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.pojo.BestFriend
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import type.NotificationType
import type.ScoreFormat
import type.UserTitleLanguage

class UserRepositoryImpl(private val userDataSource: UserDataSource,
                         private val userManager: UserManager
) : UserRepository {

    override val currentUser: User?
        get() = userManager.viewerData

    private val _sessionResponse = SingleLiveEvent<Boolean>()
    override val sessionResponse: LiveData<Boolean>
        get() = _sessionResponse

    private val _viewerDataResponse = MutableLiveData<Resource<Boolean>>()
    override val viewerDataResponse: LiveData<Resource<Boolean>>
        get() = _viewerDataResponse

    private val _viewerData = MutableLiveData<User?>()
    override val viewerData: LiveData<User?>
        get() = _viewerData

    private val _listOrAniListSettingsChanged = SingleLiveEvent<Boolean>()
    override val listOrAniListSettingsChanged: LiveData<Boolean>
        get() = _listOrAniListSettingsChanged

    private val _followersCount = MutableLiveData<Int>()
    override val followersCount: LiveData<Int>
        get() = _followersCount

    private val _followingsCount = MutableLiveData<Int>()
    override val followingsCount: LiveData<Int>
        get() = _followingsCount

    private val _updateAniListSettingsResponse = SingleLiveEvent<Resource<Boolean>>()
    override val updateAniListSettingsResponse: LiveData<Resource<Boolean>>
        get() = _updateAniListSettingsResponse

    private val _updateListSettingsResponse = SingleLiveEvent<Resource<Boolean>>()
    override val updateListSettingsResponse: LiveData<Resource<Boolean>>
        get() = _updateListSettingsResponse

    private val _toggleFavouriteResponse = SingleLiveEvent<Resource<Boolean>>()
    override val toggleFavouriteResponse: LiveData<Resource<Boolean>>
        get() = _toggleFavouriteResponse

    private val _favoriteAnimeResponse = SingleLiveEvent<Resource<FavoritesAnimeQuery.Data>>()
    override val favoriteAnimeResponse: LiveData<Resource<FavoritesAnimeQuery.Data>>
        get() = _favoriteAnimeResponse

    private val _favoriteMangaResponse = SingleLiveEvent<Resource<FavoritesMangaQuery.Data>>()
    override val favoriteMangaResponse: LiveData<Resource<FavoritesMangaQuery.Data>>
        get() = _favoriteMangaResponse

    private val _favoriteCharactersResponse = SingleLiveEvent<Resource<FavoritesCharactersQuery.Data>>()
    override val favoriteCharactersResponse: LiveData<Resource<FavoritesCharactersQuery.Data>>
        get() = _favoriteCharactersResponse

    private val _favoriteStaffsResponse = SingleLiveEvent<Resource<FavoritesStaffsQuery.Data>>()
    override val favoriteStaffsResponse: LiveData<Resource<FavoritesStaffsQuery.Data>>
        get() = _favoriteStaffsResponse

    private val _favoriteStudiosResponse = SingleLiveEvent<Resource<FavoritesStudiosQuery.Data>>()
    override val favoriteStudiosResponse: LiveData<Resource<FavoritesStudiosQuery.Data>>
        get() = _favoriteStudiosResponse

    private val _viewerReviewsResponse = SingleLiveEvent<Resource<UserReviewsQuery.Data>>()
    override val viewerReviewsResponse: LiveData<Resource<UserReviewsQuery.Data>>
        get() = _viewerReviewsResponse

    private val _userStatisticsResponse = SingleLiveEvent<Resource<UserStatisticsQuery.Data>>()
    override val userStatisticsResponse: LiveData<Resource<UserStatisticsQuery.Data>>
        get() = _userStatisticsResponse

    private val _triggerRefreshFavorite = SingleLiveEvent<Boolean>()
    override val triggerRefreshFavorite: LiveData<Boolean>
        get() = _triggerRefreshFavorite

    private val _triggerRefreshReviews = SingleLiveEvent<Boolean>()
    override val triggerRefreshReviews: LiveData<Boolean>
        get() = _triggerRefreshReviews

    private val _reorderFavoritesResponse = SingleLiveEvent<Resource<Boolean>>()
    override val reorderFavoritesResponse: LiveData<Resource<Boolean>>
        get() = _reorderFavoritesResponse

    private val _userFollowersResponse = SingleLiveEvent<Resource<UserFollowersQuery.Data>>()
    override val userFollowersResponse: LiveData<Resource<UserFollowersQuery.Data>>
        get() = _userFollowersResponse

    private val _userFollowingsResponse = SingleLiveEvent<Resource<UserFollowingsQuery.Data>>()
    override val userFollowingsResponse: LiveData<Resource<UserFollowingsQuery.Data>>
        get() = _userFollowingsResponse

    private val _toggleFollowingResponse = SingleLiveEvent<Resource<ToggleFollowMutation.Data>>()
    override val toggleFollowingResponse: LiveData<Resource<ToggleFollowMutation.Data>>
        get() = _toggleFollowingResponse

    private val _toggleFollowerResponse = SingleLiveEvent<Resource<ToggleFollowMutation.Data>>()
    override val toggleFollowerResponse: LiveData<Resource<ToggleFollowMutation.Data>>
        get() = _toggleFollowerResponse

    // For UserFragment
    private val _toggleFollowResponse = SingleLiveEvent<Resource<ToggleFollowMutation.Data>>()
    override val toggleFollowResponse: LiveData<Resource<ToggleFollowMutation.Data>>
        get() = _toggleFollowResponse

    override val viewerDataLastRetrieved: Long?
        get() = userManager.viewerDataLastRetrieved

    override val followersCountLastRetrieved: Long?
        get() = userManager.followersCountLastRetrieved

    override val followingsCountLastRetrieved: Long?
        get() = userManager.followingsCountLastRetrieved

    override val bestFriends: List<BestFriend>?
        get() = userManager.bestFriends

    private val _notificationsResponse = SingleLiveEvent<Resource<NotificationsQuery.Data>>()
    override val notificationsResponse: LiveData<Resource<NotificationsQuery.Data>>
        get() = _notificationsResponse

    private val _notificationCount = MutableLiveData<Int>()
    override val notificationCount: LiveData<Int>
        get() = _notificationCount

    // To notify SocialFragment when best friend data changed
    private val _bestFriendChangedNotifier = SingleLiveEvent<List<BestFriend>>()
    override val bestFriendChangedNotifier: LiveData<List<BestFriend>>
        get() = _bestFriendChangedNotifier

    @SuppressLint("CheckResult")
    override fun checkSession() {
        userDataSource.checkSession().subscribeWith(object : Observer<Response<SessionQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<SessionQuery.Data>) { }

            override fun onError(e: Throwable) {
                if (e is ApolloHttpException) {
                    if (e.rawResponse()?.code == 401 || e.rawResponse()?.code == 400) {
                        _sessionResponse.postValue(false)
                    }
                }
            }

            override fun onComplete() { }
        })
    }

    override fun getViewerData() {
        // used to trigger live data
        _viewerData.postValue(userManager.viewerData)
        _followersCount.postValue(userManager.followersCount)
        _followingsCount.postValue(userManager.followingsCount)
    }

    @SuppressLint("CheckResult")
    override fun retrieveViewerData() {
        _viewerDataResponse.postValue(Resource.Loading())

        userDataSource.getViewerData().subscribeWith(object : Observer<Response<ViewerQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ViewerQuery.Data>) {
                if (t.hasErrors()) {
                    _viewerDataResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    userManager.setViewerData(Converter.convertUser(t.data?.viewer))
                    _viewerDataResponse.postValue(Resource.Success(true))
                    _viewerData.postValue(userManager.viewerData)
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_viewerDataResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun updateAniListSettings(
        titleLanguage: UserTitleLanguage,
        adultContent: Boolean,
        airingNotifications: Boolean
    ) {
        _updateAniListSettingsResponse.postValue(Resource.Loading())

        userDataSource.updateAniListSettings(titleLanguage, adultContent, airingNotifications).subscribeWith(object : Observer<Response<AniListSettingsMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<AniListSettingsMutation.Data>) {
                if (t.hasErrors()) {
                    _updateAniListSettingsResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    val savedUser = userManager.viewerData
                    savedUser?.options = Converter.convertUserOptions(t.data?.updateUser?.options)
                    userManager.setViewerData(savedUser)

                    _updateAniListSettingsResponse.postValue(Resource.Success(true))
                    _viewerData.postValue(userManager.viewerData)
                    _listOrAniListSettingsChanged.postValue(true)
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_updateAniListSettingsResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun updateListSettings(
        scoreFormat: ScoreFormat,
        rowOrder: String,
        animeListOptions: MediaListTypeOptions,
        mangaListOptions: MediaListTypeOptions
    ) {
        _updateListSettingsResponse.postValue(Resource.Loading())

        userDataSource.updateListSettings(scoreFormat, rowOrder, animeListOptions, mangaListOptions).subscribeWith(object : Observer<Response<ListSettingsMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ListSettingsMutation.Data>) {
                if (t.hasErrors()) {
                    _updateListSettingsResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    val savedUser = userManager.viewerData
                    savedUser?.mediaListOptions = Converter.convertMediaListOptions(t.data?.updateUser?.mediaListOptions)
                    userManager.setViewerData(savedUser)

                    _updateListSettingsResponse.postValue(Resource.Success(true))
                    _viewerData.postValue(userManager.viewerData)
                    _listOrAniListSettingsChanged.postValue(true)
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_updateListSettingsResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun updateNotificationsSettings(notificationOptions: List<NotificationOption>) {
        _updateAniListSettingsResponse.postValue(Resource.Loading())

        userDataSource.updateNotificationsSettings(notificationOptions).subscribeWith(object : Observer<Response<AniListSettingsMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<AniListSettingsMutation.Data>) {
                if (t.hasErrors()) {
                    _updateAniListSettingsResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    val savedUser = userManager.viewerData
                    savedUser?.options = Converter.convertUserOptions(t.data?.updateUser?.options)
                    userManager.setViewerData(savedUser)

                    _updateAniListSettingsResponse.postValue(Resource.Success(true))
                    _viewerData.postValue(userManager.viewerData)
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_updateAniListSettingsResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun toggleFavourite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?
    ) {
        _toggleFavouriteResponse.postValue(Resource.Loading())

        userDataSource.toggleFavourite(
            animeId, mangaId, characterId, staffId, studioId
        ).subscribeWith(AndroidUtility.rxApolloCompletable(_toggleFavouriteResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFavoriteAnime(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _favoriteAnimeResponse.postValue(Resource.Loading())
        userDataSource.getFavoriteAnime(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_favoriteAnimeResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFavoriteManga(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _favoriteMangaResponse.postValue(Resource.Loading())
        userDataSource.getFavoriteManga(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_favoriteMangaResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFavoriteCharacters(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _favoriteCharactersResponse.postValue(Resource.Loading())
        userDataSource.getFavoriteCharacters(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_favoriteCharactersResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFavoriteStaffs(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _favoriteStaffsResponse.postValue(Resource.Loading())
        userDataSource.getFavoriteStaffs(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_favoriteStaffsResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFavoriteStudios(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _favoriteStudiosResponse.postValue(Resource.Loading())
        userDataSource.getFavoriteStudios(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_favoriteStudiosResponse))
    }

    override fun triggerRefreshProfilePageChild() {
        _triggerRefreshFavorite.postValue(true)
        _triggerRefreshReviews.postValue(true)
        getStatistics()
    }

    @SuppressLint("CheckResult")
    override fun reorderFavorites(
        animeIds: List<Int>?,
        mangaIds: List<Int>?,
        characterIds: List<Int>?,
        staffIds: List<Int>?,
        studioIds: List<Int>?,
        animeOrder: List<Int>?,
        mangaOrder: List<Int>?,
        characterOrder: List<Int>?,
        staffOrder: List<Int>?,
        studioOrder: List<Int>?
    ) {
        _reorderFavoritesResponse.postValue(Resource.Loading())

        userDataSource.reorderFavorites(animeIds, mangaIds, characterIds, staffIds, studioIds, animeOrder, mangaOrder, characterOrder, staffOrder, studioOrder).subscribeWith(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_reorderFavoritesResponse, e)
            }

            override fun onComplete() {
                _reorderFavoritesResponse.postValue(Resource.Success(true))
                _triggerRefreshFavorite.postValue(true)
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun getReviews(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        _viewerReviewsResponse.postValue(Resource.Loading())
        userDataSource.getReviews(userManager.viewerData?.id!!, page).subscribeWith(AndroidUtility.rxApolloCallback(_viewerReviewsResponse))
    }

    @SuppressLint("CheckResult")
    override fun getStatistics() {
        if (userManager.viewerData?.id == null) {
            return
        }

        _userStatisticsResponse.postValue(Resource.Loading())
        userDataSource.getStatistics(userManager.viewerData?.id!!).subscribeWith(AndroidUtility.rxApolloCallback(_userStatisticsResponse))
    }

    @SuppressLint("CheckResult")
    override fun getFollowersCount() {
        if (userManager.viewerData?.id == null) {
            return
        }

        userDataSource.getFollowers(userManager.viewerData?.id!!, 1).subscribeWith(object : Observer<Response<UserFollowersQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<UserFollowersQuery.Data>) {
                if (!t.hasErrors()) {
                    userManager.setFollowersCount(t.data?.page?.pageInfo?.total ?: 0)
                    _followersCount.postValue(userManager.followersCount)
                }
            }

            override fun onError(e: Throwable) { }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun getFollowingsCount() {
        if (userManager.viewerData?.id == null) {
            return
        }

        userDataSource.getFollowings(userManager.viewerData?.id!!, 1).subscribeWith(object : Observer<Response<UserFollowingsQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<UserFollowingsQuery.Data>) {
                if (!t.hasErrors()) {
                    userManager.setFollowingsCount(t.data?.page?.pageInfo?.total ?: 0)
                    _followingsCount.postValue(userManager.followingsCount)
                }
            }

            override fun onError(e: Throwable) { }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun getUserFollowers(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        userDataSource.getFollowers(userManager.viewerData?.id!!, page).subscribeWith(object : Observer<Response<UserFollowersQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<UserFollowersQuery.Data>) {
                if (!t.hasErrors()) {
                    userManager.setFollowersCount(t.data?.page?.pageInfo?.total ?: 0)
                    _followersCount.postValue(userManager.followersCount)
                    _userFollowersResponse.postValue(Resource.Success(t.data!!))
                } else {
                    _userFollowersResponse.postValue(Resource.Error(t.errors!![0].message))
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_userFollowersResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun getUserFollowings(page: Int) {
        if (userManager.viewerData?.id == null) {
            return
        }

        userDataSource.getFollowings(userManager.viewerData?.id!!, page).subscribeWith(object : Observer<Response<UserFollowingsQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<UserFollowingsQuery.Data>) {
                if (!t.hasErrors()) {
                    userManager.setFollowingsCount(t.data?.page?.pageInfo?.total ?: 0)
                    _followingsCount.postValue(userManager.followingsCount)
                    _userFollowingsResponse.postValue(Resource.Success(t.data!!))
                } else {
                    _userFollowingsResponse.postValue(Resource.Error(t.errors!![0].message))
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_userFollowingsResponse, e)
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun toggleFollow(userId: Int, fromPage: FollowPage) {
        if (fromPage == FollowPage.FOLLOWING) {
            _toggleFollowingResponse.postValue(Resource.Loading())
        } else {
            _toggleFollowerResponse.postValue(Resource.Loading())
        }

        userDataSource.toggleFollow(userId).subscribeWith(object : Observer<Response<ToggleFollowMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ToggleFollowMutation.Data>) {
                if (t.hasErrors()) {
                    if (fromPage == FollowPage.FOLLOWING) {
                        _toggleFollowingResponse.postValue(Resource.Error(t.errors!![0].message))
                    } else {
                        _toggleFollowerResponse.postValue(Resource.Error(t.errors!![0].message))
                    }
                } else {
                    _toggleFollowingResponse.postValue(Resource.Success(t.data!!))
                    _toggleFollowerResponse.postValue(Resource.Success(t.data!!))
                    getFollowingsCount()
                    getFollowersCount()
                }
            }

            override fun onError(e: Throwable) {
                if (fromPage == FollowPage.FOLLOWING) {
                    AndroidUtility.rxApolloHandleError(_toggleFollowingResponse, e)
                } else {
                    AndroidUtility.rxApolloHandleError(_toggleFollowerResponse, e)
                }
            }

            override fun onComplete() { }
        })
    }

    @SuppressLint("CheckResult")
    override fun toggleFollow(userId: Int) {
        _toggleFollowResponse.postValue(Resource.Loading())

        userDataSource.toggleFollow(userId).subscribeWith(object : Observer<Response<ToggleFollowMutation.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ToggleFollowMutation.Data>) {
                if (t.hasErrors()) {
                    _toggleFollowResponse.postValue(Resource.Error(t.errors!![0].message))
                } else {
                    _toggleFollowResponse.postValue(Resource.Success(t.data!!))
                    _toggleFollowingResponse.postValue(Resource.Success(t.data!!))
                    _toggleFollowerResponse.postValue(Resource.Success(t.data!!))
                    getFollowingsCount()
                    getFollowersCount()
                }
            }

            override fun onError(e: Throwable) {
                AndroidUtility.rxApolloHandleError(_toggleFollowResponse, e)
            }

            override fun onComplete() { }
        })
    }

    override fun handleBestFriend(bestFriend: BestFriend, isEdit: Boolean) {
        if (bestFriend.id == null) {
            return
        }

        val savedBestFriends = ArrayList(userManager.bestFriends ?: listOf())
        val findBestFriend = savedBestFriends.find { it.id == bestFriend.id }

        if (findBestFriend == null && isEdit) {
            return
        }

        if (findBestFriend != null) {
            if (isEdit) {
                savedBestFriends[savedBestFriends.indexOf(findBestFriend)] = bestFriend
            } else {
                savedBestFriends.remove(findBestFriend)
            }
        } else {
            savedBestFriends.add(bestFriend)
        }

        userManager.setBestFriends(savedBestFriends)
        _bestFriendChangedNotifier.postValue(userManager.bestFriends)
    }

    @SuppressLint("CheckResult")
    override fun getNotifications(page: Int, typeIn: List<NotificationType>?, reset: Boolean) {
        _notificationsResponse.postValue(Resource.Loading())
        userDataSource.getNotification(page, typeIn, reset).subscribeWith(AndroidUtility.rxApolloCallback(_notificationsResponse))
    }

    @SuppressLint("CheckResult")
    override fun getNotificationCount() {
        userDataSource.getNotificationCount().subscribeWith(object : Observer<Response<ViewerNotificationCountQuery.Data>> {
            override fun onSubscribe(d: Disposable) { }

            override fun onNext(t: Response<ViewerNotificationCountQuery.Data>) {
                if (!t.hasErrors()) {
                    val unreadNotificationCount = t.data?.viewer?.unreadNotificationCount ?: 0

                    val savedUser = userManager.viewerData
                    savedUser?.unreadNotificationCount = unreadNotificationCount
                    userManager.setViewerData(savedUser)

                    _notificationCount.postValue(unreadNotificationCount)
                }
            }

            override fun onError(e: Throwable) { }

            override fun onComplete() { }
        })
    }

    override fun sendFirebaseToken(token: String) {
        userDataSource.sendFirebaseToken(token)
    }

    override fun setLatestNotification(notificationId: Int) {
        if (userManager.latestNotification == null || (userManager.latestNotification ?: 0) < notificationId) {
            userManager.setLatestNotification(notificationId)
        }
    }
}
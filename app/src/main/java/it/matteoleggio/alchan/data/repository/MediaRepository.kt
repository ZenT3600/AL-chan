package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.AnimeDetails
import it.matteoleggio.alchan.data.response.AnimeVideo
import it.matteoleggio.alchan.data.response.MangaDetails
import it.matteoleggio.alchan.data.response.MediaTagCollection
import type.*

interface MediaRepository {
    val genreList: List<String?>
    val genreListLastRetrieved: Long?

    val tagList: List<MediaTagCollection>
    val tagListLastRetrieved: Long?

    val mostTrendingAnimeBannerLivaData: LiveData<String?>

    val mediaData: LiveData<Resource<MediaQuery.Data>>
    val mediaStatus: LiveData<Resource<MediaStatusQuery.Data>>
    val mediaOverviewData: LiveData<Resource<MediaOverviewQuery.Data>>
    val mediaCharactersData: LiveData<Resource<MediaCharactersQuery.Data>>
    val mediaStaffsData: LiveData<Resource<MediaStaffsQuery.Data>>
    val mediaStatsData: LiveData<Resource<MediaStatsQuery.Data>>
    val mediaReviewsData: LiveData<Resource<MediaReviewsQuery.Data>>
    val mediaFriendsMediaListData: LiveData<Resource<MediaSocialQuery.Data>>
    val mediaActivityData: LiveData<Resource<MediaActivityQuery.Data>>

    val trendingAnimeData: LiveData<Resource<TrendingMediaQuery.Data>>
    val trendingMangaData: LiveData<Resource<TrendingMediaQuery.Data>>
    val releasingTodayData: LiveData<Resource<ReleasingTodayQuery.Data>>
    val recentReviewsData: LiveData<Resource<ReviewsQuery.Data>>

    val reviewsData: LiveData<Resource<ReviewsQuery.Data>>
    val reviewDetailData: LiveData<Resource<ReviewDetailQuery.Data>>
    val rateReviewResponse: LiveData<Resource<RateReviewMutation.Data>>

    val checkReviewResponse: LiveData<Resource<CheckReviewQuery.Data>>
    val saveReviewResponse: LiveData<Resource<SaveReviewMutation.Data>>
    val deleteReviewResponse: LiveData<Resource<Boolean>>

    val animeDetailsLiveData: LiveData<Resource<AnimeDetails>>
    val mangaDetailsLiveData: LiveData<Resource<MangaDetails>>
    val animeVideoLiveData: LiveData<Resource<AnimeVideo>>

    val triggerMediaCharacter: LiveData<Boolean>
    val triggerMediaStaff: LiveData<Boolean>
    val triggerMediaReview: LiveData<Boolean>
    val triggerMediaSocial: LiveData<Boolean>

    fun getGenre()
    fun getTag()
    fun getMedia(id: Int)
    fun checkMediaStatus(mediaId: Int)
    fun getMediaOverview(id: Int)
    fun getMediaCharacters(id: Int, page: Int)
    fun getMediaStaffs(id: Int, page: Int)
    fun getMediaStats(id: Int)
    fun getMediaReviews(id: Int, page: Int, sort: List<ReviewSort>)
    fun getMediaFriendsMediaList(mediaId: Int, page: Int)
    fun getMediaActivity(mediaId: Int, page: Int)

    fun getTrendingAnime()
    fun getTrendingManga()
    fun getReleasingToday(page: Int)
    fun getReviews(page: Int, perPage: Int, mediaType: MediaType?, sort: List<ReviewSort>, isRecent: Boolean = false)

    fun getReviewDetail(reviewId: Int)
    fun rateReview(reviewId: Int, rating: ReviewRating)

    fun checkReview(mediaId: Int)
    fun saveReview(id: Int?, mediaId: Int, body: String, summary: String, score: Int, private: Boolean)
    fun deleteReview(id: Int)

    fun getAnimeDetails(malId: Int)
    fun getMangaDetails(malId: Int)
    fun getAnimeVideos(malId: Int)

    fun triggerRefreshMediaChildren()
}
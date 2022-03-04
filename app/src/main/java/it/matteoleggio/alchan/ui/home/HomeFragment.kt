package it.matteoleggio.alchan.ui.home


import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.*
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.Review
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.animelist.editor.AnimeListEditorActivity
import it.matteoleggio.alchan.ui.browse.BrowseActivity
import it.matteoleggio.alchan.ui.browse.media.overview.OverviewGenreRvAdapter
import it.matteoleggio.alchan.ui.calendar.CalendarActivity
import it.matteoleggio.alchan.ui.common.SetProgressDialog
import it.matteoleggio.alchan.ui.explore.ExploreActivity
import it.matteoleggio.alchan.ui.reviews.ReviewsActivity
import it.matteoleggio.alchan.ui.reviews.ReviewsRvAdapter
import it.matteoleggio.alchan.ui.search.SearchActivity
import it.matteoleggio.alchan.ui.seasonal.SeasonalActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaListStatus
import type.MediaType
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private val viewModel by viewModel<HomeViewModel>()

    private lateinit var releasingTodayAdapter: ReleasingTodayRvAdapter
    private lateinit var trendingAnimeAdapter: TrendingMediaRvAdapter
    private lateinit var trendingMangaAdapter: TrendingMediaRvAdapter
    private lateinit var recentReviewsAdapter: ReviewsRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        greetingsLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        releasingTodayAdapter = assignReleasingTodayRvAdapter()
        releasingTodayRecyclerView.adapter = releasingTodayAdapter

        trendingAnimeAdapter = assignTrendingRvAdapter(viewModel.trendingAnimeList, MediaType.ANIME)
        trendingAnimeListRecyclerView.adapter = trendingAnimeAdapter

        trendingMangaAdapter = assignTrendingRvAdapter(viewModel.trendingMangaList, MediaType.MANGA)
        trendingMangaListRecyclerView.adapter = trendingMangaAdapter

        if (viewModel.showRecentReviews) {
            recentReviewsAdapter = assignReviewsRvAdapter()
            recentReviewsRecyclerView.adapter = recentReviewsAdapter
            recentReviewsRecyclerView.visibility = View.VISIBLE
            recentReviewsLabel.visibility = View.VISIBLE
        } else {
            recentReviewsRecyclerView.visibility = View.GONE
            recentReviewsLabel.visibility = View.GONE
        }

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.viewerData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                initLayout()
            }
        })

        viewModel.viewerDataResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> loadingLayout.visibility = View.GONE
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.trendingAnimeData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> trendingAnimeLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    trendingAnimeLoading.visibility = View.GONE
                    if (it.data?.page?.media?.isNullOrEmpty() == false) {
                        initTrendingAnimeLayout(0)
                        viewModel.trendingAnimeList.clear()
                        it.data.page?.media?.forEachIndexed { index, media ->
                            viewModel.trendingAnimeList.add(TrendingMediaItem(media, index == 0))
                        }
                        trendingAnimeListRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                ResponseStatus.ERROR -> {
                    trendingAnimeLoading.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.trendingMangaData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> trendingMangaLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    trendingMangaLoading.visibility = View.GONE
                    if (it.data?.page?.media?.isNullOrEmpty() == false) {
                        initTrendingMangaLayout(0)
                        viewModel.trendingMangaList.clear()
                        it.data.page?.media?.forEachIndexed { index, media ->
                            viewModel.trendingMangaList.add(TrendingMediaItem(media, index == 0))
                        }
                        trendingMangaListRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                ResponseStatus.ERROR -> {
                    trendingMangaLoading.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.releasingTodayData.observe(viewLifecycleOwner, Observer {
            if (it.responseStatus == ResponseStatus.SUCCESS) {
                if (!viewModel.hasNextPage) {
                    return@Observer
                }

                viewModel.hasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                viewModel.page += 1
                viewModel.isInit = true

                it.data?.page?.media?.forEach { media ->
                    if (media?.mediaListEntry?.status != MediaListStatus.DROPPED) {
                        var currentEpisode = 0
                        if (media?.nextAiringEpisode != null) {
                            if (media.nextAiringEpisode.timeUntilAiring < 3600 * 24) {
                                viewModel.releasingTodayList.add(ReleasingTodayItem(media, media.nextAiringEpisode.timeUntilAiring))
                            } else {
                                currentEpisode = media.nextAiringEpisode.episode - 1
                            }
                        }

                        if (media?.airingSchedule != null && media?.airingSchedule.edges?.isNullOrEmpty() == false) {
                            val currentEpisodeSchedule = media.airingSchedule.edges.find { edge -> edge?.node?.episode == currentEpisode }
                            if (currentEpisodeSchedule != null && abs(currentEpisodeSchedule.node?.timeUntilAiring!!) < 3600 * 24) {
                                viewModel.releasingTodayList.add(ReleasingTodayItem(media, currentEpisodeSchedule.node.timeUntilAiring))
                            }
                        }
                    }
                }

                if (viewModel.hasNextPage) {
                    viewModel.getReleasingToday()
                } else {
                    viewModel.releasingTodayList.sortBy { releasingTodayItem ->  releasingTodayItem.timestamp }
                    releasingTodayAdapter = assignReleasingTodayRvAdapter()
                    releasingTodayRecyclerView.adapter = releasingTodayAdapter
                    releasingTodayRecyclerView.visibility = if (viewModel.releasingTodayList.isNullOrEmpty()) View.GONE else View.VISIBLE
                    noNewEpisodeText.visibility = if (viewModel.releasingTodayList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.recentReviewsData.observe(viewLifecycleOwner, Observer {
            recentReviewsLoading.visibility = View.GONE
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    viewModel.recentReviewsList.clear()
                    it.data?.page?.reviews?.forEach { review ->
                        if (review?.user != null && review.media != null) {
                            val user = User(
                                id = review.user.id,
                                name = review.user.name,
                                avatar = UserAvatar(medium = review.user.avatar?.medium, large = null)
                            )
                            val media = Media(
                                id = review.media.id,
                                title = MediaTitle(userPreferred = review.media.title?.userPreferred ?: ""),
                                coverImage = MediaCoverImage(medium = review.media.coverImage?.medium, large = null),
                                bannerImage = review.media.bannerImage
                            )
                            viewModel.recentReviewsList.add(
                                Review(
                                    id = review.id,
                                    userId = review.userId,
                                    mediaId = review.mediaId,
                                    mediaType = review.mediaType,
                                    summary = review.summary,
                                    rating = review.rating,
                                    ratingAmount = review.ratingAmount,
                                    userRating = review.userRating,
                                    score = review.score,
                                    siteUrl = review.siteUrl,
                                    createdAt = review.createdAt,
                                    updatedAt = review.updatedAt,
                                    user = user,
                                    media = media
                                )
                            )
                        }
                    }

                    recentReviewsAdapter.notifyDataSetChanged()

                    if (viewModel.recentReviewsList.isNullOrEmpty()) {
                        recentReviewsEmptyLayout.visibility = View.VISIBLE
                        recentReviewsRecyclerView.visibility = View.GONE
                    } else {
                        recentReviewsEmptyLayout.visibility = View.GONE
                        recentReviewsRecyclerView.visibility = View.VISIBLE
                    }
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)

                    if (viewModel.recentReviewsList.isNullOrEmpty()) {
                        recentReviewsEmptyLayout.visibility = View.VISIBLE
                        recentReviewsRecyclerView.visibility = View.GONE
                    } else {
                        recentReviewsEmptyLayout.visibility = View.GONE
                        recentReviewsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.animeListData.observe(viewLifecycleOwner, Observer {
            if (!viewModel.releasingTodayList.isNullOrEmpty()) {
                loadingLayout.visibility = View.GONE
                viewModel.page = 1
                viewModel.hasNextPage = true
                viewModel.releasingTodayList.clear()
                viewModel.getReleasingToday()
            }
        })

        viewModel.initData()
        if (!viewModel.isInit) {
            viewModel.getReleasingToday()
        } else {
            releasingTodayAdapter = assignReleasingTodayRvAdapter()
            releasingTodayRecyclerView.adapter = releasingTodayAdapter
            releasingTodayRecyclerView.visibility = if (viewModel.releasingTodayList.isNullOrEmpty()) View.GONE else View.VISIBLE
            noNewEpisodeText.visibility = if (viewModel.releasingTodayList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initLayout() {
        homeRefreshLayout.setOnRefreshListener {
            homeRefreshLayout.isRefreshing = false
            viewModel.initData()

            viewModel.page = 1
            viewModel.hasNextPage = true
            viewModel.releasingTodayList.clear()
            viewModel.getReleasingToday()

            viewModel.getNotificationCount()
        }

        val user = viewModel.viewerData.value

        GlideApp.with(this).load(user?.bannerImage).into(headerImage)

        searchBar.setOnClickListener { startActivity(Intent(activity, SearchActivity::class.java)) }

        greetingsText.text = "Hello, ${user?.name ?: ""}."
        
        if (viewModel.circularAvatar) {
            userAvatar.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_oval_transparent)
            if (viewModel.whiteBackgroundAvatar) {
                userAvatar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            } else {
                userAvatar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
            }
            GlideApp.with(this).load(user?.avatar?.large).apply(RequestOptions.circleCropTransform()).into(userAvatar)
        } else {
            userAvatar.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_rectangle_transparent)
            userAvatar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
            GlideApp.with(this).load(user?.avatar?.large).into(userAvatar)
        }

        exploreMenu.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.explorePageArray) { _, which ->
                    val intent = Intent(activity, ExploreActivity::class.java)
                    intent.putExtra(ExploreActivity.EXPLORE_PAGE, viewModel.explorePageArray[which])
                    startActivity(intent)
                }
                .show()
        }

        seasonalChartMenu.setOnClickListener {
            startActivity(Intent(activity, SeasonalActivity::class.java))
        }

        reviewsMenu.setOnClickListener {
            startActivity(Intent(activity, ReviewsActivity::class.java))
        }

        calendarMenu.setOnClickListener {
            startActivity(Intent(activity, CalendarActivity::class.java))
        }
    }

    private fun assignTrendingRvAdapter(list: List<TrendingMediaItem>, mediaType: MediaType): TrendingMediaRvAdapter {
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        val width = metrics.widthPixels / 4

        return TrendingMediaRvAdapter(requireActivity(), list, mediaType, width, object : TrendingMediaRvAdapter.TrendingMediaListener {
            override fun passSelectedMedia(position: Int, mediaType: MediaType) {
                if (mediaType == MediaType.ANIME) {
                    initTrendingAnimeLayout(position)
                    viewModel.trendingAnimeList.forEachIndexed { index, it ->
                        it.isSelected = index == position
                    }
                    trendingAnimeListRecyclerView.adapter?.notifyDataSetChanged()
                } else if (mediaType == MediaType.MANGA) {
                    initTrendingMangaLayout(position)
                    viewModel.trendingMangaList.forEachIndexed { index, it ->
                        it.isSelected = index == position
                    }
                    trendingMangaListRecyclerView.adapter?.notifyDataSetChanged()
                }
            }
        })
    }

    private fun initTrendingAnimeLayout(highlightIndex: Int) {
        trendingAnimeCard.visibility = View.VISIBLE
        trendingAnimeListRecyclerView.visibility = View.VISIBLE

        val highlightAnime = viewModel.trendingAnimeData.value?.data?.page?.media!![highlightIndex]
        GlideApp.with(this).load(highlightAnime?.bannerImage).into(trendingAnimeBanner)
        GlideApp.with(this).load(highlightAnime?.coverImage?.large).into(trendingAnimeCoverImage)
        trendingAnimeTitleText.text = highlightAnime?.title?.userPreferred
        if (highlightAnime?.studios?.edges?.isNullOrEmpty() == false) {
            trendingAnimeCreatorText.text = highlightAnime.studios.edges[0]?.node?.name
        } else {
            trendingAnimeCreatorText.text = ""
        }
        trendingAnimeScore.text = highlightAnime?.averageScore?.toString() ?: "0"
        trendingAnimeFavorite.text = highlightAnime?.favourites?.toString() ?: "0"
        trendingAnimeDescriptionText.text = HtmlCompat.fromHtml(highlightAnime?.description ?: getString(R.string.no_description), HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (!highlightAnime?.genres.isNullOrEmpty()) {
            trendingAnimeGenreRecyclerView.visibility = View.VISIBLE
            trendingAnimeGenreRecyclerView.adapter = OverviewGenreRvAdapter(highlightAnime?.genres!!, object : OverviewGenreRvAdapter.OverviewGenreListener {
                override fun passSelectedGenre(genre: String) { }
            })
        } else {
            trendingAnimeGenreRecyclerView.visibility = View.GONE
        }

        trendingAnimeCard.setOnClickListener {
            val intent = Intent(activity, BrowseActivity::class.java)
            intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.ANIME.name)
            intent.putExtra(BrowseActivity.LOAD_ID, highlightAnime?.id)
            startActivity(intent)
        }
    }

    private fun initTrendingMangaLayout(highlightIndex: Int) {
        trendingMangaCard.visibility = View.VISIBLE
        trendingMangaListRecyclerView.visibility = View.VISIBLE

        val highlightManga = viewModel.trendingMangaData.value?.data?.page?.media!![highlightIndex]
        GlideApp.with(this).load(highlightManga?.bannerImage).into(trendingMangaBanner)
        GlideApp.with(this).load(highlightManga?.coverImage?.large).into(trendingMangaCoverImage)
        trendingMangaTitleText.text = highlightManga?.title?.userPreferred
        var creatorList = ""
        highlightManga?.staff?.edges?.forEachIndexed { index, it ->
            creatorList += it?.node?.name?.full
            if (index != highlightManga.staff.edges.lastIndex) creatorList += ", "
        }
        trendingMangaCreatorText.text = creatorList
        trendingMangaScore.text = highlightManga?.averageScore?.toString() ?: "0"
        trendingMangaFavorite.text = highlightManga?.favourites?.toString() ?: "0"
        trendingMangaDescriptionText.text = HtmlCompat.fromHtml(highlightManga?.description ?: getString(R.string.no_description), HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (!highlightManga?.genres.isNullOrEmpty()) {
            trendingMangaGenreRecyclerView.visibility = View.VISIBLE
            trendingMangaGenreRecyclerView.adapter = OverviewGenreRvAdapter(highlightManga?.genres!!, object : OverviewGenreRvAdapter.OverviewGenreListener {
                override fun passSelectedGenre(genre: String) { }
            })
        } else {
            trendingMangaGenreRecyclerView.visibility = View.GONE
        }

        trendingMangaCard.setOnClickListener {
            val intent = Intent(activity, BrowseActivity::class.java)
            intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.MANGA.name)
            intent.putExtra(BrowseActivity.LOAD_ID, highlightManga?.id)
            startActivity(intent)
        }
    }

    private fun assignReleasingTodayRvAdapter(): ReleasingTodayRvAdapter {
        return ReleasingTodayRvAdapter(requireActivity(), viewModel.releasingTodayList, object : ReleasingTodayRvAdapter.ReleasingTodayListener {
            override fun openBrowsePage(id: Int) {
                val intent = Intent(activity, BrowseActivity::class.java)
                intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.ANIME.name)
                intent.putExtra(BrowseActivity.LOAD_ID, id)
                startActivity(intent)
            }

            override fun openEditor(mediaListId: Int) {
                val intent = Intent(activity, AnimeListEditorActivity::class.java)
                intent.putExtra(AnimeListEditorActivity.INTENT_ENTRY_ID, mediaListId)
                startActivity(intent)
            }

            override fun openProgressDialog(
                mediaList: ReleasingTodayQuery.MediaListEntry,
                episodeTotal: Int?
            ) {
                val setProgressDialog = SetProgressDialog()
                val bundle = Bundle()
                bundle.putInt(SetProgressDialog.BUNDLE_CURRENT_PROGRESS, mediaList.progress ?: 0)
                if (episodeTotal != null) {
                    bundle.putInt(SetProgressDialog.BUNDLE_TOTAL_EPISODES, episodeTotal)
                }
                setProgressDialog.arguments = bundle
                setProgressDialog.setListener(object : SetProgressDialog.SetProgressListener {
                    override fun passProgress(newProgress: Int) {
                        handleUpdateProgressBehavior(mediaList, episodeTotal, newProgress)
                    }
                })
                setProgressDialog.show(childFragmentManager, null)
            }

            override fun incrementProgress(
                mediaList: ReleasingTodayQuery.MediaListEntry,
                episodeTotal: Int?
            ) {
                handleUpdateProgressBehavior(mediaList, episodeTotal, mediaList.progress?.plus(1) ?: 1)
            }
        })
    }

    private fun handleUpdateProgressBehavior(mediaList: ReleasingTodayQuery.MediaListEntry, episodeTotal: Int?, newProgress: Int) {
        if (mediaList.progress == newProgress) {
            return
        }

        var status = mediaList.status
        var repeat = mediaList.repeat

        if (newProgress == episodeTotal) {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.move_to_completed,
                R.string.do_you_want_to_set_this_entry_into_completed,
                R.string.move,
                {
                    if (status == MediaListStatus.REPEATING) {
                        repeat = repeat!! + 1
                    }
                    status = MediaListStatus.COMPLETED
                    loadingLayout.visibility = View.VISIBLE
                    viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                },
                R.string.stay,
                {
                    loadingLayout.visibility = View.VISIBLE
                    viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                }
            )
        } else {
            when (mediaList.status) {
                MediaListStatus.PLANNING, MediaListStatus.PAUSED, MediaListStatus.DROPPED -> {
                    DialogUtility.showOptionDialog(
                        requireActivity(),
                        R.string.move_to_watching,
                        R.string.do_you_want_to_set_this_entry_into_watching,
                        R.string.move,
                        {
                            status = MediaListStatus.CURRENT
                            loadingLayout.visibility = View.VISIBLE
                            viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                        },
                        R.string.stay,
                        {
                            loadingLayout.visibility = View.VISIBLE
                            viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                        }
                    )
                }
                MediaListStatus.COMPLETED -> {
                    status = MediaListStatus.CURRENT
                    loadingLayout.visibility = View.VISIBLE
                    viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                }
                else -> {
                    loadingLayout.visibility = View.VISIBLE
                    viewModel.updateAnimeProgress(mediaList.id, status!!, repeat!!, newProgress)
                }
            }
        }
    }

    private fun assignReviewsRvAdapter(): ReviewsRvAdapter {
        return ReviewsRvAdapter(requireActivity(), viewModel.recentReviewsList, object : ReviewsRvAdapter.ReviewsListener {
            override fun openReview(id: Int) {
                val intent = Intent(activity, BrowseActivity::class.java)
                intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.REVIEW.name)
                intent.putExtra(BrowseActivity.LOAD_ID, id)
                startActivity(intent)
            }

            override fun openUser(userId: Int) {
                val intent = Intent(activity, BrowseActivity::class.java)
                intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.USER.name)
                intent.putExtra(BrowseActivity.LOAD_ID, userId)
                startActivity(intent)
            }
        })
    }

    class ReleasingTodayItem(val releasingToday: ReleasingTodayQuery.Medium?, val timestamp: Int)

    class TrendingMediaItem(val trendingMedia: TrendingMediaQuery.Medium?, var isSelected: Boolean)
}

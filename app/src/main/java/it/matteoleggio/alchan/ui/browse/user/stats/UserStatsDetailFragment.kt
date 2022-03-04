package it.matteoleggio.alchan.ui.browse.user.stats


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.CountryCode
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.enums.StatsCategory
import it.matteoleggio.alchan.helper.pojo.UserStatsData
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.common.ChartDialog
import it.matteoleggio.alchan.ui.profile.stats.details.StatsDetailRvAdapter
import kotlinx.android.synthetic.main.fragment_user_stats_detail.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import type.UserStatisticsSort
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.round

class UserStatsDetailFragment : BaseFragment() {

    private val viewModel by viewModel<UserStatsDetailViewModel>()

    private val pieColorList = Constant.PIE_CHART_COLOR_LIST

    companion object {
        const val USER_ID = "userId"
        const val USERNAME = "username"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_stats_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        statsRecyclerView.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        viewModel.otherUserId = arguments?.getInt(USER_ID)

        toolbarLayout.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarLayout.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_arrow_back)

        if (viewModel.selectedCategory == null) {
            viewModel.selectedCategory = StatsCategory.FORMAT
            viewModel.selectedMedia = MediaType.ANIME
            viewModel.selectedStatsSort = UserStatisticsSort.COUNT_DESC
        }

        initLayout()
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.formatStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.formats?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.format?.name?.replaceUnderscore()
                            )
                            )
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.formats?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.format?.name?.replaceUnderscore()
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.statusStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.statuses?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = if (Constant.STATUS_COLOR_MAP.containsKey(item?.status)) Constant.STATUS_COLOR_MAP[item?.status] else null,
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.status?.name.replaceUnderscore()
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.statuses?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = if (Constant.STATUS_COLOR_MAP.containsKey(item?.status)) Constant.STATUS_COLOR_MAP[item?.status] else null,
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.status?.name.replaceUnderscore()
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.scoreStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.scores?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = if (Constant.SCORE_COLOR_MAP.containsKey(item?.meanScore?.toInt())) Constant.SCORE_COLOR_MAP[item?.score?.toInt()] else null,
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.meanScore?.roundToOneDecimal()
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.scores?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = if (Constant.SCORE_COLOR_MAP.containsKey(item?.score)) Constant.SCORE_COLOR_MAP[item?.score] else null,
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.meanScore?.roundToOneDecimal()
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.lengthStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.lengths?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.length
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.lengths?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.length
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.releaseYearStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.releaseYears?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.releaseYear?.toString()
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.releaseYears?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.releaseYear?.toString()
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.startYearStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.startYears?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.startYear?.toString()
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.startYears?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.startYear?.toString()
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.genreStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.genres?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.genre
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.genres?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.genre
                            ))
                        }
                    }
                    viewModel.currentMediaList = ArrayList()
                    viewModel.searchMediaImage()
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.tagStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.tags?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = item?.tag?.name
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.tags?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = item?.tag?.name
                            ))
                        }
                    }
                    viewModel.currentMediaList = ArrayList()
                    viewModel.searchMediaImage()
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.countryStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.countries?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                label = CountryCode.valueOf(item?.country!!).value
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.countries?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = pieColorList[viewModel.currentStats?.size!!],
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                label = CountryCode.valueOf(item?.country!!).value
                            ))
                        }
                    }
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.voiceActorStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    it.data?.user?.statistics?.anime?.voiceActors?.forEach { item ->
                        viewModel.currentStats?.add(UserStatsData(
                            color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                            count = item?.count,
                            meanScore = item?.meanScore,
                            minutesWatched = item?.minutesWatched,
                            mediaIds = item?.mediaIds,
                            characterIds = item?.characterIds,
                            id = item?.voiceActor?.id,
                            label = item?.voiceActor?.name?.full
                        ))
                    }
                    viewModel.currentMediaList = ArrayList()
                    viewModel.currentCharacterList = ArrayList()

                    // excuse the magic number
                    if (viewModel.selectedImage == 1) {
                        viewModel.searchCharacterImage()
                    } else {
                        viewModel.searchMediaImage()
                    }

                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.staffStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    if (viewModel.selectedMedia == MediaType.ANIME) {
                        it.data?.user?.statistics?.anime?.staff?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                minutesWatched = item?.minutesWatched,
                                mediaIds = item?.mediaIds,
                                id = item?.staff?.id,
                                label = item?.staff?.name?.full
                            ))
                        }
                    } else {
                        it.data?.user?.statistics?.manga?.staff?.forEach { item ->
                            viewModel.currentStats?.add(UserStatsData(
                                color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                                count = item?.count,
                                meanScore = item?.meanScore,
                                chaptersRead = item?.chaptersRead,
                                mediaIds = item?.mediaIds,
                                id = item?.staff?.id,
                                label = item?.staff?.name?.full
                            ))
                        }
                    }
                    viewModel.currentMediaList = ArrayList()
                    viewModel.searchMediaImage()
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.studioStatisticResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.username = it.data?.user?.name
                    viewModel.currentStats = ArrayList()

                    it.data?.user?.statistics?.anime?.studios?.forEach { item ->
                        viewModel.currentStats?.add(UserStatsData(
                            color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor),
                            count = item?.count,
                            meanScore = item?.meanScore,
                            minutesWatched = item?.minutesWatched,
                            mediaIds = item?.mediaIds,
                            id = item?.studio?.id,
                            label = item?.studio?.name
                        ))
                    }
                    viewModel.currentMediaList = ArrayList()
                    viewModel.searchMediaImage()
                    setupStatistic()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.searchMediaImageResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    val pageInfo = it.data?.page?.pageInfo
                    viewModel.currentMediaList?.addAll(ArrayList(it.data?.page?.media ?: listOf()))
                    if (pageInfo?.hasNextPage == true) {
                        viewModel.searchMediaImage(pageInfo.currentPage!! + 1)
                    } else {
                        loadingLayout.visibility = View.GONE
                        statsRecyclerView.adapter = assignAdapter()
                    }
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    loadingLayout.visibility = View.GONE
                }
            }
        })

        viewModel.searchCharacterImageResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    val pageInfo = it.data?.page?.pageInfo
                    viewModel.currentCharacterList?.addAll(ArrayList(it.data?.page?.characters ?: listOf()))
                    if (pageInfo?.hasNextPage == true) {
                        viewModel.searchCharacterImage(pageInfo.currentPage!! + 1)
                    } else {
                        loadingLayout.visibility = View.GONE
                        statsRecyclerView.adapter = assignAdapter()
                    }
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    loadingLayout.visibility = View.GONE
                }
            }
        })

        if (viewModel.currentStats == null) {
            viewModel.getStatisticData()
        } else {
            setupStatistic()
        }
    }

    private fun initLayout() {
        statsDetailsRefreshLayout.setOnRefreshListener {
            statsDetailsRefreshLayout.isRefreshing = false
            viewModel.getStatisticData()
        }

        statsCategoryText.text = viewModel.selectedCategory?.name.replaceUnderscore()
        statsMediaText.text = viewModel.selectedMedia?.name
        statsSortText.text = viewModel.getSortString()
        statsImageText.text = viewModel.imageDataList[viewModel.selectedImage]

        statsPieChart.visibility = View.GONE
        statsBarChart.visibility = View.GONE
        statsLineChart.visibility = View.GONE
        detailStatsShowButton.visibility = View.GONE

        when (viewModel.selectedCategory) {
            StatsCategory.VOICE_ACTOR -> {
                statsMediaLayout.visibility = View.GONE
                statsSortLayout.visibility = View.VISIBLE
                statsImageLayout.visibility = View.VISIBLE
            }
            StatsCategory.STUDIO -> {
                statsMediaLayout.visibility = View.GONE
                statsSortLayout.visibility = View.VISIBLE
                statsImageLayout.visibility = View.GONE
            }
            StatsCategory.SCORE, StatsCategory.LENGTH, StatsCategory.RELEASE_YEAR, StatsCategory.START_YEAR -> {
                statsMediaLayout.visibility = View.VISIBLE
                statsSortLayout.visibility = View.GONE
                statsImageLayout.visibility = View.GONE
            }
            else -> {
                statsMediaLayout.visibility = View.VISIBLE
                statsSortLayout.visibility = View.VISIBLE
                statsImageLayout.visibility = View.GONE
            }
        }

        statsCategoryText.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.getStatsCategoryArray()) { _, which ->
                    viewModel.selectedCategory = StatsCategory.values()[which]
                    if (viewModel.selectedCategory == StatsCategory.VOICE_ACTOR || viewModel.selectedCategory == StatsCategory.STUDIO) {
                        viewModel.selectedMedia = MediaType.ANIME
                    }
                    initLayout()
                    viewModel.getStatisticData()
                }
                .show()
        }

        statsMediaText.setOnClickListener {
            val mediaTypeArray = viewModel.getMediaTypeArray()
            AlertDialog.Builder(requireActivity())
                .setItems(mediaTypeArray) { _, which ->
                    viewModel.selectedMedia = MediaType.valueOf(mediaTypeArray[which])
                    initLayout()
                    viewModel.getStatisticData()
                }
                .show()
        }

        statsSortText.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.getSortDataArray()) { _, which ->
                    viewModel.selectedStatsSort = viewModel.sortDataList[which]
                    initLayout()
                    viewModel.getStatisticData()
                }
                .show()
        }

        statsImageText.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.imageDataList.toTypedArray()) { _, which ->
                    viewModel.selectedImage = which
                    initLayout()
                    viewModel.getStatisticData()
                }
                .show()
        }
    }

    private fun setupStatistic() {
        toolbarLayout.title = "${viewModel.username} ${getString(R.string.detailed_statistics)}"

        when (viewModel.selectedCategory) {
            StatsCategory.FORMAT -> handleFormatLayout()
            StatsCategory.STATUS -> handleStatusLayout()
            StatsCategory.SCORE -> handleScoreLayout()
            StatsCategory.LENGTH -> handleLengthLayout()
            StatsCategory.RELEASE_YEAR -> handleReleaseYearLayout()
            StatsCategory.START_YEAR -> handleStartYearLayout()
            StatsCategory.GENRE -> handleGenreLayout()
            StatsCategory.TAG -> handleTagLayout()
            StatsCategory.COUNTRY -> handleCountryLayout()
            StatsCategory.VOICE_ACTOR -> handleVoiceActorLayout()
            StatsCategory.STAFF -> handleStaffLayout()
            StatsCategory.STUDIO -> handleStudioLayout()
        }
    }

    private fun assignAdapter(): StatsDetailRvAdapter {
        return StatsDetailRvAdapter(requireActivity(),
            viewModel.currentStats ?: ArrayList(),
            viewModel.currentMediaList,
            viewModel.currentCharacterList,
            viewModel.selectedCategory!!,
            viewModel.selectedMedia!!,
            viewModel.selectedImage == 1,
            object : StatsDetailRvAdapter.StatsDetailListener {
                override fun passSelectedData(id: Int, browsePage: BrowsePage) {
                    listener?.changeFragment(browsePage, id)
                }
            }
        )
    }

    private fun setupPieChart(pieDataSet: PieDataSet) {
        statsPieChart.visibility = View.VISIBLE
        statsBarChart.visibility = View.GONE
        statsLineChart.visibility = View.GONE
        detailStatsShowButton.visibility = View.GONE

        statsPieChart.clear()
        statsPieChart.invalidate()

        if (!viewModel.showStatsAutomatically) {
            statsPieChart.visibility = View.GONE
            detailStatsShowButton.visibility = View.VISIBLE

            detailStatsShowButton.setOnClickListener {
                val dialog = ChartDialog()
                val bundle = Bundle()
                bundle.putString(ChartDialog.PIE_ENTRIES, viewModel.gson.toJson(pieDataSet))
                dialog.arguments = bundle
                dialog.show(childFragmentManager, null)
            }
        } else {
            try {
                val pieData = PieData(pieDataSet)
                pieData.setDrawValues(false)

                statsPieChart.apply {
                    setHoleColor(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
                    setDrawEntryLabels(false)
                    setTouchEnabled(false)
                    description.isEnabled = false
                    legend.isEnabled = false
                    data = pieData
                    invalidate()
                }
            } catch (e: Exception) {
                DialogUtility.showToast(activity, e.localizedMessage)
            }
        }
    }

    private fun setupBarChart(barDataSet: BarDataSet, xAxisLabel: List<String>? = null) {
        statsPieChart.visibility = View.GONE
        statsBarChart.visibility = View.VISIBLE
        statsLineChart.visibility = View.GONE
        detailStatsShowButton.visibility = View.GONE

        statsBarChart.data?.clearValues()
        statsBarChart.xAxis.valueFormatter = null
        statsBarChart.notifyDataSetChanged()
        statsBarChart.clear()
        statsBarChart.invalidate()

        if (!viewModel.showStatsAutomatically) {
            statsBarChart.visibility = View.GONE
            detailStatsShowButton.visibility = View.VISIBLE

            detailStatsShowButton.setOnClickListener {
                val dialog = ChartDialog()
                val bundle = Bundle()
                bundle.putString(ChartDialog.BAR_ENTRIES, viewModel.gson.toJson(barDataSet))
                if (!xAxisLabel.isNullOrEmpty()) bundle.putStringArrayList(ChartDialog.XAXIS_LABEL, ArrayList(xAxisLabel))
                dialog.arguments = bundle
                dialog.show(childFragmentManager, null)
            }
        } else {
            try {
                val newValueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }

                val barData = BarData(barDataSet)
                barData.setValueTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
                barData.barWidth = 3F
                barData.setValueFormatter(newValueFormatter)

                statsBarChart.axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                }

                statsBarChart.axisRight.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                }

                statsBarChart.xAxis.apply {
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM
                    setLabelCount(barDataSet.entryCount, true)
                    textColor = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor)

                    if (!xAxisLabel.isNullOrEmpty()) {
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = round(value / 10.0) - 1
                                if (index < xAxisLabel.size) {
                                    return xAxisLabel[index.toInt()]
                                }
                                return ""
                            }
                        }
                    }
                }

                statsBarChart.apply {
                    setTouchEnabled(false)
                    description.isEnabled = false
                    legend.isEnabled = false
                    data = barData
                    invalidate()
                }
            } catch (e: Exception) {
                DialogUtility.showToast(activity, e.localizedMessage)
            }
        }
    }

    private fun setupLineChart(lineDataSet: LineDataSet, lineEntries: List<Entry>) {
        statsPieChart.visibility = View.GONE
        statsBarChart.visibility = View.GONE
        statsLineChart.visibility = View.VISIBLE
        detailStatsShowButton.visibility = View.GONE

        statsLineChart.data?.clearValues()
        statsLineChart.notifyDataSetChanged()
        statsLineChart.clear()
        statsLineChart.invalidate()

        if (!viewModel.showStatsAutomatically) {
            statsLineChart.visibility = View.GONE
            detailStatsShowButton.visibility = View.VISIBLE

            detailStatsShowButton.setOnClickListener {
                val dialog = ChartDialog()
                val bundle = Bundle()
                bundle.putString(ChartDialog.LINE_ENTRIES, viewModel.gson.toJson(lineEntries))
                dialog.arguments = bundle
                dialog.show(childFragmentManager, null)
            }
        } else {
            try {
                val newValueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }

                val lineData = LineData(lineDataSet)
                lineData.setValueTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
                lineData.setValueFormatter(newValueFormatter)

                statsLineChart.axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                }

                statsLineChart.axisRight.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                }

                statsLineChart.xAxis.apply {
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1F
                    textColor = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor)

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                }

                statsLineChart.apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    data = lineData
                    invalidate()
                }
            } catch (e: Exception) {
                DialogUtility.showToast(activity, e.localizedMessage)
            }
        }
    }

    private fun handleFormatLayout() {
        if (viewModel.selectedStatsSort == UserStatisticsSort.MEAN_SCORE_DESC) {
            statsRecyclerView.adapter = assignAdapter()
            return
        }

        val pieEntries = ArrayList<PieEntry>()

        viewModel.currentStats?.forEach {
            val progress = if (viewModel.selectedStatsSort == UserStatisticsSort.COUNT_DESC) {
                it.count?.toFloat()
            } else if (viewModel.selectedMedia == MediaType.ANIME) {
                it.minutesWatched?.toFloat()
            } else {
                it.chaptersRead?.toFloat()
            }
            pieEntries.add(PieEntry(progress!!, it.label))
        }

        val pieDataSet = PieDataSet(pieEntries, "Format Distribution")
        pieDataSet.colors = pieColorList

        setupPieChart(pieDataSet)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleStatusLayout() {
        if (viewModel.selectedStatsSort == UserStatisticsSort.MEAN_SCORE_DESC) {
            statsRecyclerView.adapter = assignAdapter()
            return
        }

        val pieEntries = ArrayList<PieEntry>()
        val sortedColorList = ArrayList<Int>()

        viewModel.currentStats?.forEach {
            val progress = if (viewModel.selectedStatsSort == UserStatisticsSort.COUNT_DESC) {
                it.count?.toFloat()
            } else if (viewModel.selectedMedia == MediaType.ANIME) {
                it.minutesWatched?.toFloat()
            } else {
                it.chaptersRead?.toFloat()
            }
            pieEntries.add(PieEntry(progress!!, it.label))
            sortedColorList.add(it.color ?: AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
        }

        val pieDataSet = PieDataSet(pieEntries, "Status Distribution")
        pieDataSet.colors = sortedColorList

        setupPieChart(pieDataSet)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleScoreLayout() {
        val barEntries = ArrayList<BarEntry>()
        val scoreList = arrayListOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        val sortedStats = ArrayList<UserStatsData>()

        val userScoreList = HashMap<Int, ArrayList<UserStatsData>>()
        viewModel.currentStats?.forEach {
            val score = (round(it.meanScore!! / 10.0) * 10).toInt()
            if (userScoreList.containsKey(score)) {
                userScoreList[score]!!.add(it)
            } else {
                userScoreList[score] = arrayListOf(it)
            }
        }

        scoreList.forEach { score ->
            if (userScoreList.containsKey(score)) {
                var totalCount = 0
                var totalMinutes = 0
                var totalChapters = 0
                userScoreList[score]!!.forEach { data ->
                    totalCount += data.count ?: 0
                    totalMinutes += data.minutesWatched ?: 0
                    totalChapters += data.chaptersRead ?: 0
                }

                barEntries.add(BarEntry(score.toFloat(), totalCount.toFloat()))
                sortedStats.add(
                    UserStatsData(
                        color = Constant.SCORE_COLOR_MAP[score],
                        count = totalCount,
                        meanScore = score.toDouble(),
                        minutesWatched = totalMinutes,
                        chaptersRead = totalChapters,
                        label = score.toString()
                    )
                )
            } else {
                barEntries.add(BarEntry(score.toFloat(), 0F))
                sortedStats.add(
                    UserStatsData(
                        color = Constant.SCORE_COLOR_MAP[score],
                        count = 0,
                        meanScore = score.toDouble(),
                        minutesWatched = 0,
                        chaptersRead = 0,
                        label = score.toString()
                    )
                )
            }
        }

        viewModel.currentStats = sortedStats

        val barDataSet = BarDataSet(barEntries, "Score Distribution")
        barDataSet.colors = Constant.SCORE_COLOR_LIST

        setupBarChart(barDataSet)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleLengthLayout() {
        val barEntries = ArrayList<BarEntry>()
        val lengthList = if (viewModel.selectedMedia == MediaType.ANIME) {
            arrayListOf("1", "2-6", "7-16", "17-28", "29-55", "56-100", "101+", "Unknown")
        } else {
            arrayListOf("1", "2-10", "11-25", "26-50", "51-100", "101-200", "201+", "Unknown")
        }
        val sortedStats = ArrayList<UserStatsData>()

        lengthList.forEachIndexed { index, length ->
            val lengthDetail = if (index != lengthList.lastIndex) {
                viewModel.currentStats?.find { it.label == length }
            } else {
                viewModel.currentStats?.find { it.label == null }
            }
            if (index == lengthList.lastIndex) {
                lengthDetail?.label = length
            }
            if (lengthDetail != null) {
                lengthDetail.color = Constant.SCORE_COLOR_LIST[index]
                barEntries.add(BarEntry(index.toFloat() * 10 + 10, lengthDetail.count?.toFloat()!!))
                sortedStats.add(lengthDetail)
            } else {
                barEntries.add(BarEntry(index.toFloat() * 10 + 10, 0F))
                sortedStats.add(UserStatsData(
                    color = Constant.PIE_CHART_COLOR_LIST[index],
                    count = 0,
                    meanScore = 0.0,
                    minutesWatched = 0,
                    chaptersRead = 0,
                    label = length)
                )
            }
        }

        viewModel.currentStats = sortedStats

        val barDataSet = BarDataSet(barEntries, "Length Distribution")
        barDataSet.colors = Constant.SCORE_COLOR_LIST

        setupBarChart(barDataSet, lengthList)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleReleaseYearLayout() {
        val lineEntries = ArrayList<Entry>()
        val sortedStats = viewModel.currentStats?.sortedBy { it.label }

        sortedStats?.forEach {
            lineEntries.add(Entry(it.label?.toFloat()!!, it.count?.toFloat()!!))
        }

        viewModel.currentStats = ArrayList(sortedStats ?: listOf())

        val lineDataSet = LineDataSet(lineEntries, "Release Year Distribution")
        lineDataSet.color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor)
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.line_chart_fill)

        setupLineChart(lineDataSet, lineEntries)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleStartYearLayout() {
        val lineEntries = ArrayList<Entry>()
        val sortedStats = viewModel.currentStats?.sortedBy { it.label }

        sortedStats?.forEach {
            lineEntries.add(Entry(it.label?.toFloat()!!, it.count?.toFloat()!!))
        }

        viewModel.currentStats = ArrayList(sortedStats ?: listOf())

        val lineDataSet = LineDataSet(lineEntries, "Start Year Distribution")
        lineDataSet.color = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor)
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.line_chart_fill)

        setupLineChart(lineDataSet, lineEntries)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleGenreLayout() {
        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleTagLayout() {
        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleCountryLayout() {
        if (viewModel.selectedStatsSort == UserStatisticsSort.MEAN_SCORE_DESC) {
            statsRecyclerView.adapter = assignAdapter()
            return
        }

        val pieEntries = ArrayList<PieEntry>()

        viewModel.currentStats?.forEach {
            val progress = if (viewModel.selectedStatsSort == UserStatisticsSort.COUNT_DESC) {
                it.count?.toFloat()
            } else if (viewModel.selectedMedia == MediaType.ANIME) {
                it.minutesWatched?.toFloat()
            } else {
                it.chaptersRead?.toFloat()
            }
            pieEntries.add(PieEntry(progress!!, it.label))
        }

        val pieDataSet = PieDataSet(pieEntries, "Country Distribution")
        pieDataSet.colors = pieColorList

        setupPieChart(pieDataSet)

        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleVoiceActorLayout() {
        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleStaffLayout() {
        statsRecyclerView.adapter = assignAdapter()
    }

    private fun handleStudioLayout() {
        statsRecyclerView.adapter = assignAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statsRecyclerView.adapter = null
    }
}

package it.matteoleggio.alchan.ui.browse.media.stats


import MediaOverviewQuery
import MediaStatsQuery
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.network.service.JikanRestService
import it.matteoleggio.alchan.data.response.AnimeStats
import it.matteoleggio.alchan.data.response.MangaSerialization
import it.matteoleggio.alchan.data.response.MangaStats
import it.matteoleggio.alchan.data.response.ScoreEntry
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.pojo.StatusDistributionItem
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import it.matteoleggio.alchan.ui.common.ChartDialog
import it.matteoleggio.alchan.ui.settings.app.AppSettingsViewModel
import kotlinx.android.synthetic.main.fragment_media_stats.*
import kotlinx.android.synthetic.main.layout_loading.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 */
class MediaStatsFragment : BaseFragment() {
    private val viewModel by viewModel<MediaStatsViewModel>()
    private val viewModelSettings by viewModel<AppSettingsViewModel>()

    var votes = arrayListOf<Int>()

    private var mediaData: MediaStatsQuery.Media? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_stats, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.mediaId = arguments?.getInt(MediaFragment.MEDIA_ID)

        setupObserver()
    }

    private fun setupObserver() {
        viewModel.mediaStatsData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (viewModel.mediaId != it.data?.media?.id) {
                        return@Observer
                    }

                    viewModel.mediaData = it.data?.media
                    mediaData = it.data?.media
                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        if (viewModel.mediaData == null) {
            viewModel.getMediaStats()
        } else {
            mediaData = viewModel.mediaData
            initLayout()
        }
    }

    private fun initLayout() {
        handlePerformance()
        handleRankings()
        handleStatusDistribution()
        handleScoreDistribution()
    }

    private fun addScoreToArray(scores: ScoreEntry) {
        for (i in 0..scores.votes!!) {
            votes.add(scores.score!!)
        }
    }

    private fun handlePerformance() {
        mediaAvgScoreText.text = "${mediaData?.averageScore?.toString() ?: "0"}%"
        mediaMeanScoreText.text = "${mediaData?.meanScore?.toString() ?: "0"}%"
        mediaPopularityText.text = mediaData?.popularity?.toString() ?: "0"
        mediaFavoritesText.text = mediaData?.favourites?.toString() ?: "0"

        if (viewModelSettings.appSettings.fetchFromMal) {
            var animeStats: AnimeStats? = null
            var mangaStats: MangaStats? = null
            try {
                malPerformanceLayout.visibility = View.VISIBLE
                malPerformanceTextView.visibility = View.VISIBLE
                thread(start = true) {
                    val client = OkHttpClient()
                    val json = JSONObject()
                    json.put("query", MediaOverviewQuery.QUERY_DOCUMENT)
                    json.put("variables", JSONObject("{'id': ${viewModel.mediaId}}"))
                    val requestBody = RequestBody.create(null, json.toString())
                    val request = Request.Builder().url(Constant.ANILIST_API_URL).post(requestBody)
                        .addHeader("content-type", "application/json").build()
                    val response = client.newCall(request).execute().body?.string()
                    println(response.toString())

                    val Jobject = JSONObject(response!!)
                    val data = Jobject.getJSONObject("data")
                    val media = data.getJSONObject("Media")
                    var idMal: Int? = null
                    var type: String? = null
                    try {
                        idMal = media.getInt("idMal")
                        type = media.getString("type")
                    } catch (e: Exception) {
                        idMal = 0
                        type = ""
                    }

                    if (type == MediaType.ANIME.toString()) {
                        println(idMal)
                        animeStats =
                            JikanRestService().getAnimeStats(idMal!!).execute().body()
                        addScoreToArray(animeStats?.scores?.score1!!)
                        addScoreToArray(animeStats?.scores?.score2!!)
                        addScoreToArray(animeStats?.scores?.score3!!)
                        addScoreToArray(animeStats?.scores?.score4!!)
                        addScoreToArray(animeStats?.scores?.score5!!)
                        addScoreToArray(animeStats?.scores?.score6!!)
                        addScoreToArray(animeStats?.scores?.score7!!)
                        addScoreToArray(animeStats?.scores?.score8!!)
                        addScoreToArray(animeStats?.scores?.score9!!)
                        addScoreToArray(animeStats?.scores?.score10!!)
                    } else {
                        println(idMal)
                        mangaStats =
                            JikanRestService().getMangaStats(idMal!!).execute().body()
                        addScoreToArray(mangaStats?.scores?.score1!!)
                        addScoreToArray(mangaStats?.scores?.score2!!)
                        addScoreToArray(mangaStats?.scores?.score3!!)
                        addScoreToArray(mangaStats?.scores?.score4!!)
                        addScoreToArray(mangaStats?.scores?.score5!!)
                        addScoreToArray(mangaStats?.scores?.score6!!)
                        addScoreToArray(mangaStats?.scores?.score7!!)
                        addScoreToArray(mangaStats?.scores?.score8!!)
                        addScoreToArray(mangaStats?.scores?.score9!!)
                        addScoreToArray(mangaStats?.scores?.score10!!)
                    }
                }
                malMediaAvgScoreText.text = "${votes.average().toString() ?: "0"}%"
                try {
                    malTotalWatchesText.text = animeStats?.total.toString()
                } catch (E: Exception) {
                    malTotalWatchesText.text = mangaStats?.total.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleRankings() {
        if (mediaData?.rankings?.isNullOrEmpty() == true) {
            mediaStatsRankingLayout.visibility = View.GONE
            return
        }

        mediaStatsRankingLayout.visibility = View.VISIBLE
        mediaStatsRankingRecyclerView.adapter = MediaStatsRankingRvAdapter(mediaData?.rankings!!)
    }

    private fun handleStatusDistribution() {
        if (mediaData?.stats?.statusDistribution?.isNullOrEmpty() == true) {
            mediaStatsStatusLayout.visibility = View.GONE
            return
        }

        mediaStatsStatusLayout.visibility = View.VISIBLE

        val statusDistributionList = ArrayList<StatusDistributionItem>()

        val pieEntries = ArrayList<PieEntry>()
        mediaData?.stats?.statusDistribution?.forEach {
            val pieEntry = PieEntry(it?.amount!!.toFloat(), it.status?.toString())
            pieEntries.add(pieEntry)
            statusDistributionList.add(StatusDistributionItem(it.status?.name!!, it.amount, Constant.STATUS_COLOR_LIST[statusDistributionList.size]))
        }

        val pieDataSet = PieDataSet(pieEntries, "Score Distribution")
        pieDataSet.colors = Constant.STATUS_COLOR_LIST

        if (!viewModel.showStatsAutomatically) {
            mediaStatsStatusPieChart.visibility = View.GONE
            mediaStatsStatusShowButton.visibility = View.VISIBLE

            mediaStatsStatusShowButton.setOnClickListener {
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

                mediaStatsStatusPieChart.setHoleColor(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
                mediaStatsStatusPieChart.setDrawEntryLabels(false)
                mediaStatsStatusPieChart.setTouchEnabled(false)
                mediaStatsStatusPieChart.description.isEnabled = false
                mediaStatsStatusPieChart.legend.isEnabled = false
                mediaStatsStatusPieChart.data = pieData
                mediaStatsStatusPieChart.invalidate()
            } catch (e: Exception) {
                DialogUtility.showToast(activity, e.localizedMessage)
            }

            mediaStatsStatusPieChart.visibility = View.VISIBLE
            mediaStatsStatusShowButton.visibility = View.GONE
        }

        mediaStatsStatusRecyclerView.adapter = MediaStatsStatusRvAdapter(requireActivity(), statusDistributionList)
    }

    private fun handleScoreDistribution() {
        if (mediaData?.stats?.scoreDistribution?.isNullOrEmpty() == true) {
            mediaStatsScoreLayout.visibility = View.GONE
            return
        }

        mediaStatsScoreLayout.visibility = View.VISIBLE

        val barEntries = ArrayList<BarEntry>()
        mediaData?.stats?.scoreDistribution?.forEach {
            val barEntry = BarEntry(it?.score?.toFloat()!!, it.amount?.toFloat()!!)
            barEntries.add(barEntry)
        }

        val barDataSet = BarDataSet(barEntries, "Score Distribution")
        barDataSet.colors = Constant.SCORE_COLOR_LIST

        if (!viewModel.showStatsAutomatically) {
            mediaStatsScoreBarChart.visibility = View.GONE
            mediaStatsScoreShowButton.visibility = View.VISIBLE

            mediaStatsScoreShowButton.setOnClickListener {
                val dialog = ChartDialog()
                val bundle = Bundle()
                bundle.putString(ChartDialog.BAR_ENTRIES, viewModel.gson.toJson(barDataSet))
                dialog.arguments = bundle
                dialog.show(childFragmentManager, null)
            }
        } else {
            try {
                val barData = BarData(barDataSet)
                barData.setValueTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
                barData.barWidth = 3F

                mediaStatsScoreBarChart.axisLeft.setDrawGridLines(false)
                mediaStatsScoreBarChart.axisLeft.setDrawAxisLine(false)
                mediaStatsScoreBarChart.axisLeft.setDrawLabels(false)

                mediaStatsScoreBarChart.axisRight.setDrawGridLines(false)
                mediaStatsScoreBarChart.axisRight.setDrawAxisLine(false)
                mediaStatsScoreBarChart.axisRight.setDrawLabels(false)

                mediaStatsScoreBarChart.xAxis.setDrawGridLines(false)
                mediaStatsScoreBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                mediaStatsScoreBarChart.xAxis.setLabelCount(barEntries.size, true)
                mediaStatsScoreBarChart.xAxis.textColor = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor)

                mediaStatsScoreBarChart.setTouchEnabled(false)
                mediaStatsScoreBarChart.description.isEnabled = false
                mediaStatsScoreBarChart.legend.isEnabled = false
                mediaStatsScoreBarChart.data = barData
                mediaStatsScoreBarChart.invalidate()
            } catch (e: Exception) {
                DialogUtility.showToast(activity, e.localizedMessage)
            }

            mediaStatsScoreBarChart.visibility = View.VISIBLE
            mediaStatsScoreShowButton.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaStatsRankingRecyclerView.adapter = null
        mediaStatsStatusRecyclerView.adapter = null
    }
}

package it.matteoleggio.alchan.ui.profile.stats.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.StatsCategory
import it.matteoleggio.alchan.helper.pojo.UserStatsData
import it.matteoleggio.alchan.helper.roundToTwoDecimal
import it.matteoleggio.alchan.helper.setRegularPlural
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import kotlinx.android.synthetic.main.list_stats_detail.view.*
import type.MediaType

class StatsDetailRvAdapter(private val context: Context,
                           private val list: List<UserStatsData>,
                           private val mediaList: List<MediaImageQuery.Medium?>?,
                           private val characterList: List<CharacterImageQuery.Character?>?,
                           private val statsCategory: StatsCategory,
                           private val mediaType: MediaType,
                           private val showCharacter: Boolean,
                           private val listener: StatsDetailListener
): RecyclerView.Adapter<StatsDetailRvAdapter.ViewHolder>() {

    interface StatsDetailListener {
        fun passSelectedData(id: Int, browsePage: BrowsePage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_stats_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (statsCategory) {
            StatsCategory.SCORE -> handleScoreLayout(holder, position)
            StatsCategory.GENRE, StatsCategory.TAG, StatsCategory.VOICE_ACTOR, StatsCategory.STAFF, StatsCategory.STUDIO -> handleRankedLayout(holder, position)
            else -> handleLayout(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun handleLayout(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.statsRankLayout.visibility = View.GONE
        holder.statsMeanScoreLayout.visibility = View.VISIBLE
        holder.statsMediaRecyclerView.visibility = View.GONE

        holder.statsLabelText.setTextColor(item.color ?: AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
        holder.statsLabelText.text = item.label
        holder.statsCountText.text = item.count?.toString() ?: "0"
        holder.statsCountPercentageText.text = statsCountPercentageText(item)
        holder.statsProgressLabel.text = getProgressLabel()
        holder.statsProgressText.text = getProgressString(item)
        holder.statsProgressPercentageText.text= getProgressPercentageString(item)
        holder.statsScoreText.text = item.meanScore?.roundToTwoDecimal()
    }

    private fun handleScoreLayout(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.statsRankLayout.visibility = View.GONE
        holder.statsMeanScoreLayout.visibility = View.GONE
        holder.statsMediaRecyclerView.visibility = View.GONE

        holder.statsLabelText.setTextColor(item.color ?: AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
        holder.statsLabelText.text = item.label
        holder.statsCountText.text = item.count?.toString() ?: "0"
        holder.statsCountPercentageText.text = statsCountPercentageText(item)
        holder.statsProgressLabel.text = getProgressLabel()
        holder.statsProgressText.text = getProgressString(item)
        holder.statsProgressPercentageText.text= getProgressPercentageString(item)
    }

    private fun handleRankedLayout(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.statsRankLayout.visibility = View.VISIBLE
        holder.statsMeanScoreLayout.visibility = View.VISIBLE
        holder.statsMediaRecyclerView.visibility = View.GONE

        holder.statsRankText.text = String.format("%02d.", position + 1)
        holder.statsLabelText.setTextColor(item.color ?: AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
        holder.statsLabelText.text = item.label
        holder.statsCountText.text = item.count?.toString() ?: "0"
        holder.statsCountPercentageText.text = statsCountPercentageText(item)
        holder.statsProgressLabel.text = getProgressLabel()
        holder.statsProgressText.text = getProgressString(item)
        holder.statsProgressPercentageText.text= getProgressPercentageString(item)
        holder.statsScoreText.text = item.meanScore?.roundToTwoDecimal()

        if (statsCategory == StatsCategory.VOICE_ACTOR && showCharacter) {
            if (!item.characterIds.isNullOrEmpty() && !characterList.isNullOrEmpty()) {
                // only take 6 from each entry
                val characterItemList = ArrayList<StatsDetailCharacterItem>()
                item.characterIds.forEach { id ->
                    if (characterItemList.size == 6) {
                        return@forEach
                    }
                    val findCharacter = characterList.find { character -> character?.id == id }
                    if (findCharacter != null) {
                        characterItemList.add(StatsDetailCharacterItem(id!!, findCharacter.image?.large))
                    }
                }
                holder.statsMediaRecyclerView.adapter = StatsDetailCharacterRvAdapter(context, characterItemList, object : StatsDetailCharacterRvAdapter.StatsDetailCharacterListener {
                    override fun passSelectedCharacter(id: Int) {
                        listener.passSelectedData(id, BrowsePage.CHARACTER)
                    }
                })
                holder.statsMediaRecyclerView.visibility = View.VISIBLE
            }
        } else {
            if (!item.mediaIds.isNullOrEmpty() && !mediaList.isNullOrEmpty()) {
                // only take 6 from each entry
                val mediaItemList = ArrayList<StatsDetailMediaItem>()
                item.mediaIds.forEach { id ->
                    if (mediaItemList.size == 6) {
                        return@forEach
                    }
                    val findMedia = mediaList.find { media -> media?.id == id }
                    if (findMedia != null) {
                        mediaItemList.add(StatsDetailMediaItem(id!!, findMedia.coverImage?.large, findMedia.type!!))
                    }
                }
                holder.statsMediaRecyclerView.adapter = StatsDetailMediaRvAdapter(context, mediaItemList, object : StatsDetailMediaRvAdapter.StatsDetailMediaListener {
                    override fun passSelectedMedia(id: Int, mediaType: MediaType) {
                        listener.passSelectedData(id, if (mediaType == MediaType.ANIME) BrowsePage.ANIME else BrowsePage.MANGA)
                    }
                })
                holder.statsMediaRecyclerView.visibility = View.VISIBLE
            }
        }

        if (statsCategory == StatsCategory.STAFF || statsCategory == StatsCategory.VOICE_ACTOR || statsCategory == StatsCategory.STUDIO) {
            holder.itemView.setOnClickListener {
                val targetPage = when (statsCategory) {
                    StatsCategory.STAFF, StatsCategory.VOICE_ACTOR -> BrowsePage.STAFF
                    StatsCategory.STUDIO -> BrowsePage.STUDIO
                    else -> null
                } ?: return@setOnClickListener

                listener.passSelectedData(item.id!!, targetPage)
            }
        }
    }

    private fun statsCountPercentageText(item: UserStatsData): String {
        if (item.count == null) {
            return "(0%)"
        }

        val percentage = (item.count / list.sumByDouble { it.count?.toDouble()!! } * 100).roundToTwoDecimal()

        return "($percentage%)"
    }

    private fun getProgressLabel(): String {
        return if (mediaType == MediaType.ANIME) {
            context.getString(R.string.time_watched)
        } else {
            context.getString(R.string.chapters_read)
        }
    }

    private fun getProgressString(item: UserStatsData): String {
        if (mediaType == MediaType.ANIME) {
            if (item.minutesWatched == null || item.minutesWatched == 0) {
                return "0 hours"
            }
            val days = (item.minutesWatched / 60 / 24)
            val hours = (item.minutesWatched - (days * 60 * 24)) / 60
            var progressText = ""
            if (days != 0) {
                progressText += "$days ${"day".setRegularPlural(days)}"
            }
            if (hours != 0) {
                if (days != 0) {
                    progressText += " "
                }
                progressText += "$hours ${"hour".setRegularPlural(hours)}"
            }
            return progressText
        } else {
            return item.chaptersRead?.toString() ?: "0"
        }
    }

    private fun getProgressPercentageString(item: UserStatsData): String {
        if (mediaType == MediaType.ANIME) {
            if (item.minutesWatched == null || item.minutesWatched == 0) {
                return "(0%)"
            }
            val percentage = (item.minutesWatched / list.sumByDouble { it.minutesWatched?.toDouble()!! } * 100).roundToTwoDecimal()
            return "($percentage%)"
        } else {
            if (item.chaptersRead == null || item.chaptersRead == 0) {
                return "(0%)"
            }
            val percentage = (item.chaptersRead / list.sumByDouble { it.chaptersRead?.toDouble()!! } * 100).roundToTwoDecimal()
            return "($percentage%)"
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val statsRankLayout = view.statsRankLayout!!
        val statsRankText = view.statsRankText!!
        val statsLabelText = view.statsLabelText!!
        val statsCountText = view.statsCountText!!
        val statsCountPercentageText = view.statsCountPercentageText!!
        val statsProgressLabel = view.statsProgressLabel!!
        val statsProgressText = view.statsProgressText!!
        val statsProgressPercentageText = view.statsProgressPercentageText!!
        val statsMeanScoreLayout = view.statsMeanScoreLayout!!
        val statsScoreText = view.statsScoreText!!
        val statsMediaRecyclerView = view.statsMediaRecyclerView!!
    }

    class StatsDetailMediaItem(val id: Int, val image: String?, val mediaType: MediaType)

    class StatsDetailCharacterItem(val id: Int, val image: String?)
}
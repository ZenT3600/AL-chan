package it.matteoleggio.alchan.ui.animelist.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.MediaList
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.ListStyle
import it.matteoleggio.alchan.helper.roundToOneDecimal
import it.matteoleggio.alchan.helper.secondsToDateTime
import it.matteoleggio.alchan.helper.setRegularPlural
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import kotlinx.android.synthetic.main.list_anime_list_linear.view.*
import kotlinx.android.synthetic.main.list_title.view.*
import type.ScoreFormat

class AnimeListRvAdapter(private val context: Context,
                         private val list: List<MediaList>,
                         private val scoreFormat: ScoreFormat,
                         private val listStyle: ListStyle?,
                         private val useRelativeDate: Boolean,
                         private val listener: AnimeListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TITLE = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TITLE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_title, parent, false)
            TitleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_anime_list_linear, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TitleViewHolder) {
            val item = list[position]
            holder.titleText.text = item.notes
            if (listStyle?.textColor != null) {
                holder.titleText.setTextColor(Color.parseColor(listStyle.textColor))
            }
        } else if (holder is ItemViewHolder) {
            val mediaList = list[position]

            GlideApp.with(context).load(mediaList.media?.coverImage?.large).into(holder.animeCoverImage)
            holder.animeTitleText.text = mediaList.media?.title?.userPreferred
            holder.animeFormatText.text = mediaList.media?.format?.name?.replace('_', ' ')

            if (listStyle?.hideAiringIndicator != true && mediaList.media?.nextAiringEpisode != null) {
                holder.animeAiringDividerIcon.visibility = View.VISIBLE
                holder.animeAiringDateText.visibility = View.VISIBLE

                val episode = mediaList.media?.nextAiringEpisode?.episode
                val timeUntilAiring = mediaList.media?.nextAiringEpisode?.timeUntilAiring ?: 0

                var nextEpisodeMessage = if (useRelativeDate) {
                    when {
                        timeUntilAiring > 3600 * 24 -> {
                            context.getString(R.string.ep_in, episode, timeUntilAiring / 3600 / 24 + 1, context.getString(R.string.day).setRegularPlural(timeUntilAiring / 3600 / 24 + 1))
                        }
                        timeUntilAiring >= 3600 -> {
                            context.getString(R.string.ep_in, episode, timeUntilAiring / 3600, context.getString(R.string.hour).setRegularPlural(timeUntilAiring / 3600))
                        }
                        else -> {
                            context.getString(R.string.ep_in, episode, timeUntilAiring / 60, context.getString(R.string.minute).setRegularPlural(timeUntilAiring / 60))
                        }
                    }
                } else {
                    context.getString(R.string.ep_on, episode, mediaList.media?.nextAiringEpisode?.airingAt?.secondsToDateTime())
                }

                val epDiff = mediaList.media?.nextAiringEpisode?.episode!! - mediaList.progress!!
                if (epDiff > 1) {
                    nextEpisodeMessage += context.getString(R.string.you_are_behind, epDiff - 1, context.getString(R.string.episode_small).setRegularPlural(epDiff - 1))
                }
                holder.animeAiringDateText.text = nextEpisodeMessage
            } else {
                holder.animeAiringDividerIcon.visibility = View.GONE
                holder.animeAiringDateText.visibility = View.GONE
            }

            if (scoreFormat == ScoreFormat.POINT_3) {
                GlideApp.with(context).load(AndroidUtility.getSmileyFromScore(mediaList.score)).into(holder.animeStarIcon)
                holder.animeStarIcon.imageTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                holder.animeRatingText.text = ""
            } else {
                GlideApp.with(context).load(R.drawable.ic_star_filled).into(holder.animeStarIcon)
                holder.animeStarIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellowStar))
                holder.animeRatingText.text = if (mediaList.score == null || mediaList.score == 0.0) {
                    "?"
                } else {
                    mediaList.score?.roundToOneDecimal()
                }
            }

            holder.animeProgressBar.progress = if (mediaList.media?.episodes == null && mediaList.progress!! > 0) {
                50
            } else if (mediaList.media?.episodes != null && mediaList.media?.episodes != 0) {
                (mediaList.progress!!.toDouble() / mediaList.media?.episodes!!.toDouble() * 100).toInt()
            } else {
                0
            }

            holder.animeProgressText.text = "${mediaList.progress}/${mediaList.media?.episodes ?: '?'}"
            holder.animeIncrementProgressButton.visibility = if ((mediaList.media?.episodes == null || mediaList.media?.episodes!! > mediaList.progress!!) && mediaList.progress!! < UShort.MAX_VALUE.toInt()) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                listener.openEditor(mediaList.id)
            }

            holder.animeIncrementProgressButton.setOnClickListener {
                listener.incrementProgress(mediaList)
            }

            holder.animeProgressText.setOnClickListener {
                listener.openProgressDialog(mediaList)
            }

            holder.animeRatingText.setOnClickListener {
                listener.openScoreDialog(mediaList)
            }

            holder.animeStarIcon.setOnClickListener {
                listener.openScoreDialog(mediaList)
            }

            holder.animeTitleText.setOnClickListener {
                listener.openBrowsePage(mediaList.media!!)
            }

            holder.animeCoverImage.setOnClickListener {
                listener.openBrowsePage(mediaList.media!!)
            }

            holder.itemView.setOnLongClickListener {
                if (listStyle?.longPressViewDetail == true) {
                    listener.showDetail(mediaList.id)
                    true
                } else {
                    false
                }
            }

            if (listStyle?.showNotesIndicator == true && !mediaList.notes.isNullOrBlank()) {
                holder.animeNotesLayout.visibility = View.VISIBLE
                holder.animeNotesLayout.setOnClickListener {
                    DialogUtility.showInfoDialog(context, mediaList.notes ?: "")
                }
            } else {
                holder.animeNotesLayout.visibility = View.GONE
            }

            if (listStyle?.showPriorityIndicator == true && mediaList.priority != null && mediaList.priority != 0) {
                holder.animePriorityIndicator.visibility = View.VISIBLE
                holder.animePriorityIndicator.setBackgroundColor(Constant.PRIORITY_COLOR_MAP[mediaList.priority!!]!!)
            } else {
                holder.animePriorityIndicator.visibility = View.GONE
            }

            if (listStyle?.hideScoreWhenNotScored == true && (mediaList.score == null || mediaList.score == 0.0)) {
                holder.animeStarIcon.visibility = View.GONE
                holder.animeRatingText.visibility = View.GONE
            } else {
                holder.animeStarIcon.visibility = View.VISIBLE
                holder.animeRatingText.visibility = View.VISIBLE
            }

            if (listStyle?.cardColor != null) {
                holder.listCardBackground.setCardBackgroundColor(Color.parseColor(listStyle.cardColor))

                // 6 is color hex code length without the alpha
                val transparentCardColor =  "#CC" + listStyle.cardColor?.substring(listStyle.cardColor?.length!! - 6)
                holder.animeNotesLayout.setCardBackgroundColor(Color.parseColor(transparentCardColor))
            }

            if (listStyle?.primaryColor != null) {
                holder.animeTitleText.setTextColor(Color.parseColor(listStyle.primaryColor))
                holder.animeRatingText.setTextColor(Color.parseColor(listStyle.primaryColor))
                holder.animeProgressText.setTextColor(Color.parseColor(listStyle.primaryColor))
                holder.animeIncrementProgressButton.strokeColor = ColorStateList.valueOf(Color.parseColor(listStyle.primaryColor))
                holder.animeIncrementProgressButton.setTextColor(Color.parseColor(listStyle.primaryColor))

                if (scoreFormat == ScoreFormat.POINT_3) {
                    holder.animeStarIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(listStyle.primaryColor))
                }
            }

            if (listStyle?.secondaryColor != null) {
                holder.animeAiringDateText.setTextColor(Color.parseColor(listStyle.secondaryColor))
                holder.animeProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(listStyle.secondaryColor))
                val transparentSecondary = listStyle.secondaryColor?.substring(0, 1) + "80" + listStyle.secondaryColor?.substring(1)
                holder.animeProgressBar.progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor(transparentSecondary))
            }

            if (listStyle?.textColor != null) {
                holder.animeFormatText.setTextColor(Color.parseColor(listStyle.textColor))
                holder.animeAiringDividerIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(listStyle.textColor))
                holder.animeNotesIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(listStyle.textColor))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].id == 0) VIEW_TYPE_TITLE else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listCardBackground = view.listCardBackground!!
        val animeTitleText = view.animeTitleText!!
        val animeCoverImage = view.animeCoverImage!!
        val animeFormatText = view.animeFormatText!!
        val animeAiringDividerIcon = view.animeAiringDividerIcon!!
        val animeAiringDateText = view.animeAiringDateText!!
        val animeStarIcon = view.animeStarIcon!!
        val animeRatingText = view.animeRatingText!!
        val animeProgressBar = view.animeProgressBar!!
        val animeProgressText = view.animeProgressText!!
        val animeIncrementProgressButton = view.animeIncrementProgressButton!!
        val animeNotesLayout = view.animeNotesLayout!!
        val animeNotesIcon = view.animeNotesIcon!!
        val animePriorityIndicator = view.animePriorityIndicator!!
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText = view.titleText!!
    }
}
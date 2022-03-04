package it.matteoleggio.alchan.ui.animelist.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.android.synthetic.main.list_anime_list_album.view.*
import kotlinx.android.synthetic.main.list_title.view.*
import type.ScoreFormat

class AnimeListAlbumRvAdapter(private val context: Context,
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_anime_list_album, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TitleViewHolder) {
            val mediaList = list[position]
            holder.titleText.text = mediaList.notes
            if (listStyle?.textColor != null) {
                holder.titleText.setTextColor(Color.parseColor(listStyle.textColor))
            }
        } else if (holder is ItemViewHolder) {
            val mediaList = list[position]

            GlideApp.with(context).load(mediaList.media?.coverImage?.large).into(holder.animeCoverImage)
            holder.animeTitleText.text = mediaList.media?.title?.userPreferred

            if (listStyle?.hideAiringIndicator != true && mediaList.media?.nextAiringEpisode != null) {
                holder.animeAiringIcon.visibility = View.VISIBLE
                if (mediaList.media?.nextAiringEpisode?.episode!! > mediaList.progress!! + 1) {
                    GlideApp.with(context).load(R.drawable.ic_spam).into(holder.animeAiringIcon)
                } else {
                    GlideApp.with(context).load(R.drawable.ic_filled_circle).into(holder.animeAiringIcon)
                }
            } else {
                holder.animeAiringIcon.visibility = View.GONE
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

            holder.animeProgressText.text = "${mediaList.progress}/${mediaList.media?.episodes ?: '?'}"

            holder.animeAiringIcon.setOnClickListener {
                val episode = mediaList.media?.nextAiringEpisode?.episode
                val timeUntilAiring = mediaList.media?.nextAiringEpisode?.timeUntilAiring ?: 0

                var message = if (useRelativeDate) {
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
                    message += context.getString(R.string.you_are_behind, epDiff - 1, context.getString(R.string.episode_small).setRegularPlural(epDiff - 1))
                }
                DialogUtility.showToast(context, message, Toast.LENGTH_LONG)
            }

            holder.animeTitleText.setOnClickListener {
                listener.openBrowsePage(mediaList.media!!)
            }

            holder.animeProgressText.setOnClickListener {
                listener.openProgressDialog(mediaList)
            }

            holder.animeScoreLayout.setOnClickListener {
                listener.openScoreDialog(mediaList)
            }

            holder.animeCoverImage.setOnClickListener {
                listener.openEditor(mediaList.id)
            }

            holder.animeCoverImage.setOnLongClickListener {
                if (listStyle?.longPressViewDetail == true) {
                    listener.showDetail(mediaList.id)
                    true
                } else {
                    false
                }
            }

            if (listStyle?.showPriorityIndicator == true && mediaList.priority != null && mediaList.priority != 0) {
                holder.animePriorityIndicator.visibility = View.VISIBLE
                holder.animePriorityIndicator.backgroundTintList = ColorStateList.valueOf(Constant.PRIORITY_COLOR_MAP[mediaList.priority!!]!!)
            } else {
                holder.animePriorityIndicator.visibility = View.GONE
            }

            if (listStyle?.hideScoreWhenNotScored == true && (mediaList.score == null || mediaList.score == 0.0)) {
                holder.animeScoreLayout.visibility = View.GONE
            } else {
                holder.animeScoreLayout.visibility = View.VISIBLE
            }

            if (listStyle?.cardColor != null) {
                holder.listCardBackground.setCardBackgroundColor(Color.parseColor(listStyle.cardColor))
            }

            if (listStyle?.primaryColor != null) {
                holder.animeTitleText.setTextColor(Color.parseColor(listStyle.primaryColor))
                holder.animeRatingText.setTextColor(Color.parseColor(listStyle.primaryColor))
                holder.animeProgressText.setTextColor(Color.parseColor(listStyle.primaryColor))

                if (scoreFormat == ScoreFormat.POINT_3) {
                    holder.animeStarIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(listStyle.primaryColor))
                }
            }

            if (listStyle?.secondaryColor != null) {
                holder.animeAiringIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(listStyle.secondaryColor))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].id == 0) AnimeListGridRvAdapter.VIEW_TYPE_TITLE else AnimeListGridRvAdapter.VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listCardBackground = view.listCardBackground!!
        val animeTitleText = view.animeTitleText!!
        val animeCoverImage = view.animeCoverImage!!
        val animeProgressText = view.animeProgressText!!
        val animeScoreLayout = view.animeScoreLayout!!
        val animeStarIcon = view.animeStarIcon!!
        val animeRatingText = view.animeRatingText!!
        val animeAiringIcon = view.animeAiringIcon!!
        val animePriorityIndicator = view.animePriorityIndicator!!
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText = view.titleText!!
    }
}
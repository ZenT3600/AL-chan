package com.zen.alchan.ui.medialist

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.textview.MaterialTextView
import com.zen.alchan.R
import com.zen.alchan.data.entitiy.AppSetting
import com.zen.alchan.data.response.anilist.MediaListOptions
import com.zen.alchan.databinding.ListTitleBinding
import com.zen.alchan.data.entitiy.ListStyle
import com.zen.alchan.data.response.anilist.Media
import com.zen.alchan.data.response.anilist.MediaList
import com.zen.alchan.helper.extensions.*
import com.zen.alchan.helper.pojo.MediaListItem
import com.zen.alchan.ui.base.BaseRecyclerViewAdapter
import type.MediaFormat
import type.MediaType
import type.ScoreFormat

abstract class BaseMediaListRvAdapter(
    private val context: Context,
    list: List<MediaListItem>,
    private val appSetting: AppSetting,
    private val listStyle: ListStyle,
    private val mediaListOptions: MediaListOptions
) : BaseRecyclerViewAdapter<MediaListItem, ViewBinding>(list) {

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    protected inner class TitleViewHolder(private val binding: ListTitleBinding) : ViewHolder(binding) {
        override fun bind(item: MediaListItem, index: Int) {
            binding.titleText.text = item.title
        }
    }

    abstract inner class ItemViewHolder(private val binding: ViewBinding) : ViewHolder(binding) {
        protected fun getCoverImage(media: Media): String {
            return media.getCoverImage(appSetting)
        }

        protected fun getTitle(media: Media): String {
            return media.getTitle(appSetting)
        }

        protected fun getFormat(media: Media): String {
            return media.getFormattedMediaFormat(true)
        }

        protected fun shouldShowAiringIndicator(media: Media): Boolean {
            return !listStyle.hideAiringIndicator && media.type == MediaType.ANIME && media.nextAiringEpisode != null
        }

        @DrawableRes
        protected fun getAiringIndicatorIcon(mediaList: MediaList): Int {
            val nextEpisode = mediaList.media.nextAiringEpisode?.episode ?: 0
            return if (mediaList.progress + 1 < nextEpisode)
                R.drawable.ic_spam
            else
                R.drawable.ic_filled_circle
        }

        protected fun getAiringText(mediaList: MediaList): String {
            val nextAiringEpisode = mediaList.media.nextAiringEpisode ?: return ""
            val airingScheduleText = if (appSetting.useRelativeDateForNextAiringEpisode) {
                val secondsUntilAiring = nextAiringEpisode.timeUntilAiring
                val remainingTime = when {
                    // more than 1 day
                    secondsUntilAiring > 3600 * 24 -> {
                        secondsUntilAiring.convertSecondsToDays().showUnit(context, R.plurals.day)
                    }
                    // more than 1 hour
                    secondsUntilAiring >= 3600 -> {
                        secondsUntilAiring.convertSecondsToHours().showUnit(context, R.plurals.hour)
                    }
                    else -> {
                        secondsUntilAiring.convertSecondsToMinutes().showUnit(context, R.plurals.minute)
                    }
                }
                context.getString(R.string.ep_x_in_y, nextAiringEpisode.episode, remainingTime)
            } else {
                context.getString(R.string.ep_x_on_y, nextAiringEpisode.episode, nextAiringEpisode.airingAt.displaySecondsInDayDateTimeFormat())
            }

            var episodesBehindText = ""
            val progressDifference = nextAiringEpisode.episode - mediaList.progress
            if (progressDifference > 1) {
                episodesBehindText += " "
                episodesBehindText += context.getString(R.string.you_are_x_behind, (progressDifference - 1).showUnit(context, R.plurals.episode))
            }

            return airingScheduleText + episodesBehindText
        }

        protected fun shouldShowNotes(mediaList: MediaList): Boolean {
            return listStyle.showNotes && mediaList.notes.isNotBlank()
        }

        protected fun shouldShowPriority(mediaList: MediaList): Boolean {
            return listStyle.showPriority && mediaList.priority > 0
        }

        @ColorInt
        protected fun getPriorityColor(mediaList: MediaList): Int {
            return Color.parseColor(
                when (mediaList.priority) {
                    1 -> "FF0000"
                    2 -> "#FFFF00"
                    3 -> "#00FF00"
                    4 -> "#00FFFF"
                    5 -> "#0000FF"
                    else -> "#0000FF"
                }
            )
        }

        protected fun handleScoring(scoreLayout: View, scoreIcon: View, scoreText: TextView, scoreSmiley: ImageView, mediaList: MediaList) {
            if (listStyle.hideScoreWhenNotScored)
                scoreLayout.show(mediaList.score != 0.0)

            val isUseNumberScore = mediaListOptions.scoreFormat != ScoreFormat.POINT_3
            scoreIcon.show(isUseNumberScore)
            scoreText.show(isUseNumberScore)
            scoreSmiley.show(!isUseNumberScore)

            scoreText.text = mediaList.getScore()
            scoreSmiley.setImageResource(mediaList.getScoreSmiley())
        }

        protected fun getProgressText(mediaList: MediaList): String {
            return when (mediaList.media.type) {
                MediaType.ANIME -> {
                    "${mediaList.progress} / ${mediaList.media.episodes ?: "?"}"
                }
                MediaType.MANGA -> {
                    "${mediaList.progress} / ${mediaList.media.chapters ?: "?"}"
                }
                else -> {
                    "${mediaList.progress} / ?"
                }
            }
        }

        protected fun getProgressVolumeText(mediaList: MediaList): String {
            return "${mediaList.progressVolumes ?: 0} / ${mediaList.media.volumes ?: "?"}"
        }

        protected fun shouldShowProgress(media: Media): Boolean {
            if (media.type == MediaType.ANIME)
                return true

            if (media.format == MediaFormat.NOVEL)
                return !listStyle.hideChapterForNovel

            return !listStyle.hideChapterForManga
        }

        protected fun shouldShowProgressVolume(media: Media): Boolean {
            if (media.type == MediaType.ANIME)
                return false

            if (media.format == MediaFormat.NOVEL)
                return !listStyle.hideVolumeForNovel

            return !listStyle.hideVolumeForManga
        }
    }

    interface MediaListListener {
        fun showAiringText(airingText: String)
        fun showNotes(mediaList: MediaList)
    }
}
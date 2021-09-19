package com.zen.alchan.ui.medialist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zen.alchan.data.entitiy.AppSetting
import com.zen.alchan.data.entitiy.ListStyle
import com.zen.alchan.data.response.anilist.MediaListOptions
import com.zen.alchan.databinding.ListMediaListAlbumBinding
import com.zen.alchan.databinding.ListTitleBinding
import com.zen.alchan.helper.extensions.clicks
import com.zen.alchan.helper.extensions.show
import com.zen.alchan.helper.pojo.MediaListItem
import com.zen.alchan.helper.utils.ImageUtil

class MediaListAlbumRvAdapter(
    private val context: Context,
    list: List<MediaListItem>,
    appSetting: AppSetting,
    listStyle: ListStyle,
    mediaListOptions: MediaListOptions,
    private val listener: MediaListListener
) : BaseMediaListRvAdapter(context, list, appSetting, listStyle, mediaListOptions) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MediaListItem.VIEW_TYPE_TITLE -> {
                val view = ListTitleBinding.inflate(inflater, parent, false)
                TitleViewHolder(view)
            }
            else -> {
                val view = ListMediaListAlbumBinding.inflate(inflater, parent, false)
                ListItemViewHolder(view)
            }
        }
    }

    inner class ListItemViewHolder(private val binding: ListMediaListAlbumBinding) : ItemViewHolder(binding) {
        override fun bind(item: MediaListItem, index: Int) {
            val mediaList = item.mediaList
            val media = mediaList.media
            binding.apply {
                // cover
                ImageUtil.loadImage(context, getCoverImage(media), mediaListCoverImage)

                // title
                mediaListTitleText.text = getTitle(media)
                mediaListTitleText.clicks {

                }

                // airing indicator
                mediaListAiringIndicator.show(shouldShowAiringIndicator(media))
                mediaListAiringIndicator.setImageResource(getAiringIndicatorIcon(mediaList))
                mediaListAiringIndicator.clicks {
                    listener.showAiringText(getAiringText(mediaList))
                }

                // priority
                mediaListPriority.show(shouldShowPriority(mediaList))
                mediaListPriority.setBackgroundColor(getPriorityColor(mediaList))

                // score
                handleScoring(mediaListScoreLayout, mediaListScoreIcon, mediaListScoreText, mediaListScoreSmiley, mediaList)
                mediaListScoreLayout.clicks {

                }

                // progress
                mediaListProgressText.text = getProgressText(mediaList)
                mediaListProgressText.show(shouldShowProgress(media))
                mediaListProgressText.clicks {

                }

                mediaListProgressVolumeText.text = getProgressVolumeText(mediaList)
                mediaListProgressVolumeText.show(shouldShowProgressVolume(media))
                mediaListProgressVolumeText.clicks {

                }
            }
        }
    }
}
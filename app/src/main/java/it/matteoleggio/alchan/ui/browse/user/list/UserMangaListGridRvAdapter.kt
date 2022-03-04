package it.matteoleggio.alchan.ui.browse.user.list

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
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.roundToOneDecimal
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import kotlinx.android.synthetic.main.list_manga_list_grid.view.*
import kotlinx.android.synthetic.main.list_title.view.*
import type.ScoreFormat

class UserMangaListGridRvAdapter(private val context: Context,
                                 private val list: List<UserMediaListCollectionQuery.Entry?>,
                                 private val scoreFormat: ScoreFormat,
                                 private val userId: Int?,
                                 private val listener: UserMediaListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TITLE = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TITLE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_title, parent, false)
            TitleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_manga_list_grid, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TitleViewHolder) {
            val item = list[position]
            holder.titleText.text = item?.notes
        } else if (holder is ItemViewHolder) {
            val mediaList = list[position]!!

            GlideApp.with(context).load(mediaList.media?.coverImage?.large).into(holder.mangaCoverImage)
            holder.mangaTitleText.text = mediaList.media?.title?.userPreferred
            holder.mangaFormatText.text = mediaList.media?.format?.name?.replace('_', ' ')

            if (scoreFormat == ScoreFormat.POINT_3) {
                GlideApp.with(context).load(AndroidUtility.getSmileyFromScore(mediaList.score)).into(holder.mangaStarIcon)
                holder.mangaStarIcon.imageTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                holder.mangaRatingText.text = ""
            } else {
                GlideApp.with(context).load(R.drawable.ic_star_filled).into(holder.mangaStarIcon)
                holder.mangaStarIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellowStar))
                holder.mangaRatingText.text = if (mediaList.score == null || mediaList.score.toInt() == 0) {
                    "?"
                } else {
                    mediaList.score.roundToOneDecimal()
                }
            }

            holder.mangaProgressText.text = "${mediaList.progress}/${mediaList.media?.chapters ?: '?'}"
            holder.mangaProgressVolumesText.text = "${mediaList.progressVolumes}/${mediaList.media?.volumes ?: '?'}"

            if (!mediaList.notes.isNullOrBlank()) {
                holder.mangaNotesLayout.visibility = View.VISIBLE
                holder.mangaNotesLayout.setOnClickListener {
                    DialogUtility.showInfoDialog(context, mediaList.notes)
                }
            } else {
                holder.mangaNotesLayout.visibility = View.GONE
            }

            if (mediaList.priority != null && mediaList.priority != 0) {
                holder.mangaPriorityIndicator.visibility = View.VISIBLE
                holder.mangaPriorityIndicator.backgroundTintList = ColorStateList.valueOf(Constant.PRIORITY_COLOR_MAP[mediaList.priority]!!)
            } else {
                holder.mangaPriorityIndicator.visibility = View.GONE
            }

            holder.mangaProgressLayout.isEnabled = false
            holder.mangaProgressVolumesLayout.isEnabled = false
            holder.mangaScoreLayout.isEnabled = false

            holder.mangaCoverImage.setOnClickListener {
                listener.openSelectedMedia(mediaList.media?.id!!, mediaList.media.type!!)
            }

            holder.mangaCoverImage.setOnLongClickListener {
                listener.viewMediaListDetail(mediaList.id)
                true
            }

            if (userId == Constant.EVA_ID) {
                holder.listCardBackground.setCardBackgroundColor(Color.parseColor("#80000000"))
            } else {
                holder.listCardBackground.setCardBackgroundColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeCardColor))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position]?.id == 0) VIEW_TYPE_TITLE else VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val listCardBackground = view.listCardBackground!!
        val mangaTitleText = view.mangaTitleText!!
        val mangaTitleLayout = view.mangaTitleLayout!!
        val mangaCoverImage = view.mangaCoverImage!!
        val mangaFormatText = view.mangaFormatText!!
        val mangaFormatLayout = view.mangaFormatLayout!!
        val mangaProgressText = view.mangaProgressText!!
        val mangaProgressVolumesText = view.mangaProgressVolumesText!!
        val mangaScoreLayout = view.mangaScoreLayout!!
        val mangaStarIcon = view.mangaStarIcon!!
        val mangaRatingText = view.mangaRatingText!!
        val mangaProgressLayout = view.mangaProgressLayout!!
        val mangaProgressVolumesLayout = view.mangaProgressVolumesLayout!!
        val mangaPriorityIndicator = view.mangaPriorityIndicator!!
        val mangaNotesLayout = view.mangaNotesLayout!!
        val mangaNotesIcon = view.mangaNotesIcon!!
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText = view.titleText!!
    }
}
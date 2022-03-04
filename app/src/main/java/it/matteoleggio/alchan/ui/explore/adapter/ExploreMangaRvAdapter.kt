package it.matteoleggio.alchan.ui.explore.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.SearchResult
import it.matteoleggio.alchan.helper.replaceUnderscore
import it.matteoleggio.alchan.helper.setRegularPlural
import it.matteoleggio.alchan.ui.browse.media.overview.OverviewGenreRvAdapter
import it.matteoleggio.alchan.ui.search.SearchListener
import kotlinx.android.synthetic.main.list_explore_anime.view.*
import type.MediaListStatus

class ExploreMangaRvAdapter(private val context: Context,
                            private val list: List<SearchResult?>,
                            private val listener: SearchListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_explore_anime, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = list[position]?.mangaSearchResult
            GlideApp.with(context).load(item?.coverImage?.large).into(holder.exploreCoverImage)
            holder.exploreTitleText.text = item?.title?.userPreferred
            holder.exploreYearText.text = item?.startDate?.year?.toString() ?: "TBA"
            holder.exploreFormatText.text = item?.format?.name?.replaceUnderscore() ?: "TBA"
            holder.exploreScoreText.text = item?.averageScore?.toString() ?: "0"
            holder.exploreFavoriteText.text = item?.favourites?.toString() ?: "0"

            if (item?.staff != null && item.staff.edges?.isNullOrEmpty() == false) {
                var creatorList = ""
                item.staff.edges.forEachIndexed { index, it ->
                    creatorList += it?.node?.name?.full
                    if (index != item.staff.edges.lastIndex) creatorList += ", "
                }
                holder.exploreCreatorText.text = creatorList
            } else {
                holder.exploreCreatorText.text = "TBA"
            }

            if (item?.chapters != null && item.chapters != 0) {
                holder.exploreCountIcon.visibility = View.VISIBLE
                holder.exploreCountText.visibility = View.VISIBLE
                holder.exploreCountText.text = "${item.chapters} ${"chapter".setRegularPlural(item.chapters)}"
            } else {
                holder.exploreCountIcon.visibility = View.GONE
                holder.exploreCountText.visibility = View.GONE
            }

            if (!item?.genres.isNullOrEmpty()) {
                holder.exploreGenreRecyclerView.visibility = View.VISIBLE
                holder.exploreGenreRecyclerView.adapter = OverviewGenreRvAdapter(item?.genres!!, object : OverviewGenreRvAdapter.OverviewGenreListener {
                    override fun passSelectedGenre(genre: String) { }
                })
            } else {
                holder.exploreGenreRecyclerView.visibility = View.GONE
            }

            if (item?.mediaListEntry != null) {
                holder.userStatusText.visibility = View.VISIBLE
                holder.userStatusIcon.visibility = View.VISIBLE

                holder.userStatusText.text = if (item.mediaListEntry.status == MediaListStatus.CURRENT) context.getString(R.string.reading_caps) else item.mediaListEntry.status?.name.replaceUnderscore()

                val statusColor = Constant.STATUS_COLOR_MAP[item.mediaListEntry.status] ?: Constant.STATUS_COLOR_LIST[0]
                holder.userStatusText.setTextColor(statusColor)
                holder.userStatusIcon.setColorFilter(statusColor)
            } else {
                holder.userStatusText.visibility = View.GONE
                holder.userStatusIcon.visibility = View.GONE
            }

            holder.entryRankText.text = (position + 1).toString()

            holder.itemView.setOnClickListener {
                listener.passSelectedItem(item?.id!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exploreCoverImage = view.exploreCoverImage!!
        val exploreTitleText = view.exploreTitleText!!
        val exploreCreatorText = view.exploreCreatorText!!
        val exploreYearText = view.exploreYearText!!
        val exploreFormatText = view.exploreFormatText!!
        val exploreCountIcon = view.exploreCountIcon!!
        val exploreCountText = view.exploreCountText!!
        val exploreScoreText = view.exploreScoreText!!
        val exploreFavoriteText = view.exploreFavoriteText!!
        val exploreGenreRecyclerView = view.exploreGenreRecyclerView!!
        val userStatusIcon = view.userStatusIcon!!
        val userStatusText = view.userStatusText!!
        val entryRankText = view.entryRankText!!
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
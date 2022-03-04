package it.matteoleggio.alchan.ui.browse.user.list


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.network.Converter
import it.matteoleggio.alchan.data.response.Media
import it.matteoleggio.alchan.data.response.MediaList
import it.matteoleggio.alchan.data.response.MediaTitle
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ListType
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.MediaFilterData
import it.matteoleggio.alchan.helper.updateBottomPadding
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.common.MediaListDetailDialog
import it.matteoleggio.alchan.ui.filter.MediaFilterActivity
import kotlinx.android.synthetic.main.fragment_user_media_list.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import type.ScoreFormat

/**
 * A simple [Fragment] subclass.
 */
class UserMediaListFragment : BaseFragment() {

    private val viewModel by viewModel<UserMediaListViewModel>()

    private lateinit var adapter: RecyclerView.Adapter<*>
    private var itemSearch: MenuItem? = null
    private var itemCustomiseList: MenuItem? = null
    private var itemFilter: MenuItem? = null

    private var searchView: SearchView? = null

    companion object {
        const val USER_ID = "userId"
        const val MEDIA_TYPE = "mediaType"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_media_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        mediaListRecyclerView.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        mediaListRearrangeLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        viewModel.userId = arguments?.getInt(USER_ID)
        viewModel.mediaType = MediaType.valueOf(arguments?.getString(MEDIA_TYPE)!!)

        if (viewModel.listType == null) {
            viewModel.listType = ListType.LINEAR
        }

        toolbarLayout.apply {
            title = if (viewModel.mediaType == MediaType.ANIME) getString(R.string.anime_list) else getString(R.string.manga_list)
            setNavigationOnClickListener { activity?.onBackPressed() }
            navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_arrow_back)

            inflateMenu(R.menu.menu_media_list)
            itemSearch = menu.findItem(R.id.itemSearch)
            itemCustomiseList = menu.findItem(R.id.itemCustomiseList)
            itemFilter = menu.findItem(R.id.itemFilter)
        }

        searchView = itemSearch?.actionView as SearchView
        searchView?.queryHint = getString(R.string.search)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handleSearch(newText)
                return true
            }
        })

        itemFilter?.setOnMenuItemClickListener {
            val intent = Intent(activity, MediaFilterActivity::class.java)
            intent.putExtra(MediaFilterActivity.MEDIA_TYPE, viewModel.mediaType?.name)
            intent.putExtra(MediaFilterActivity.FILTER_DATA, viewModel.gson.toJson(viewModel.filterData))
            intent.putExtra(MediaFilterActivity.SCORE_FORMAT, viewModel.userData?.mediaListOptions?.scoreFormat?.name ?: ScoreFormat.POINT_100.name)
            startActivityForResult(intent, MediaFilterActivity.ACTIVITY_FILTER)
            true
        }

        itemCustomiseList?.title = getString(R.string.change_list_type)
        itemCustomiseList?.setOnMenuItemClickListener {
            if (viewModel.listType == ListType.LINEAR) {
                viewModel.listType = ListType.GRID
            } else {
                viewModel.listType = ListType.LINEAR
            }
            assignAdapter()
            true
        }

        assignAdapter()
        initLayout()
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.userMediaListCollection.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    viewModel.unfilteredData = it.data
                    viewModel.userData = it.data?.mediaListCollection?.user
                    if (viewModel.mediaType == MediaType.ANIME) {
                        toolbarLayout.title = "${viewModel.userData?.name} ${getString(R.string.anime_list)}"
                    } else {
                        toolbarLayout.title = "${viewModel.userData?.name} ${getString(R.string.manga_list)}"
                    }

                    viewModel.setSortedGroup()
                    handleList()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)

                    if (viewModel.currentList.isNullOrEmpty()) {
                        emptyLayout.visibility = View.VISIBLE
                        mediaListRecyclerView.visibility = View.GONE
                    } else {
                        emptyLayout.visibility = View.GONE
                        mediaListRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })

        if (!viewModel.sortedGroup.isNullOrEmpty()) {
            if (viewModel.mediaType == MediaType.ANIME) {
                toolbarLayout.title = "${viewModel.userData?.name} ${getString(R.string.anime_list)}"
            } else {
                toolbarLayout.title = "${viewModel.userData?.name} ${getString(R.string.manga_list)}"
            }
            handleList()
        } else {
            viewModel.retrieveMediaListCollection()
        }
    }

    private fun handleList() {
        val currentTab = viewModel.tabItemList[viewModel.selectedTab]
        toolbarLayout.subtitle = "${currentTab.status} (${currentTab.count})"

        viewModel.currentList.clear()
        viewModel.currentList.addAll(ArrayList(viewModel.sortedGroup.find { status -> status?.name == currentTab.status }?.entries ?: listOf()))

        handleSearch(searchView?.query.toString())

        if (viewModel.currentList.isNullOrEmpty()) {
            emptyLayout.visibility = View.VISIBLE
            mediaListRecyclerView.visibility = View.GONE
        } else {
            emptyLayout.visibility = View.GONE
            mediaListRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun initLayout() {
        mediaListRefreshLayout.setOnRefreshListener {
            mediaListRefreshLayout.isRefreshing = false
            viewModel.retrieveMediaListCollection()
        }

        mediaListRearrangeButton.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.getTabItemArray()) { _, which ->
                    viewModel.selectedTab = which

                    val currentTab = viewModel.tabItemList[viewModel.selectedTab]
                    toolbarLayout.subtitle = "${currentTab.status} (${currentTab.count})"

                    handleSearch(searchView?.query.toString())
                }
                .show()
        }

        mediaListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    mediaListRearrangeButton.hide()
                } else {
                    mediaListRearrangeButton.show()
                }
            }
        })

        if (viewModel.userId == Constant.EVA_ID) {
            GlideApp.with(this).load(R.drawable.eva_bg).into(mediaListBackgroundImage)
        } else {
            GlideApp.with(this).load(0).into(mediaListBackgroundImage)
        }
    }

    private fun handleSearch(query: String?) {
        viewModel.currentList.clear()
        var lastItemIsTitle = false
        val selectedList = viewModel.getSelectedList()
        selectedList.forEachIndexed { index, filtered ->
            if (filtered?.id == 0) {
                if (lastItemIsTitle) {
                    viewModel.currentList.removeAt(viewModel.currentList.lastIndex)
                }
                viewModel.currentList.add(filtered)
                lastItemIsTitle = true
            } else if (
                filtered?.media?.title?.romaji?.toLowerCase()?.contains(query?: "") == true ||
                filtered?.media?.title?.english?.toLowerCase()?.contains(query ?: "") == true ||
                filtered?.media?.title?.native_?.toLowerCase()?.contains(query ?: "") == true ||
                filtered?.media?.synonyms?.find { synonym -> synonym?.toLowerCase()?.contains(query ?: "") == true } != null
            ) {
                viewModel.currentList.add(filtered)
                lastItemIsTitle = false
            } else if (index == selectedList.lastIndex && lastItemIsTitle) {
                viewModel.currentList.removeAt(viewModel.currentList.lastIndex)
            }
        }
        adapter.notifyDataSetChanged()

        if (viewModel.currentList.isNullOrEmpty()) {
            emptyLayout.visibility = View.VISIBLE
            mediaListRecyclerView.visibility = View.GONE
        } else {
            emptyLayout.visibility = View.GONE
            mediaListRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun assignAdapter() {
        val scoreFormat = viewModel.userData?.mediaListOptions?.scoreFormat ?: ScoreFormat.POINT_100
        adapter = when {
            viewModel.mediaType == MediaType.ANIME && viewModel.listType == ListType.LINEAR -> {
                mediaListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                UserAnimeListRvAdapter(requireActivity(), viewModel.currentList, scoreFormat, viewModel.userId, viewModel.useRelativeDate, handleListAction())
            }
            viewModel.mediaType == MediaType.ANIME && viewModel.listType == ListType.GRID -> {
                mediaListRecyclerView.layoutManager = GridLayoutManager(activity, resources.getInteger(R.integer.gridSpan), GridLayoutManager.VERTICAL, false)
                UserAnimeListGridRvAdapter(requireActivity(), viewModel.currentList, scoreFormat, viewModel.userId, viewModel.useRelativeDate, handleListAction())
            }
            viewModel.mediaType == MediaType.MANGA && viewModel.listType == ListType.LINEAR -> {
                mediaListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                UserMangaListRvAdapter(requireActivity(), viewModel.currentList, scoreFormat, viewModel.userId, handleListAction())
            }
            viewModel.mediaType == MediaType.MANGA && viewModel.listType == ListType.GRID -> {
                mediaListRecyclerView.layoutManager = GridLayoutManager(activity, resources.getInteger(R.integer.gridSpan), GridLayoutManager.VERTICAL, false)
                UserMangaListGridRvAdapter(requireActivity(), viewModel.currentList, scoreFormat, viewModel.userId, handleListAction())
            }
            else -> {
                mediaListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                UserAnimeListRvAdapter(requireActivity(), viewModel.currentList, scoreFormat, viewModel.userId, viewModel.useRelativeDate, handleListAction())
            }
        }

        mediaListRecyclerView.adapter = adapter

        if (viewModel.listType == ListType.GRID) {
            (mediaListRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == UserAnimeListGridRvAdapter.VIEW_TYPE_TITLE) {
                        resources.getInteger(R.integer.gridSpan)
                    } else {
                        1
                    }
                }
            }
        }
    }

    private fun handleListAction() = object : UserMediaListener {
        override fun openSelectedMedia(mediaId: Int, mediaType: MediaType) {
            listener?.changeFragment(BrowsePage.valueOf(mediaType.name), mediaId)
        }

        override fun viewMediaListDetail(mediaListId: Int) {
            val entry = viewModel.currentList.find { it?.id == mediaListId } ?: return
            val mediaList = MediaList(
                id = entry.id,
                status = entry.status,
                score = entry.score,
                progress = entry.progress,
                progressVolumes = entry.progressVolumes,
                repeat = entry.repeat,
                priority = entry.priority,
                private = entry.private_,
                notes = entry.notes,
                hiddenFromStatusList = entry.hiddenFromStatusLists,
                customLists = entry.customLists,
                advancedScores = entry.customLists,
                startedAt = if (entry.startedAt != null) Converter.convertFuzzyDate(entry.startedAt) else null,
                completedAt = if (entry.completedAt != null) Converter.convertFuzzyDate(entry.completedAt) else null,
                updatedAt = entry.updatedAt,
                createdAt = entry.createdAt,
                media = Media(
                    id = entry.media?.id!!,
                    title = MediaTitle(userPreferred = entry.media.title?.userPreferred!!),
                    type = entry.media.type,
                    format = entry.media.format
                )
            )

            val dialog = MediaListDetailDialog()
            val bundle = Bundle()
            bundle.putString(MediaListDetailDialog.MEDIA_LIST_ITEM, viewModel.gson.toJson(mediaList))
            bundle.putString(MediaListDetailDialog.SCORE_FORMAT, viewModel.userData?.mediaListOptions?.scoreFormat?.name)
            // advancedScoringEnabled is the same in animeList and mangaList
            bundle.putBoolean(MediaListDetailDialog.USE_ADVANCED_SCORES, viewModel.userData?.mediaListOptions?.animeList?.advancedScoringEnabled == true)
            dialog.arguments = bundle
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MediaFilterActivity.ACTIVITY_FILTER && resultCode == Activity.RESULT_OK) {
            val filterData = if (data?.extras != null && data.extras?.getString(MediaFilterActivity.FILTER_DATA) != null) {
                viewModel.gson.fromJson(data.extras?.getString(MediaFilterActivity.FILTER_DATA), MediaFilterData::class.java)
            } else {
                null
            }
            viewModel.filterData = filterData
            viewModel.setSortedGroup()
            handleList()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaListRecyclerView.adapter = null
        itemCustomiseList = null
        itemFilter = null
        itemSearch = null
        searchView = null
    }
}

package it.matteoleggio.alchan.ui.browse.studio


import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.pojo.StudioMedia
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import kotlinx.android.synthetic.main.fragment_studio.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 */
class StudioFragment : BaseFragment() {

    private val viewModel by viewModel<StudioViewModel>()

    private lateinit var scaleUpAnim: Animation
    private lateinit var scaleDownAnim: Animation

    private lateinit var adapter: StudioMediaRvAdapter
    private var isLoading = false

    private var itemOpenAniList: MenuItem? = null
    private var itemCopyLink: MenuItem? = null

    companion object {
        const val STUDIO_ID = "studioId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_studio, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        studioToolbar.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        viewModel.studioId = arguments?.getInt(STUDIO_ID)
        scaleUpAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_up)
        scaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)

        studioToolbar.setNavigationOnClickListener { activity?.finish() }
        studioToolbar.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete)
        studioToolbar.inflateMenu(R.menu.menu_anilist_link)
        itemOpenAniList = studioToolbar.menu.findItem(R.id.itemOpenAnilist)
        itemCopyLink = studioToolbar.menu.findItem(R.id.itemCopyLink)

        adapter = assignAdapter()
        studioRecyclerView.adapter = adapter

        setupObserver()
        initLayout()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkStudioIsFavorite()
    }

    private fun assignAdapter(): StudioMediaRvAdapter {
        return StudioMediaRvAdapter(requireActivity(), viewModel.studioMediaList, object : StudioMediaRvAdapter.StudioMediaListener {
            override fun passSelectedMedia(mediaId: Int, mediaType: MediaType) {
                listener?.changeFragment(BrowsePage.valueOf(mediaType.name), mediaId)
            }
        })
    }

    private fun setupObserver() {
        viewModel.studioData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (it?.data?.studio?.id != viewModel.studioId) {
                        return@Observer
                    }

                    if (it.data?.studio != null) {
                        viewModel.currentStudioData = it.data.studio
                        setupHeader()
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.studioIsFavoriteData.observe(viewLifecycleOwner, Observer {
            if (it?.data?.studio?.id != viewModel.studioId) {
                return@Observer
            }

            if (it.responseStatus == ResponseStatus.SUCCESS) {
                if (it.data?.studio?.isFavourite == true) {
                    studioFavoriteButton.text = getString(R.string.favorited)
                    studioFavoriteButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    studioFavoriteButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
                    studioFavoriteButton.strokeColor = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    studioFavoriteButton.strokeWidth = 2
                } else {
                    studioFavoriteButton.text = getString(R.string.set_as_favorite)
                    studioFavoriteButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeBackgroundColor))
                    studioFavoriteButton.backgroundTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    studioFavoriteButton.strokeWidth = 0
                }

                studioFavoriteButton.isEnabled = true
            }
        })

        viewModel.toggleFavouriteResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    viewModel.checkStudioIsFavorite()
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, R.string.change_saved)
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.studioMediaData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it.data?.studio?.id != viewModel.studioId) {
                        return@Observer
                    }

                    if (isLoading) {
                        viewModel.studioMediaList.removeAt(viewModel.studioMediaList.lastIndex)
                        adapter.notifyItemRemoved(viewModel.studioMediaList.size)
                        isLoading = false
                    }

                    if (!viewModel.hasNextPage) {
                        return@Observer
                    }

                    viewModel.hasNextPage = it.data?.studio?.media?.pageInfo?.hasNextPage ?: false
                    viewModel.page += 1
                    viewModel.isInit = true

                    it.data?.studio?.media?.edges?.forEach { edge ->
                        val staffMedia = StudioMedia(
                            edge?.node?.id,
                            edge?.node?.title?.userPreferred,
                            edge?.node?.type,
                            edge?.node?.format,
                            edge?.node?.coverImage?.large
                        )
                        viewModel.studioMediaList.add(staffMedia)
                    }

                    adapter.notifyDataSetChanged()
                    emptyLayout.visibility = if (viewModel.studioMediaList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    if (isLoading) {
                        viewModel.studioMediaList.removeAt(viewModel.studioMediaList.lastIndex)
                        adapter.notifyItemRemoved(viewModel.studioMediaList.size)
                        isLoading = false
                    }
                    emptyLayout.visibility = if (viewModel.studioMediaList.isNullOrEmpty()) View.VISIBLE else View.GONE
                    if (!viewModel.isInit) {
                        retryButton.visibility = View.VISIBLE
                        retryButton.setOnClickListener { viewModel.getStudioMedia() }
                    } else {
                        retryButton.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.getStudio()
        if (!viewModel.isInit) {
            viewModel.getStudioMedia()
        }
    }

    private fun setupHeader() {
        studioNameText.text = viewModel.currentStudioData?.name
        studioFavoriteCountText.text = viewModel.currentStudioData?.favourites?.toString()

        itemOpenAniList?.isVisible = true
        itemCopyLink?.isVisible = true

        itemOpenAniList?.setOnMenuItemClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(requireActivity(), Uri.parse(viewModel.currentStudioData?.siteUrl))
            true
        }

        itemCopyLink?.setOnMenuItemClickListener {
            AndroidUtility.copyToClipboard(activity, viewModel.currentStudioData?.siteUrl!!)
            DialogUtility.showToast(activity, R.string.link_copied)
            true
        }
    }

    private fun initLayout() {
        studioRefreshLayout.setOnRefreshListener {
            studioRefreshLayout.isRefreshing = false
            viewModel.getStudio()
            viewModel.checkStudioIsFavorite()
            if (!viewModel.isInit) {
                viewModel.getStudioMedia()
            }
        }

        studioAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // disable refresh when toolbar is not fully expanded
            studioRefreshLayout?.isEnabled = verticalOffset == 0

            // 50 is magic number gotten from trial and error
            if (abs(verticalOffset) - appBarLayout.totalScrollRange >= -50) {
                if (studioBannerContentLayout?.isVisible == true) {
                    studioBannerContentLayout?.startAnimation(scaleDownAnim)
                    studioBannerContentLayout?.visibility = View.INVISIBLE
                }
            } else {
                if (studioBannerContentLayout?.isInvisible == true) {
                    studioBannerContentLayout?.startAnimation(scaleUpAnim)
                    studioBannerContentLayout?.visibility = View.VISIBLE
                }
            }
        })

        studioFavoriteButton.setOnClickListener {
            viewModel.updateFavorite()
        }

        studioRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1) && viewModel.isInit && !isLoading) {
                    loadMore()
                    isLoading = true
                }
            }
        })

        studioMediaSortText.text = viewModel.mediaSortArray[viewModel.mediaSortIndex]

        studioMediaSortText.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.mediaSortArray) { _, which ->
                    viewModel.mediaSortIndex = which
                    studioMediaSortText.text = viewModel.mediaSortArray[viewModel.mediaSortIndex]

                    isLoading = false
                    viewModel.studioMediaList.clear()
                    viewModel.page = 1
                    viewModel.hasNextPage = true
                    viewModel.getStudioMedia()
                }
                .show()
        }
    }

    private fun loadMore() {
        if (viewModel.hasNextPage) {
            viewModel.studioMediaList.add(null)
            adapter.notifyItemInserted(viewModel.studioMediaList.lastIndex)
            viewModel.getStudioMedia()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        studioRecyclerView.adapter = null
        itemOpenAniList = null
        itemCopyLink = null
    }
}

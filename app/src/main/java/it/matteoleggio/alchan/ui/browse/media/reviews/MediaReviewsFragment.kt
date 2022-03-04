package it.matteoleggio.alchan.ui.browse.media.reviews


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import it.matteoleggio.alchan.ui.browse.reviews.editor.ReviewEditorActivity
import kotlinx.android.synthetic.main.fragment_media_reviews.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class MediaReviewsFragment : BaseFragment() {

    private val viewModel by viewModel<MediaReviewsViewModel>()

    private lateinit var adapter: MediaReviewsRvAdapter
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_reviews, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.mediaId = arguments?.getInt(MediaFragment.MEDIA_ID)
        adapter = assignAdapter()
        reviewsRecyclerView.adapter = adapter

        initLayout()
        setupObserver()
    }

    private fun assignAdapter(): MediaReviewsRvAdapter {
        return MediaReviewsRvAdapter(requireActivity(), viewModel.mediaReviews, object : MediaReviewsRvAdapter.MediaReviewsListener {
            override fun passSelectedReview(reviewId: Int) {
                listener?.changeFragment(BrowsePage.REVIEW, reviewId)
            }
        })
    }

    private fun setupObserver() {
        viewModel.mediaReviewsData.observe(viewLifecycleOwner, Observer {
            loadingLayout.visibility = View.GONE
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it.data?.media?.id != viewModel.mediaId) {
                        return@Observer
                    }

                    if (isLoading) {
                        viewModel.mediaReviews.removeAt(viewModel.mediaReviews.lastIndex)
                        adapter.notifyItemRemoved(viewModel.mediaReviews.size)
                        isLoading = false
                    }

                    if (!viewModel.hasNextPage) {
                        return@Observer
                    }

                    viewModel.hasNextPage = it.data?.media?.reviews?.pageInfo?.hasNextPage ?: false
                    viewModel.page += 1
                    viewModel.isInit = true

                    it.data?.media?.reviews?.edges?.forEach { edge ->
                        viewModel.mediaReviews.add(edge?.node)
                    }

                    adapter.notifyDataSetChanged()
                    emptyLayout.visibility = if (viewModel.mediaReviews.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    if (isLoading) {
                        viewModel.mediaReviews.removeAt(viewModel.mediaReviews.lastIndex)
                        adapter.notifyItemRemoved(viewModel.mediaReviews.size)
                        isLoading = false
                    }

                    emptyLayout.visibility = if (viewModel.mediaReviews.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.triggerMediaReview.observe(viewLifecycleOwner, Observer {
            isLoading = false
            viewModel.refresh()
        })

        if (!viewModel.isInit) {
            viewModel.getMediaReviews()
            loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun initLayout() {
        reviewSortText.text = viewModel.sortReviewArray[viewModel.sortReviewList.indexOf(viewModel.selectedSort)]

        reviewSortText.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setItems(viewModel.sortReviewArray) { _, which ->
                    viewModel.selectedSort = viewModel.sortReviewList[which]
                    reviewSortText.text = viewModel.sortReviewArray[which]

                    isLoading = false
                    viewModel.page = 1
                    viewModel.hasNextPage = true
                    viewModel.mediaReviews.clear()
                    adapter.notifyDataSetChanged()
                    viewModel.getMediaReviews()
                    loadingLayout.visibility = View.VISIBLE
                }
                .show()
        }

        reviewsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1) && viewModel.isInit && !isLoading) {
                    loadMore()
                    isLoading = true
                }
            }
        })

        newReviewText.setOnClickListener {
            val intent = Intent(activity, ReviewEditorActivity::class.java)
            intent.putExtra(ReviewEditorActivity.MEDIA_ID, viewModel.mediaId)
            startActivity(intent)
        }
    }

    private fun loadMore() {
        if (viewModel.hasNextPage) {
            viewModel.mediaReviews.add(null)
            adapter.notifyItemInserted(viewModel.mediaReviews.lastIndex)
            viewModel.getMediaReviews()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reviewsRecyclerView.adapter = null
    }
}

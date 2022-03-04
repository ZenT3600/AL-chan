package it.matteoleggio.alchan.ui.browse.reviews


import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.secondsToDate
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.reviews.editor.ReviewEditorActivity
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_reviews_reader.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import type.ReviewRating
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 */
class ReviewsReaderFragment : BaseFragment() {

    private val viewModel by viewModel<ReviewsReaderViewModel>()

    private lateinit var markwon: Markwon
    private var maxWidth = 0

    private var itemOpenAniList: MenuItem? = null
    private var itemCopyLink: MenuItem? = null
    private var itemEdit: MenuItem? = null

    companion object {
        const val REVIEW_ID = "reviewId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reviews_reader, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.reviewId = arguments?.getInt(REVIEW_ID)

        markwon = AndroidUtility.initMarkwon(requireActivity())
        maxWidth = AndroidUtility.getScreenWidth(requireActivity())

        reviewToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        reviewToolbar.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_arrow_back)
        reviewToolbar.inflateMenu(R.menu.menu_review)

        itemEdit = reviewToolbar.menu.findItem(R.id.itemEdit)
        itemOpenAniList = reviewToolbar.menu.findItem(R.id.itemOpenAnilist)
        itemCopyLink = reviewToolbar.menu.findItem(R.id.itemCopyLink)

        reviewToolbar.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.reviewDetailData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (it?.data?.review?.id != viewModel.reviewId) {
                        return@Observer
                    }

                    viewModel.reviewDetail = it.data?.review
                    viewModel.currentUserRating = viewModel.reviewDetail?.userRating
                    viewModel.currentRating = viewModel.reviewDetail?.rating
                    viewModel.currentRatingAmount = viewModel.reviewDetail?.ratingAmount
                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    activity?.onBackPressed()
                }
            }
        })

        viewModel.rateReviewResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.currentUserRating = it.data?.rateReview?.userRating
                    viewModel.currentRating = it.data?.rateReview?.rating
                    viewModel.currentRatingAmount = it.data?.rateReview?.ratingAmount

                    handleLike()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        if (viewModel.reviewDetail == null) {
            viewModel.getReviewDetail()
        }
    }

    private fun initLayout() {
        reviewRefreshLayout.setOnRefreshListener {
            reviewRefreshLayout.isRefreshing = false
            viewModel.getReviewDetail()
        }

        if (viewModel.reviewDetail == null) {
            return
        }

        val review = viewModel.reviewDetail!!

        GlideApp.with(this).load(review.media?.bannerImage).into(mediaBannerImage)

        reviewMediaTypeText.text = getString(R.string.a_review, review.media?.type?.name?.toLowerCase()?.capitalize())

        reviewTitleText.text = getString(R.string.review_of_by, review.media?.title?.userPreferred, review.user?.name)
        reviewTitleText.setOnClickListener {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.open_media_page,
                getString(R.string.do_you_want_to_open_a_page, review.media?.title?.userPreferred),
                R.string.open_media_page,
                {
                    listener?.changeFragment(BrowsePage.valueOf(review.media?.type?.name ?: MediaType.ANIME.name), review.mediaId)
                },
                R.string.cancel,
                { }
            )
        }

        reviewSummaryText.text = review.summary

        GlideApp.with(this).load(review.user?.avatar?.medium).apply(RequestOptions.circleCropTransform()).into(avatarImage)
        avatarImage.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, review.userId)
        }
        avatarLayout.visibility = View.VISIBLE

        reviewUsernameText.text = review.user?.name
        reviewUsernameText.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, review.userId)
        }
        reviewDateText.text = review.createdAt.secondsToDate()

        AndroidUtility.convertMarkdown(requireActivity(), reviewText, review.body, maxWidth, markwon)

        reviewAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // disable refresh when toolbar is not fully expanded
            reviewRefreshLayout?.isEnabled = verticalOffset == 0
        })

        scoreText.text = "${review.score}/100"

        try {
            val nearestTen = (round(review.score!! / 10.0) * 10).toInt()
            scoreLayout.setCardBackgroundColor(Constant.SCORE_COLOR_MAP[nearestTen]!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        reviewThumbsUpCard.setOnClickListener {
            if (viewModel.currentUserRating != ReviewRating.UP_VOTE) {
                viewModel.rateReview(ReviewRating.UP_VOTE)
            } else {
                viewModel.rateReview(ReviewRating.NO_VOTE)
            }
        }

        reviewThumbsDownCard.setOnClickListener {
            if (viewModel.currentUserRating != ReviewRating.DOWN_VOTE) {
                viewModel.rateReview(ReviewRating.DOWN_VOTE)
            } else {
                viewModel.rateReview(ReviewRating.NO_VOTE)
            }
        }

        handleLike()

        itemCopyLink?.isVisible = true
        itemOpenAniList?.isVisible = true

        itemCopyLink?.setOnMenuItemClickListener {
            if (viewModel.reviewDetail?.siteUrl == null) {
                DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            } else {
                AndroidUtility.copyToClipboard(activity, viewModel.reviewDetail?.siteUrl!!)
                DialogUtility.showToast(activity, R.string.link_copied)
            }
            true
        }

        itemOpenAniList?.setOnMenuItemClickListener {
            if (viewModel.reviewDetail?.siteUrl == null) {
                DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            } else {
                CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(viewModel.reviewDetail?.siteUrl))
            }
            true
        }

        itemEdit?.isVisible = viewModel.reviewDetail?.userId == viewModel.userId

        itemEdit?.setOnMenuItemClickListener {
            val intent = Intent(activity, ReviewEditorActivity::class.java)
            intent.putExtra(ReviewEditorActivity.REVIEW_ID, viewModel.reviewDetail?.id)
            intent.putExtra(ReviewEditorActivity.MEDIA_ID, viewModel.reviewDetail?.mediaId)
            startActivityForResult(intent, ReviewEditorActivity.ACTIVITY_EDITOR)
            true
        }
    }

    private fun handleLike() {
        reviewThumbsUpIcon.imageTintList = if (viewModel.currentUserRating == ReviewRating.UP_VOTE) {
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.green))
        } else {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(requireActivity(), R.attr.themeContentColor))
        }

        reviewThumbsDownIcon.imageTintList = if (viewModel.currentUserRating == ReviewRating.DOWN_VOTE) {
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.red))
        } else {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(requireActivity(), R.attr.themeContentColor))
        }

        reviewLikeText.text = if (viewModel.currentRatingAmount == 1) {
            getString(R.string.out_of_user_liked_this_review, viewModel.currentRating ?: 0, viewModel.currentRatingAmount ?: 0)
        } else {
            getString(R.string.out_of_users_liked_this_review, viewModel.currentRating ?: 0, viewModel.currentRatingAmount ?: 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == ReviewEditorActivity.ACTIVITY_EDITOR && resultCode == Activity.RESULT_OK) {
//            if (data?.extras?.getBoolean(ReviewEditorActivity.IS_DELETE) == true) {
//                activity?.onBackPressed()
//            } else {
//                viewModel.getReviewDetail()
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ReviewEditorActivity.ACTIVITY_EDITOR && resultCode == Activity.RESULT_OK) {
//            if (data?.extras?.getBoolean(ReviewEditorActivity.IS_DELETE) == true) {
//                activity?.onBackPressed()
//            } else {
                viewModel.getReviewDetail()
//            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemEdit = null
        itemOpenAniList = null
        itemCopyLink = null
    }
}

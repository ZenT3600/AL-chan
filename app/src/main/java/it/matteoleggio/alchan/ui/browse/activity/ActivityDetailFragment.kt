package it.matteoleggio.alchan.ui.browse.activity


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
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.*
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.EditorType
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.ListActivity
import it.matteoleggio.alchan.helper.pojo.MessageActivity
import it.matteoleggio.alchan.helper.pojo.TextActivity
import it.matteoleggio.alchan.helper.replaceUnderscore
import it.matteoleggio.alchan.helper.secondsToDateTime
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.common.LikesDialog
import it.matteoleggio.alchan.ui.common.TextEditorActivity
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_activity_detail.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.LikeableType

/**
 * A simple [Fragment] subclass.
 */
class ActivityDetailFragment : BaseFragment() {

    private val viewModel by viewModel<ActivityDetailViewModel>()

    private lateinit var likesRvAdapter: ActivityLikesRvAdapter
    private lateinit var repliesRvAdapter: ActivityRepliesRvAdapter

    private var maxWidth = 0
    private lateinit var markwon: Markwon

    companion object {
        const val ACTIVITY_ID = "activityId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        viewModel.activityId = arguments?.getInt(ACTIVITY_ID)

        maxWidth = AndroidUtility.getScreenWidth(activity)
        markwon = AndroidUtility.initMarkwon(requireActivity())

        toolbarLayout.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarLayout.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_arrow_back)
        toolbarLayout.inflateMenu(R.menu.menu_activity_detail)
        toolbarLayout.menu.findItem(R.id.itemReply).setOnMenuItemClickListener {
            val intent = Intent(activity, TextEditorActivity::class.java)
            intent.putExtra(TextEditorActivity.EDITOR_TYPE, EditorType.ACTIVITY_REPLY.name)
            intent.putExtra(TextEditorActivity.ACTIVITY_ID, viewModel.activityId)
            startActivityForResult(intent, EditorType.ACTIVITY_REPLY.ordinal)
            true
        }

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.activityDetailResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    val act = it.data?.activity
                    val replies = viewModel.getReplies(act?.__typename, act?.fragments)
                    val likes = viewModel.getLikes(act?.__typename, act?.fragments)

                    viewModel.activityDetail = when (act?.__typename) {
                        viewModel.textActivityText -> {
                            val item = act.fragments.onTextActivity
                            if (item?.user?.id == null) {
                                null
                            } else {
                                val user = User(id = item.user.id, name = item.user.name, avatar = UserAvatar(null, item.user.avatar?.medium))
                                TextActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, replies, likes, item.userId, item.text, user)
                            }
                        }
                        viewModel.listActivityText -> {
                            val item = act.fragments.onListActivity
                            if (item?.media?.id == null || item.user?.id == null) {
                                null
                            } else {
                                val media = Media(id = item.media.id, title = MediaTitle(userPreferred = item.media.title?.userPreferred!!), coverImage = MediaCoverImage(null, item.media.coverImage?.medium), type = item.media.type, format = item.media.format, startDate = FuzzyDate(item.media.startDate?.year, item.media.startDate?.month, item.media.startDate?.day))
                                val user = User(id = item.user.id, name = item.user.name, avatar = UserAvatar(null, item.user.avatar?.medium))
                                ListActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, replies, likes, item.userId, item.status, item.progress, media, user)
                            }
                        }
                        viewModel.messageActivityText -> {
                            val item = act.fragments.onMessageActivity
                            if (item?.recipient?.id == null || item.messenger?.id == null) {
                                null
                            } else {
                                val recipient = User(id = item.recipient.id, name = item.recipient.name, avatar = UserAvatar(null, item.recipient.avatar?.medium))
                                val messenger = User(id = item.messenger.id, name = item.messenger.name, avatar = UserAvatar(null, item.messenger.avatar?.medium))
                                MessageActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, replies, likes, item.recipientId, item.messengerId, item.message, item.isPrivate, recipient, messenger)
                            }
                        }
                        else -> null
                    }

                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.toggleLikeDetailResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (it.data?.id == viewModel.activityId) {
                        viewModel.activityDetail?.isLiked = it.data?.isLiked
                        viewModel.activityDetail?.likeCount = it.data?.likeCount ?: 0
                        viewModel.activityDetail?.likes = it.data?.likes

                        activityLikeIcon.imageTintList = if (viewModel.activityDetail?.isLiked == true) {
                            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                        } else {
                            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
                        }
                        activityLikeText.text = if (viewModel.activityDetail?.likeCount != 0) viewModel.activityDetail?.likeCount.toString() else ""

                        likesRvAdapter = assignLikesAdapter()
                        likesRecyclerView.adapter = likesRvAdapter
                    } else {
                        val findActivity = viewModel.activityDetail?.replies?.find { item -> item.id == it.data?.id }
                        val activityIndex = viewModel.activityDetail?.replies?.indexOf(findActivity)
                        if (activityIndex != null && activityIndex != -1) {
                            viewModel.activityDetail?.replies!![activityIndex].isLiked = it.data?.isLiked
                            viewModel.activityDetail?.replies!![activityIndex].likeCount = it.data?.likeCount ?: 0
                            viewModel.activityDetail?.replies!![activityIndex].likes = it.data?.likes
                            repliesRvAdapter.notifyItemChanged(activityIndex)
                        }
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.toggleActivitySubscriptionDetailResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.activityDetail?.isSubscribed = it.data?.isSubscribed
                    activitySubscribeIcon.imageTintList = if (viewModel.activityDetail?.isSubscribed == true) {
                        ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    } else {
                        ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.deleteActivityDetailResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    activity?.onBackPressed()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.deleteActivityReplyResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    val findReply = viewModel.activityDetail?.replies?.find { reply -> reply.id == it.data }
                    val findIndex = viewModel.activityDetail?.replies?.indexOf(findReply)

                    if (findIndex != null && findIndex != -1) {
                        viewModel.activityDetail?.replies?.removeAt(findIndex)
                        viewModel.activityDetail?.replyCount = viewModel.activityDetail?.replies?.size ?: 0
                        activityReplyText.text = if (viewModel.activityDetail?.replyCount != 0) viewModel.activityDetail?.replyCount.toString() else ""
                        repliesRvAdapter.notifyItemRemoved(findIndex)
                        viewModel.notifyAllActivityList()
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        if (viewModel.activityDetail == null) {
            viewModel.getActivityDetail()
        }
    }

    private fun initLayout() {
        activityDetailRefreshLayout.setOnRefreshListener {
            activityDetailRefreshLayout.isRefreshing = false
            viewModel.getActivityDetail()
        }

        if (viewModel.activityDetail == null) {
            return
        }

        val act = viewModel.activityDetail!!

        activityListLayout.visibility = View.GONE
        activityTextLayout.visibility = View.GONE

        timeText.text = act.createdAt.secondsToDateTime()

        activityReplyText.text = if (act.replyCount != 0) act.replyCount.toString() else ""
        activityReplyLayout.setOnClickListener {
            val intent = Intent(activity, TextEditorActivity::class.java)
            intent.putExtra(TextEditorActivity.EDITOR_TYPE, EditorType.ACTIVITY_REPLY.name)
            intent.putExtra(TextEditorActivity.ACTIVITY_ID, viewModel.activityId)
            startActivityForResult(intent, EditorType.ACTIVITY_REPLY.ordinal)
        }

        activityLikeIcon.imageTintList = if (act.isLiked == true) {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
        } else {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
        }
        activityLikeText.text = if (act.likeCount != 0) act.likeCount.toString() else ""
        activityLikeLayout.setOnClickListener {
            if (viewModel.activityId != null) {
                viewModel.toggleLike(viewModel.activityId!!, LikeableType.ACTIVITY)
            }
        }

        activitySubscribeIcon.imageTintList = if (act.isSubscribed == true) {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
        } else {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeContentColor))
        }
        activitySubscribeLayout.setOnClickListener {
            if (viewModel.activityId != null) {
                viewModel.toggleSubscription(viewModel.activityId!!, viewModel.activityDetail?.isSubscribed != true)
            }
        }

        activityMoreLayout.setOnClickListener {
            // view pop up menu (edit, delete, view in anilist, copy link)
            val wrapper = ContextThemeWrapper(requireActivity(), R.style.PopupTheme)
            val popupMenu = PopupMenu(wrapper, it)
            popupMenu.menuInflater.inflate(R.menu.menu_activity, popupMenu.menu)


            popupMenu.menu.apply {
                findItem(R.id.itemEdit).isVisible = (act is TextActivity && act.userId == viewModel.userId) ||
                        (act is MessageActivity && act.messengerId == viewModel.userId)
                findItem(R.id.itemDelete).isVisible = (act is TextActivity && act.userId == viewModel.userId) ||
                        (act is ListActivity && act.userId == viewModel.userId) ||
                        (act is MessageActivity && (act.recipientId == viewModel.userId || act.messengerId == viewModel.userId))
            }

            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem? ->
                when (menuItem?.itemId) {
                    R.id.itemEdit -> {
                        editActivity(
                            act.id,
                            when (act) {
                                is TextActivity -> act.text ?: ""
                                is MessageActivity -> act.message ?: ""
                                else -> ""
                            },
                            if (act is MessageActivity) act.recipientId else null,
                            if (act is MessageActivity) act.recipient?.name else null
                        )
                    }
                    R.id.itemDelete -> deleteActivity(act.id)
                    R.id.itemViewOnAniList -> viewOnAniList(act.siteUrl)
                    R.id.itemCopyLink -> copyLink(act.siteUrl)
                }
                true
            }
            popupMenu.show()
        }

        if (viewModel.activityDetail?.likes.isNullOrEmpty()) {
            likesLayout.visibility = View.GONE
        } else {
            likesLayout.visibility = View.VISIBLE
        }

        likesRvAdapter = assignLikesAdapter()
        likesRecyclerView.adapter = likesRvAdapter

        if (viewModel.activityDetail?.replies.isNullOrEmpty()) {
            activityRepliesRecyclerView.visibility = View.GONE
        } else {
            activityRepliesRecyclerView.visibility = View.VISIBLE
        }

        repliesRvAdapter = assignRepliesAdapter()
        activityRepliesRecyclerView.adapter = repliesRvAdapter

        when (act) {
            is TextActivity -> handleTextActivityLayout(act)
            is ListActivity -> handleListActivityLayout(act)
            is MessageActivity -> handleMessageActivityLayout(act)
        }
    }

    private fun handleTextActivityLayout(act: TextActivity) {
        nameText.text = act.user?.name
        nameText.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.userId!!)
        }

        GlideApp.with(this).load(act.user?.avatar?.medium).apply(RequestOptions.circleCropTransform()).into(avatarImage)
        avatarImage.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.userId!!)
        }

        AndroidUtility.convertMarkdown(requireActivity(), activityTextLayout, act.text, maxWidth, markwon)

        activityListLayout.visibility = View.GONE
        activityTextLayout.visibility = View.VISIBLE
        recipientLayout.visibility = View.GONE
        privateLayout.visibility = View.GONE
    }

    private fun handleListActivityLayout(act: ListActivity) {
        nameText.text = act.user?.name
        nameText.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.userId!!)
        }

        GlideApp.with(this).load(act.user?.avatar?.medium).apply(RequestOptions.circleCropTransform()).into(avatarImage)
        avatarImage.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.userId!!)
        }

        listActivityText.text = "${act.status?.capitalize()}${if (act.progress != null) " ${act.progress} of " else " "}${act.media?.title?.userPreferred}"

        mediaTitleText.text = act.media?.title?.userPreferred

        GlideApp.with(this).load(act.media?.coverImage?.medium).into(mediaImage)

        mediaYearText.text = if (act.media?.startDate?.year != null) {
            act.media.startDate.year.toString()
        } else {
            "TBA"
        }

        mediaTypeText.text = act.media?.type?.name

        if (act.media?.format != null) {
            mediaFormatDivider.visibility = View.VISIBLE
            mediaFormatText.text = act.media.format.name.replaceUnderscore()
        } else {
            mediaFormatDivider.visibility = View.GONE
            mediaFormatText.text = ""
        }

        mediaLayout.setOnClickListener {
            listener?.changeFragment(BrowsePage.valueOf(act.media?.type!!.name), act.media.id)
        }

        activityListLayout.visibility = View.VISIBLE
        activityTextLayout.visibility = View.GONE
        recipientLayout.visibility = View.GONE
        privateLayout.visibility = View.GONE
    }

    private fun handleMessageActivityLayout(act: MessageActivity) {
        nameText.text = act.messenger?.name ?: ""
        nameText.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.messengerId!!)
        }
        GlideApp.with(this).load(act.messenger?.avatar?.medium).apply(RequestOptions.circleCropTransform()).into(avatarImage)
        avatarImage.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.messengerId!!)
        }

        recipientLayout.visibility = View.VISIBLE
        recipientNameText.text = act.recipient?.name ?: ""
        recipientNameText.setOnClickListener {
            listener?.changeFragment(BrowsePage.USER, act.recipientId!!)
        }

        privateLayout.visibility = if (act.isPrivate == true) View.VISIBLE else View.GONE

        AndroidUtility.convertMarkdown(requireActivity(), activityTextLayout, act.message, maxWidth, markwon)

        activityListLayout.visibility = View.GONE
        activityTextLayout.visibility = View.VISIBLE
        recipientLayout.visibility = View.VISIBLE
    }

    private fun editActivity(activityId: Int, text: String, recipientId: Int?, recipientName: String?) {
        val intent = Intent(activity, TextEditorActivity::class.java)
        intent.putExtra(TextEditorActivity.ACTIVITY_ID, activityId)
        intent.putExtra(TextEditorActivity.TEXT_CONTENT, text)
        intent.putExtra(TextEditorActivity.RECIPIENT_ID, recipientId)
        intent.putExtra(TextEditorActivity.RECIPIENT_NAME, recipientName)
        startActivityForResult(intent, EditorType.ACTIVITY.ordinal)
    }

    private fun deleteActivity(activityId: Int) {
        if (viewModel.activityId != null) {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.delete_activity,
                R.string.are_you_sure_you_want_to_delete_this_activity,
                R.string.delete,
                {
                    viewModel.deleteActivity(activityId)
                },
                R.string.cancel,
                { }
            )
        }
    }

    private fun viewOnAniList(siteUrl: String?) {
        if (siteUrl.isNullOrBlank()) {
            DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            return
        }

        CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(siteUrl))
    }

    private fun copyLink(siteUrl: String?) {
        if (siteUrl.isNullOrBlank()) {
            DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            return
        }

        AndroidUtility.copyToClipboard(activity, siteUrl)
        DialogUtility.showToast(activity, R.string.link_copied)
    }

    private fun assignLikesAdapter(): ActivityLikesRvAdapter {
        return ActivityLikesRvAdapter(
            requireActivity(),
            viewModel.activityDetail?.likes ?: listOf(),
            maxWidth / resources.getInteger(R.integer.horizontalListLikeDivider),
            object : ActivityLikesRvAdapter.ActivityLikesListener {
                override fun openUserPage(userId: Int) {
                    listener?.changeFragment(BrowsePage.USER, userId)
                }

                override fun showUsername(name: String) {
                    DialogUtility.showToast(activity, name)
                }
            }
        )
    }

    private fun assignRepliesAdapter(): ActivityRepliesRvAdapter {
        return ActivityRepliesRvAdapter(
            requireActivity(),
            viewModel.activityDetail?.replies ?: listOf(),
            viewModel.userId,
            maxWidth,
            markwon,
            object : ActivityRepliesRvAdapter.ActivityRepliesListener {
                override fun openUserPage(userId: Int) {
                    listener?.changeFragment(BrowsePage.USER, userId)
                }

                override fun editReply(replyId: Int, text: String) {
                    val intent = Intent(activity, TextEditorActivity::class.java)
                    intent.putExtra(TextEditorActivity.EDITOR_TYPE, EditorType.ACTIVITY_REPLY.name)
                    intent.putExtra(TextEditorActivity.REPLY_ID, replyId)
                    intent.putExtra(TextEditorActivity.ACTIVITY_ID, viewModel.activityId)
                    intent.putExtra(TextEditorActivity.TEXT_CONTENT, text)
                    startActivityForResult(intent, EditorType.ACTIVITY_REPLY.ordinal)
                }

                override fun deleteReply(replyId: Int) {
                    viewModel.deleteActivityReply(replyId)
                }

                override fun likeReply(replyId: Int) {
                    viewModel.toggleLike(replyId, LikeableType.ACTIVITY_REPLY)
                }

                override fun showLikes(likes: List<User>) {
                    val dialog = LikesDialog()
                    dialog.setListener(object : LikesDialog.LikesDialogListener {
                        override fun passSelectedUser(userId: Int) {
                            listener?.changeFragment(BrowsePage.USER, userId)
                        }
                    })
                    val bundle = Bundle()
                    bundle.putString(LikesDialog.USER_LIST, viewModel.gson.toJson(likes))
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, null)
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == EditorType.ACTIVITY.ordinal || requestCode == EditorType.ACTIVITY_REPLY.ordinal) && resultCode == Activity.RESULT_OK) {
            viewModel.getActivityDetail()
            viewModel.notifyAllActivityList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        likesRecyclerView.adapter = null
        activityRepliesRecyclerView.adapter = null
    }
}

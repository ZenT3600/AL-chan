package it.matteoleggio.alchan.ui.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.NotificationCategory
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.secondsToDateTime
import fragment.*
import kotlinx.android.synthetic.main.list_notification.view.*
import type.MediaType

class NotificationRvAdapter(private val context: Context,
                            private val list: List<NotificationsQuery.Notification?>,
                            private val unreadNotifications: Int,
                            private val listener: NotificationListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface NotificationListener {
        fun openUserPage(userId: Int)
        fun openActivityDetail(activityId: Int)
        fun openMediaPage(mediaId: Int, mediaType: MediaType)
        fun openThread(threadId: Int, siteUrl: String)
        fun openThreadReply(threadReplyId: Int, siteUrl: String)
    }

    private var latestNotificationId: Int? = null

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_notification, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = list[position]

            if (position < unreadNotifications) {
                holder.notificationUnreadLayout.visibility = View.VISIBLE
            } else {
                holder.notificationUnreadLayout.visibility = View.GONE
            }

            when (item?.__typename) {
                NotificationCategory.AIRING_NOTIFICATION.value -> {
                    handleAiringNotification(item.fragments.onAiringNotification!!, holder)
                    if (position == 0) latestNotificationId = item.fragments.onAiringNotification.id
                }

                NotificationCategory.FOLLOWING_NOTIFICATION.value -> {
                    handleFollowingNotification(item.fragments.onFollowingNotification!!, holder)
                    if (position == 0) latestNotificationId = item.fragments.onFollowingNotification.id
                }

                NotificationCategory.ACTIVITY_MESSAGE_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityMessageNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityMessageNotification.id
                }
                NotificationCategory.ACTIVITY_MENTION_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityMentionNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityMentionNotification.id
                }
                NotificationCategory.ACTIVITY_REPLY_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityReplyNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityReplyNotification.id
                }
                NotificationCategory.ACTIVITY_REPLY_SUBSCRIBED_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityReplySubscribedNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityReplySubscribedNotification.id
                }
                NotificationCategory.ACTIVITY_LIKE_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityLikeNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityLikeNotification.id
                }
                NotificationCategory.ACTIVITY_REPLY_LIKE_NOTIFICATION.value -> {
                    val notif = item.fragments.onActivityReplyLikeNotification!!
                    val activityNotification = ActivityNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.activityId)
                    handleActivityNotification(activityNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onActivityReplyLikeNotification.id
                }

                NotificationCategory.THREAD_COMMENT_MENTION_NOTIFICATION.value -> {
                    val notif = item.fragments.onThreadCommentMentionNotification!!
                    val threadReplyNotification = ThreadReplyNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.thread?.id, notif.thread?.title, notif.thread?.siteUrl, notif.commentId, notif.comment?.siteUrl)
                    handleThreadReplyNotification(threadReplyNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onThreadCommentMentionNotification.id
                }
                NotificationCategory.THREAD_COMMENT_REPLY_NOTIFICATION.value -> {
                    val notif = item.fragments.onThreadCommentReplyNotification!!
                    val threadReplyNotification = ThreadReplyNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.thread?.id, notif.thread?.title, notif.thread?.siteUrl, notif.commentId, notif.comment?.siteUrl)
                    handleThreadReplyNotification(threadReplyNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onThreadCommentReplyNotification.id
                }
                NotificationCategory.THREAD_COMMENT_SUBSCRIBED_NOTIFICATION.value -> {
                    val notif = item.fragments.onThreadCommentSubscribedNotification!!
                    val threadReplyNotification = ThreadReplyNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.thread?.id, notif.thread?.title, notif.thread?.siteUrl, notif.commentId, notif.comment?.siteUrl)
                    handleThreadReplyNotification(threadReplyNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onThreadCommentSubscribedNotification.id
                }
                NotificationCategory.THREAD_COMMENT_LIKE_NOTIFICATION.value -> {
                    val notif = item.fragments.onThreadCommentLikeNotification!!
                    val threadReplyNotification = ThreadReplyNotification(notif.userId, notif.user?.name, notif.user?.avatar?.large, notif.createdAt, notif.context, notif.thread?.id, notif.thread?.title, notif.thread?.siteUrl, notif.commentId, notif.comment?.siteUrl)
                    handleThreadReplyNotification(threadReplyNotification, holder)
                    if (position == 0) latestNotificationId = item.fragments.onThreadCommentLikeNotification.id
                }

                NotificationCategory.THREAD_LIKE_NOTIFICATION.value -> {
                    handleThreadNotification(item.fragments.onThreadLikeNotification!!, holder)
                    if (position == 0) latestNotificationId = item.fragments.onThreadLikeNotification.id
                }

                NotificationCategory.RELATED_MEDIA_ADDITION_NOTIFICATION.value -> {
                    handleRelationNotification(item.fragments.onRelatedMediaAdditionNotification!!, holder)
                    if (position == 0) latestNotificationId = item.fragments.onRelatedMediaAdditionNotification.id
                }
            }
        }
    }

    private fun handleAiringNotification(item: OnAiringNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.media?.coverImage?.large).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openMediaPage(item.animeId, item.media?.type ?: MediaType.ANIME)
        }

        try {
            holder.notificationText.text = "${item.contexts!![0]}${item.episode}${item.contexts[1]}${item.media?.title?.userPreferred}${item.contexts[2]}"
        } catch (e: Exception) {
            holder.notificationText.text = context.getString(R.string.episode_n_of_x_aired, item.episode, item.media?.title?.userPreferred)
        }

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openMediaPage(item.animeId, item.media?.type ?: MediaType.ANIME)
        }
    }

    private fun handleFollowingNotification(item: OnFollowingNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.user?.avatar?.large).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openUserPage(item.userId)
        }

        holder.notificationText.text = "${item.user?.name}${item.context}"

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openUserPage(item.userId)
        }
    }

    private fun handleActivityNotification(item: ActivityNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.userAvatar).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openUserPage(item.userId)
        }

        holder.notificationText.text = "${item.userName}${item.context}"

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openActivityDetail(item.activityId)
        }
    }

    private fun handleThreadReplyNotification(item: ThreadReplyNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.userAvatar).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openUserPage(item.userId)
        }

        holder.notificationText.text = "${item.userName}${item.context}${item.threadName}"

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openThreadReply(item.threadReplyId, item.threadReplyUrl ?: "")
        }
    }

    private fun handleThreadNotification(item: OnThreadLikeNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.user?.avatar?.large).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openUserPage(item.userId)
        }

        holder.notificationText.text = "${item.user?.name}${item.context}${item.thread?.title}"

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openThread(item.threadId, item.thread?.siteUrl ?: "")
        }
    }

    private fun handleRelationNotification(item: OnRelatedMediaAdditionNotification, holder: ItemViewHolder) {
        GlideApp.with(context).load(item.media?.coverImage?.large).into(holder.notificationImage)

        holder.notificationImage.setOnClickListener {
            listener.openMediaPage(item.mediaId, item.media?.type ?: MediaType.ANIME)
        }

        holder.notificationText.text = "${item.media?.title?.userPreferred}${item.context}"

        holder.notificationDateText.text = item.createdAt?.secondsToDateTime()

        holder.itemView.setOnClickListener {
            listener.openMediaPage(item.mediaId, item.media?.type ?: MediaType.ANIME)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun getLatestNotification(): Int? {
        return latestNotificationId
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notificationImage = view.notificationImage!!
        val notificationText = view.notificationText!!
        val notificationDateText = view.notificationDateText!!
        val notificationUnreadLayout = view.notificationUnreadLayout!!
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ActivityNotification(val userId: Int, val userName: String?, val userAvatar: String?, val createdAt: Int?, val context: String?, val activityId: Int)

    class ThreadReplyNotification(val userId: Int, val userName: String?, val userAvatar: String?, val createdAt: Int?, val context: String?, val threadId: Int?, val threadName: String?, val threadUrl: String?, val threadReplyId: Int, val threadReplyUrl: String?)
}